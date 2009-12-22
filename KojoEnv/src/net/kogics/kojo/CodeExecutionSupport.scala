/*
 * Copyright (C) 2009 Lalit Pant <pant.lalit@gmail.com>
 *
 * The contents of this file are subject to the GNU General Public License
 * Version 3 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.gnu.org/copyleft/gpl.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 */
package net.kogics.kojo

import javax.swing._
import java.awt.{List => AwtList, _}
import java.awt.event._
import javax.swing.event._

import java.util.concurrent.CountDownLatch
import java.util.logging._

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.nodes._

import util._
import net.kogics.kojo.core.RunContext

import org.openide.windows._
import org.openide.awt.UndoRedo

object CodeExecutionSupport extends Singleton[CodeExecutionSupport] {
  private var instanceInited = false

  protected override def instanceCheck() {
    if (!instanceInited) throw new IllegalStateException("Instance not initialized")
  }

  def initedInstance(codePane: JEditorPane, manager: UndoRedo.Manager) = synchronized {
    instanceInited = true
    val ret = instance()
    instance.setCodePane(codePane)
    instance.undoRedoManager = manager
    ret
  }

  protected def newInstance = new CodeExecutionSupport()
}

class CodeExecutionSupport private extends core.CodeCompletionSupport {
  val Log = Logger.getLogger(getClass.getName);

  val tCanvas = sprite.SpriteCanvas.instance
  tCanvas.outputFn = showOutput _

  val commandHistory = CommandHistory.instance
  val historyManager = new HistoryManager()
  @volatile var pendingCommands = false
  val findAction = new org.netbeans.editor.ext.ExtKit.FindAction()
  val replaceAction = new org.netbeans.editor.ext.ExtKit.ReplaceAction()

  val (toolbar, runButton, stopButton, hNextButton, hPrevButton, clearButton, undoButton) = makeToolbar()

  @volatile var runMonitor: RunMonitor = new NoOpRunMonitor()
  var undoRedoManager: UndoRedo.Manager = _ 
  var codePane: JEditorPane = _

  val codeRunner = makeCodeRunner()
  
  lazy val IO = makeOutput2()

  val statusStrip = new StatusStrip()

  setSpriteListener()
  doWelcome()

  def setCodePane(cp: JEditorPane) {
    codePane = cp;
    addCodePaneShortcuts()
    statusStrip.linkToPane()
  }

  def doWelcome() = {
    val msg = """Welcome to Kojo!
    |
    |Here are some tips to get you started:
    |* You can right-click on (most) windows to access context-sensitive actions.
    |* You can run the help command, i.e. type help and press Ctrl+Enter in the script window, if you need assistance.
    |
    |""".stripMargin
    
    showOutput(msg)
  }

  def makeOutput2(): org.openide.windows.InputOutput = {
    val ioc = IOContainer.create(OutputTopComponent.findInstance)
    IOProvider.getDefault().getIO("Script Output", Array[Action](), ioc)
  }

  def makeToolbar(): (JToolBar, JButton, JButton, JButton, JButton, JButton, JButton) = {

    val RunScript = "RunScript"
    val StopScript = "StopScript"
    val HistoryNext = "HistoryNext"
    val HistoryPrev = "HistoryPrev"
    val ClearOutput = "ClearOutput"
    val UndoCommand = "UndoCommand"

    var clearButton: JButton = null

    val actionListener = new ActionListener {
      def actionPerformed(e: ActionEvent) = e.getActionCommand match {
        case RunScript =>
          runCode()
        case StopScript =>
          codeRunner.interruptInterpreter()
          tCanvas.stop
        case HistoryNext =>
          loadCodeFromHistoryNext
        case HistoryPrev =>
          loadCodeFromHistoryPrev
        case ClearOutput =>
          clrOutput()
        case UndoCommand =>
          smartUndo()
      }
    }

    def makeNavigationButton(imageFile: String, actionCommand: String,
                             toolTipText: String, altText: String): JButton = {
      val button = new JButton()
      button.setActionCommand(actionCommand)
      button.setToolTipText(toolTipText)
      button.addActionListener(actionListener)
      button.setIcon(Utils.loadIcon(imageFile, altText))
      // button.setMnemonic(KeyEvent.VK_ENTER)
      button;
    }


    val toolbar = new JToolBar
    toolbar.setPreferredSize(new Dimension(100, 24))

    val runButton = makeNavigationButton("/images/run24.png", RunScript, "Run Script (Ctrl + Enter)", "Run the Code")
    val stopButton = makeNavigationButton("/images/stop24.png", StopScript, "Stop Script/Animation", "Stop the Code")
    val hNextButton = makeNavigationButton("/images/history-next.png", HistoryNext, "Go to Next Script in History (Ctrl + Down Arrow)", "Next in History")
    val hPrevButton = makeNavigationButton("/images/history-prev.png", HistoryPrev, "Goto Previous Script in History (Ctrl + Up Arrow)", "Prev in History")
    clearButton = makeNavigationButton("/images/clear24.png", ClearOutput, "Clear Output", "Clear the Output")
    val undoButton = makeNavigationButton("/images/undo.png", UndoCommand, "Undo Last Turtle Command", "Undo")

    toolbar.add(runButton)

    stopButton.setEnabled(false)
    toolbar.add(stopButton)

    hPrevButton.setEnabled(false)
    toolbar.add(hPrevButton)

    hNextButton.setEnabled(false)
    toolbar.add(hNextButton)

    clearButton.setEnabled(false)
    toolbar.add(clearButton)

    undoButton.setEnabled(false)
    toolbar.add(undoButton)

    (toolbar, runButton, stopButton, hNextButton, hPrevButton, clearButton, undoButton)
  }

