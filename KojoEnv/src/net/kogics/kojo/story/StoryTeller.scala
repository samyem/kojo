/*
 * Copyright (C) 2010 Lalit Pant <pant.lalit@gmail.com>
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
package story

import java.awt._
import java.awt.event._
import javax.swing._
import util.Utils
import util.NumberOrString
import util.NoS._
import javazoom.jl.player.Player
import java.io._
import javax.swing.text.html.HTMLDocument

object StoryTeller extends InitedSingleton[StoryTeller] {
  def initedInstance(kojoCtx: KojoCtx) = synchronized {
    instanceInit()
    val ret = instance()
    ret.kojoCtx = kojoCtx
    ret
  }

  protected def newInstance = new StoryTeller
}

class StoryTeller extends JPanel {
  val NoText = <span/>
  @volatile var kojoCtx: core.KojoCtx = _
  @volatile var content: xml.Node = NoText
  @volatile var mp3Player: Player = _
  @volatile var bgmp3Player: Player = _
  @volatile var running = false
  
  val pageFields = new collection.mutable.HashMap[String, JTextField]()

//  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  setLayout(new BorderLayout())

  val ep = new JEditorPane()
  ep.setEditorKit(new CustomHtmlEditorKit())

  ep.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
  ep.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20))
  ep.setEditable(false)
  val sp = new JScrollPane(ep)
  sp.setBorder(BorderFactory.createEmptyBorder())
  add(sp, BorderLayout.CENTER)

  val holder = new JPanel()
  holder.setBackground(Color.white)
  holder.setLayout(new BoxLayout(holder, BoxLayout.Y_AXIS))

  val uc = new JPanel
  uc.setBackground(Color.white)
  holder.add(uc)

  val cp = new JPanel
  cp.setBackground(Color.white)
  holder.add(cp)

  val statusBar = new JLabel()
//  statusBar.setPreferredSize(new Dimension(100, 16))
  val sHolder = new JPanel()
  sHolder.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1))
  sHolder.add(statusBar)
  holder.add(sHolder)
  add(holder, BorderLayout.SOUTH)

  def ensureVisible() {
    kojoCtx.makeStoryTellerVisible()
  }

  def baseDir = Utils.runInSwingThreadAndWait {
    CodeEditorTopComponent.findInstance().getLastLoadStoreDir() + "/"
  }

  def stopMp3Player() {
    if (mp3Player != null && !mp3Player.isComplete) {
      mp3Player.close()
    }
  }

  def stopBgMp3Player() {
    if (bgmp3Player != null && !bgmp3Player.isComplete) {
      bgmp3Player.close()
    }
  }

  private def clearHelper() {
    // needs to run on GUI thread
    newPage()
    setContent(NoText)
  }

  private def newPage() {
    // needs to run on GUI thread
    cp.removeAll()
    cp.setBorder(BorderFactory.createEmptyBorder())

    uc.removeAll()
    uc.setBorder(BorderFactory.createEmptyBorder())
    
    pageFields.clear()
    showStatusMsg("")
    
    repaint()
    stopMp3Player()
  }

  def clear() {
    running = true
    Utils.runInSwingThread {
      clearHelper()
      ensureVisible()
      val doc = ep.getDocument.asInstanceOf[HTMLDocument]
      doc.setBase(new java.net.URL("file:///" + baseDir))
    }
  }

  def done() {
    running = false
    Utils.runInSwingThread {
      clearHelper()
      stopBgMp3Player()
    }
  }

  def setContent(html: xml.Node) {
    Utils.runInSwingThread {
      content = html
      ep.setText(html.toString)
    }
  }

  def appendContent(html: xml.Node) {
    setContent(xml.Group(Array(content, html)))
  }

  def addField(label: String): JTextField = {
    val l = new JLabel(label)
    val tf = new JTextField("", 6)

    Utils.runInSwingThread {
      uc.add(l)
      uc.add(tf)
      uc.setBorder(BorderFactory.createEtchedBorder())
      pageFields += (label -> tf)
    }
    tf
  }

  def fieldValue[T](label: String, default: T)(implicit nos: NumberOrString[T]): T = {
    Utils.runInSwingThreadAndWait {
      val tf = pageFields.get(label)
      if (tf.isDefined) {
        val svalue = tf.get.getText
        if (svalue != null && svalue.trim != "") {
          try {
            nos.value(svalue)
          }
          catch {
            case ex: Exception =>
              showStatusError("Unable to convert value - %s - to required type %s" format(svalue, nos.typeName))
              throw ex
          }
        }
        else {
          tf.get.setText(default.toString)
          default
        }
      }
      else {
        showStatusError("Field with label - %s is not defined" format(label))
        throw new IllegalArgumentException("Field with label - %s is not defined" format(label))
      }
    }
  }

  def addButton(label: String)(fn: => Unit) {
    val but = new JButton(label)
    but.addActionListener(new ActionListener {
        def actionPerformed(e: ActionEvent) {
          showStatusMsg("")
          fn
        }
      })

    addUiComponent(but)
  }

  def addUiComponent(c: JComponent) {
    Utils.runInSwingThread {
      uc.add(c)
      uc.setBorder(BorderFactory.createEtchedBorder())
    }
  }

  def waitForUser() {
    val but = new JButton("Continue")
    import java.util.concurrent.CountDownLatch
    val latch = new CountDownLatch(1)
    but.addActionListener(new ActionListener {
        def actionPerformed(e: ActionEvent) {
          newPage()
          latch.countDown()
        }
      })

    Utils.runInSwingThread {
      cp.add(but)
      cp.setBorder(BorderFactory.createEtchedBorder())
    }
    latch.await()
  }

  addComponentListener(new ComponentAdapter {
      override def componentResized(e: ComponentEvent) {
        statusBar.setPreferredSize(new Dimension(getSize().width-6, 16))
      }
    })

  def showStatusMsg(msg: String) {
    statusBar.setForeground(Color.black)
    statusBar.setText(msg)
  }

  def showStatusError(msg: String) {
    statusBar.setForeground(Color.red)
    statusBar.setText(msg)
  }


  private def playHelper(mp3File: String)(fn: (FileInputStream) => Unit) {
    val f = new File(mp3File)
    val f2 = if (f.exists) f else new File(baseDir + mp3File)

    if (f2.exists) {
      val is = new FileInputStream(f2)
      fn(is)
//      is.close() - player closes the stream
    }
    else {
      Utils.runInSwingThread {
        showStatusError("MP3 file - %s does not exist" format(mp3File))
      }
    }
  }


  def play(mp3File: String) = playHelper(mp3File) {is =>
    stopMp3Player()
    Utils.runAsync {
      mp3Player = new Player(is)
      mp3Player.play
    }
  }
  
  def playInBg(mp3File: String): Unit = playHelper(mp3File) {is =>
    stopBgMp3Player()
    Utils.runAsync {
      bgmp3Player = new Player(is)
      bgmp3Player.play
      if (running) {
        // loop bg music
        playInBg(mp3File)
      }
    }
  }
}
