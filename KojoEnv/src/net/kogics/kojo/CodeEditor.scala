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

import java.util.logging._

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.nodes._

import util._

object CodeEditor extends Singleton[CodeEditor] {
  protected def newInstance = new CodeEditor
}

class CodeEditor private extends JPanel with core.CodeCompletionSupport {
  val Log = Logger.getLogger(getClass.getName);

  val tCanvas = sprite.SpriteCanvas.instance
  val commandHistory = CommandHistory.instance
  @volatile var pendingCommands = false

  setLayout(new BorderLayout)

  val (toolbar, runButton, stopButton, hNextButton, hPrevButton, clearButton) = makeToolbar()
  val output = makeOutput()
  val codeRunner = makeCodeRunner()
  val codePane = makeCodePane()

  val splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
                                 new JScrollPane(codePane), new JScrollPane(output))
  add(splitPane, BorderLayout.CENTER)
  setSpriteListener()
  codeRunner.runCode("welcome")

  Utils.schedule(3) {
    setCode(commandHistory.size)
  }

  def makeToolbar(): (JToolBar, JButton, JButton, JButton, JButton, JButton) = {

    val RunScript = "RunScript"
    val StopScript = "StopScript"
    val HistoryNext = "HistoryNext"
    val HistoryPrev = "HistoryPrev"
    val ClearOutput = "ClearOutput"

    var clearButton: JButton = null

    val actionListener = new ActionListener {
      def actionPerformed(e: ActionEvent) = e.getActionCommand match {
        case RunScript =>
          runCode()
        case StopScript =>
          codeRunner.interruptInterpreter()
          tCanvas.stop
        case HistoryNext =>
          historyMoveForward
        case HistoryPrev =>
          historyMoveBack
        case ClearOutput =>
          clrOutput()
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

    toolbar.add(runButton)

    stopButton.setEnabled(false)
    toolbar.add(stopButton)

    hPrevButton.setEnabled(false)
    toolbar.add(hPrevButton)

    hNextButton.setEnabled(false)
    toolbar.add(hNextButton)

    clearButton.setEnabled(false)
    toolbar.add(clearButton)

    add(toolbar, BorderLayout.NORTH)
    (toolbar, runButton, stopButton, hNextButton, hPrevButton, clearButton)
  }


  def makeOutput() = new JTextArea {
    // setEditable(false)
    setFont(new Font(Font.MONOSPACED, Font.BOLD, 15))
    setLineWrap(true)
    setWrapStyleWord(true)

    override def paste {}

    addKeyListener(new KeyAdapter {
        override def keyPressed(evt: KeyEvent) {
          evt.getKeyCode match {
            case KeyEvent.VK_UP => // let em through
            case KeyEvent.VK_DOWN =>
            case KeyEvent.VK_LEFT =>
            case KeyEvent.VK_RIGHT =>
            case KeyEvent.VK_V => evt.consume // disallow pasting
            case KeyEvent.VK_X => evt.consume // disallow cutting
            case kc if (evt.isControlDown) => // allow copying
            case _ => evt.consume // disallow everything else
          }
        }

        override def keyTyped(evt: KeyEvent) {
          evt.consume
        }
      })
  }

  def makeCodeRunner() = {
    val codeRunner = new xscala.ScalaCodeRunner(new RunContext {

        def reportRunError() {
          setCode(commandHistory.size-1)
        }

        def reportOutput(lineFragment: String) {
          def maybeTruncateOutput {
            val doc = output.getDocument
            if (doc.getLength > 50000) doc.remove(0, 10000)
          }

          Utils.runInSwingThread {
            maybeTruncateOutput
            output.append(lineFragment)
            output.setCaretPosition(output.getDocument().getLength())
            if (!clearButton.isEnabled) clearButton.setEnabled(true)
          }
        }

        def getCurrentOutput = output.getText

        def interpreterStarted {
          runButton.setEnabled(false)
          stopButton.setEnabled(true)
        }
      
        def interpreterDone {
          runButton.setEnabled(true)
          if (!pendingCommands) {
            stopButton.setEnabled(false)
          }
        }

        def clearOutput() = clrOutput()
      }, tCanvas)
    codeRunner
  }

  def isRunningEnabled = runButton.isEnabled

  
  def makeCodePane(): CodePane = {
    val codePane = new CodePane(codeRunner)

    codePane.addKeyListener(new KeyAdapter {
        override def keyPressed(evt: KeyEvent) {
          evt.getKeyCode match {
            case KeyEvent.VK_ENTER =>
              if(evt.isControlDown && isRunningEnabled) {
                runCode()
              }
            case KeyEvent.VK_UP =>
              if(evt.isControlDown) {
                historyMoveBack
                evt.consume
              }
            case KeyEvent.VK_DOWN =>
              if(evt.isControlDown) {
                historyMoveForward
                evt.consume
              }
            case _ => // do nothing special
          }
        }

      })
    codePane
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
        }
      })
  }

  def historyMoveBack {
    val prevCode = commandHistory.previous
    hPrevButton.setEnabled(commandHistory.hasPrevious)
    hNextButton.setEnabled(true)
  }

  def historyMoveForward {
    val nextCode = commandHistory.next
    if(!nextCode.isDefined) {
      hNextButton.setEnabled(false)
    }
    hPrevButton.setEnabled(true)
  }

  def setCode(historyIdx: Int) {
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
      }
      else {
        codePane.setText(null)
      }
      codePane.requestFocusInWindow
    }
  }

  def clrOutput() {
    Utils.runInSwingThread {
      output.setText(null)
      clearButton.setEnabled(false)
    }
  }

  def runCode() {
    // Runs on swing thread
    val code = codePane.getText()
    if (code == null || code.trim.length == 0) return

    commandHistory.add(code)
    codeRunner.runCode(code)
  }

  def stripCR(str: String) = str.replaceAll("\r\n", "\n")
  def methodCompletions(caretOffset: Int) = codeRunner.methodCompletions(stripCR(codePane.getText).substring(0, caretOffset))
  def varCompletions(caretOffset: Int) = codeRunner.varCompletions(stripCR(codePane.getText).substring(0, caretOffset))
  def keywordCompletions(caretOffset: Int) = codeRunner.keywordCompletions(stripCR(codePane.getText).substring(0, caretOffset))
}

trait RunContext {
  def reportRunError()
  def reportOutput(lineFragment: String)
  def getCurrentOutput(): String
  def interpreterStarted()
  def interpreterDone()
  def clearOutput()
}

class CodePane(codeRunner: xscala.ScalaCodeRunner) extends JEditorPane {

  val Log = Logger.getLogger(getClass.getName);
  val OutputDelimiter = codeRunner.OutputDelimiter

  def scheduleFocusRequest = Utils.schedule(3) {
    requestFocusInWindow
  }

  if (EventQueue.isDispatchThread) setKit
  else Utils.runInSwingThread(setKit _)

  def setKit {
    // trying to fix the null DataObject exception that sometimes shows up on startup
    setText("// write your turtle commands here")
    setEditorKit(org.openide.text.CloneableEditorSupport.getEditorKit("text/x-scala"))
  }

  setBackground(Color.white)
//  setFont(new Font(Font.MONOSPACED, Font.BOLD, 16))
  setPreferredSize(new Dimension(500, 200))
  setMinimumSize(new Dimension(100, 200))
}