  def makeCodeRunner(): core.CodeRunner = {
    new core.ProxyCodeRunner(makeRealCodeRunner _)
  }

  def makeRealCodeRunner: core.CodeRunner = {
    val codeRunner = new xscala.ScalaCodeRunner(new RunContext {

        def onInterpreterStart {
          codePane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          runButton.setEnabled(false)
          stopButton.setEnabled(true)
          runMonitor.onRunStart()
        }

        def onRunError() {
          historyManager.codeRunError()
          interpreterDone()
          Utils.runInSwingThread {
            statusStrip.onError()
          }
        }

        def onRunSuccess() = {
          interpreterDone()
          Utils.runInSwingThread {
            statusStrip.onSuccess()
          }
        }

        def onRunInterpError() = interpreterDone()

        def reportOutput(outText: String) {
          showOutput(outText)
          runMonitor.reportOutput(outText)
        }

        def reportErrorMsg(errMsg: String) {
          showErrorMsg(errMsg)
          runMonitor.reportOutput(errMsg)
        }

        def reportErrorText(errText: String) {
          showErrorText(errText)
          runMonitor.reportOutput(errText)
        }

        private def interpreterDone() {
          runButton.setEnabled(true)
          if (!pendingCommands) {
            stopButton.setEnabled(false)
          }
          runMonitor.onRunEnd()
          Utils.runInSwingThread {
            undoRedoManager.discardAllEdits()
          }
        }

        def clearOutput() = clrOutput()
      }, tCanvas)
    codeRunner
  }

  def isRunningEnabled = runButton.isEnabled

  def addCodePaneShortcuts() {
    codePane.addKeyListener(new KeyAdapter {
        override def keyPressed(evt: KeyEvent) {
          evt.getKeyCode match {
            case KeyEvent.VK_ENTER =>
              if(evt.isControlDown && isRunningEnabled) {
                runCode()
                evt.consume
              }
            case KeyEvent.VK_UP =>
              if(evt.isControlDown) {
                loadCodeFromHistoryPrev
                evt.consume
              }
            case KeyEvent.VK_DOWN =>
              if(evt.isControlDown) {
                loadCodeFromHistoryNext
                evt.consume
              }
            case _ => // do nothing special
          }
        }

      })
  }

  def setSpriteListener() {
    tCanvas.setSpriteListener(new sprite.AbstractSpriteListener {
        def interpreterDone = runButton.isEnabled
        override def hasPendingCommands {
          pendingCommands = true
          stopButton.setEnabled(true)
        }
        override def pendingCommandsDone {
          pendingCommands = false
          if (interpreterDone) stopButton.setEnabled(false)
          if (tCanvas.hasUndoHistory) undoButton.setEnabled(true) else undoButton.setEnabled(false)
        }
      })
  }

  def loadCodeFromHistoryPrev = historyManager.historyMoveBack
  def loadCodeFromHistoryNext = historyManager.historyMoveForward
  def loadCodeFromHistory(historyIdx: Int) = historyManager.setCode(historyIdx)

  def smartUndo() {
    if (codePane.getText.trim() == "") {
      // if code pane is blank, do undo via the interp, so that we go back in
      // history to the last command/script (which we are trying to undo)
      codePane.setText("undo")
      runCode()
    }
    else {
      // if code pane is not blank, selected text was run or the user has loaded
      // something from history (or an error occurred - not relevant here)
      // call coderunner directly so that the buffer is retained
      codeRunner.runCode("undo")
    }
  }

  def showFindDialog() {
    findAction.actionPerformed(null, codePane)
    tweakFindReplaceDialog()
  }

  def showReplaceDialog() {
    replaceAction.actionPerformed(null, codePane)
    tweakFindReplaceDialog()
  }

  def tweakFindReplaceDialog() {
    // hacks to control appearance and behavior of Find/Replace Dialog
    import org.netbeans.editor.ext.FindDialogSupport
    try {
      // work around 'disabled find button' bug in Find Dialog
      if (codePane.getSelectedText != null) {
        val findBtnsField = classOf[FindDialogSupport].getDeclaredField("findButtons")
        findBtnsField.setAccessible(true)
        val findBtn = findBtnsField.get(null).asInstanceOf[Array[JButton]](0)
        findBtn.setEnabled(true)
      }

      // hide help button
      val findDialogField = classOf[FindDialogSupport].getDeclaredField("findDialog")
      findDialogField.setAccessible(true)
      val findDlg = findDialogField.get(null).asInstanceOf[Dialog]
      val rootPane = findDlg.getComponent(0).asInstanceOf[JRootPane]
      val pane = rootPane.getComponent(1).asInstanceOf[JLayeredPane]
      val panel = pane.getComponent(0).asInstanceOf[JPanel]
      val panel2 = panel.getComponent(1).asInstanceOf[JPanel]
      panel2.getComponents.foreach {c => c match {
          case button: JButton => if (button.getText == "Help") button.setVisible(false)
          case _ => // pass
        }
      }
    }
    catch {
      case t: Throwable => // pass
    }
  }

  def locateError(errorText0: String) {

    def showHelpMessage() {
      val msg = """
      |The error text is not present in your current script.
      |
      |This can happen - if you made a change to your script after seeing an
      |error message in the output window, and *then* tried to locate the error
      |by clicking on the error hyperlink.
      """.stripMargin
      JOptionPane.showMessageDialog(null, msg, "Error Locator", JOptionPane.INFORMATION_MESSAGE)
    }

    if (errorText0 == null || errorText0.trim() == "") {
      return
    }
    else {
      val errorText = errorText0.trim()
      val code = stripCR(codePane.getText)
      val idx = code.indexOf(errorText)
      if (idx == -1) {
        showHelpMessage()
      }
      else {
        codePane.select(idx, idx + errorText.length)
        val idx2 = code.lastIndexOf(errorText)
        if (idx != idx2) showFindDialog()
      }
    }
  }

  def clrOutput() {
    Utils.runInSwingThread {
      IO.getOut().reset()
      clearButton.setEnabled(false)
    }
  }

  val listener = new OutputListener() {
    def outputLineAction(ev: OutputEvent) {
      locateError(ev.getLine)
    }

    def outputLineSelected(ev: OutputEvent) {
      // Let's not do anything special.
    }

    def outputLineCleared(ev: OutputEvent) {
      // Leave it blank, no state to remove.
    }
  }

  def enableClearButton() = if (!clearButton.isEnabled) clearButton.setEnabled(true)

  def showOutput(outText: String) {
    Utils.runInSwingThread {
      IOColorPrint.print(IO, outText, Color.black);
      enableClearButton()
    }
  }

  def showErrorMsg(errMsg: String) {
    Utils.runInSwingThread {
      IOColorPrint.print(IO, errMsg, Color.red);
      enableClearButton()
    }
  }

  def showErrorText(errText: String) {
    Utils.runInSwingThread {
      IOColorPrint.print(IO, errText, listener, true, Color.red);
      enableClearButton()
    }
  }

  def runCode() {
    // Runs on swing thread
    
    // now that we use the proxy code runner, disable the run button right away and change
    // the cursor so that the user gets some feedback the first time he runs something
    // - relevant if the proxy is still loading the real runner
    runButton.setEnabled(false)
    codePane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    val code = codePane.getText()
    if (code == null || code.trim.length == 0) return
    if (code.contains(CommandHistory.Separator)) {
      showOutput(
        """|Sorry, you can't have the word %s in your script. This is an
           |internal reserved word within Kojo.
           |Please change %s to something else and rerun your script.""".stripMargin.format(CommandHistory.Separator, CommandHistory.Separator))
      return
    }

    val selStart = codePane.getSelectionStart
    val selEnd = codePane.getSelectionEnd

    val selectedCode = codePane.getSelectedText
    val codeToRun = if (selectedCode == null) code else selectedCode

    try {
      // always add full code to history
      historyManager.codeRun(code, selectedCode != null, (selStart, selEnd))
    }
    catch {
      case ioe: java.io.IOException => showOutput("Unable to save history to disk: %s\n" format(ioe.getMessage))
    }
    codeRunner.runCode(codeToRun)
  }

  def stripCR(str: String) = str.replaceAll("\r\n", "\n")
  def codeFragment(caretOffset: Int) = {
    val cpt = codePane.getText
    if (caretOffset > cpt.length) ""
    else stripCR(cpt).substring(0, caretOffset)
  }
  def methodCompletions(caretOffset: Int) = codeRunner.methodCompletions(codeFragment(caretOffset))
  def varCompletions(caretOffset: Int) = codeRunner.varCompletions(codeFragment(caretOffset))
  def keywordCompletions(caretOffset: Int) = codeRunner.keywordCompletions(codeFragment(caretOffset))

  def loadFrom(file: java.io.File) {
    import util.RichFile._
    val script = file.readAsString
    codePane.setText(script)
  }

  def saveTo(file0: java.io.File) {
    import util.RichFile._
    val script = codePane.getText()

    val file = if (file0.getName.endsWith(".kojo")) file0
    else new java.io.File(file0.getAbsolutePath + ".kojo")

    file.write(script)
  }

  class HistoryManager {
    var _selRange = (0, 0)

    def historyMoveBack {
      // depend on history listener mechanism to move back
      val prevCode = commandHistory.previous
      hPrevButton.setEnabled(commandHistory.hasPrevious)
      hNextButton.setEnabled(true)
    }

    def historyMoveForward {
      // depend on history listener mechanism to move forward
      val nextCode = commandHistory.next
      if(!nextCode.isDefined) {
        hNextButton.setEnabled(false)
      }
      hPrevButton.setEnabled(true)
    }

    def setCode(historyIdx: Int, selRange: (Int, Int) = (0,0)) {
      if (commandHistory.size > 0 && historyIdx != 0)
        hPrevButton.setEnabled(true)
      else
        hPrevButton.setEnabled(false)

      if (historyIdx < commandHistory.size)
        hNextButton.setEnabled(true)
      else
        hNextButton.setEnabled(false)

      val codeAtIdx = commandHistory.toPosition(historyIdx)
      Utils.runInSwingThread {
        if(codeAtIdx.isDefined) {
          codePane.setText(codeAtIdx.get)
          if (selRange._1 != selRange._2) {
            codePane.setSelectionStart(selRange._1)
            codePane.setSelectionEnd(selRange._2)
          }
        }
        else {
          codePane.setText(null)
        }
        codePane.requestFocusInWindow
      }
    }

    def codeRunError() = {
      setCode(commandHistory.size-1, (_selRange._1, _selRange._2))
      _selRange = (0,0)
    }

    def codeRun(code: String, stayPut: Boolean, selRange: (Int, Int)) {
      _selRange = selRange
      val tcode = code.trim()
      val undo = (tcode == "undo"
                  || tcode == "undo()"
                  || tcode.endsWith(".undo")
                  || tcode.endsWith(".undo()"))

      if (!undo) {
        // automatically shows the last (blank) history entry through listener mechanism
        commandHistory.add(code)
      }
      else {
        // undo
        _selRange = (0, 0)
      }
      if (stayPut || undo) {
        setCode(commandHistory.size-1, (_selRange._1, _selRange._2))
      }
    }
  }

  def runCodeWithOutputCapture(): String = {
    runMonitor = new OutputCapturingRunner()
    val ret = runMonitor.asInstanceOf[OutputCapturingRunner].go()
    runMonitor = new NoOpRunMonitor()
    ret
  }

  class OutputCapturingRunner extends RunMonitor {
    val outputx: StringBuilder = new StringBuilder()
    val latch = new CountDownLatch(1)

    def reportOutput(outText: String) = captureOutput(outText)
    def onRunStart() {}
    def onRunEnd() = latch.countDown()

    def go(): String = {
      runCode()
      latch.await()
      outputx.toString
    }

    def captureOutput(output: String) {
      outputx.append(output)
    }
  }

  class StatusStrip extends JPanel {
    val ErrorColor = new Color(0xff1a1a) // reddish
    val SuccessColor = new Color(0x33ff33) // greenish
    val NeutralColor = new Color(0xf0f0f0) // very light gray
    val StripWidth = 3

    setBackground(NeutralColor)
    setPreferredSize(new Dimension(StripWidth, 10))

    def linkToPane() {
      codePane.getDocument.addDocumentListener(new DocumentListener {
          def insertUpdate(e: DocumentEvent) = onDocChange()
          def removeUpdate(e: DocumentEvent) = onDocChange()
          def changedUpdate(e: DocumentEvent) {}
        })
    }

    def onSuccess() {
      setBackground(SuccessColor)
    }

    def onError() {
      setBackground(ErrorColor)
    }

    def onDocChange() {
      if (getBackground != NeutralColor) setBackground(NeutralColor)
    }
  }

}

trait RunMonitor {
  def reportOutput(outText: String)
  def onRunStart()
  def onRunEnd()
}

class NoOpRunMonitor extends RunMonitor {
  def reportOutput(outText: String) {}
  def onRunStart() {}
  def onRunEnd() {}
}
