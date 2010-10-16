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
  @volatile var kojoCtx: KojoCtx = _
  @volatile var content: xml.Node = NoText
  @volatile var mp3Player: Player = _

  this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))

  val ep = new JEditorPane()
  ep.setEditorKit(new CustomHtmlEditorKit())

  ep.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
  ep.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20))
  ep.setEditable(false)
  add(ep)

  val cp = new JPanel
  cp.setBackground(Color.white)
  add(cp)

  def baseDir = Utils.runInSwingThreadAndWait {
    CodeEditorTopComponent.findInstance().getLastLoadStoreDir() + "/"
  }

  def stopMp3Player() {
    if (mp3Player != null && !mp3Player.isComplete) {
      mp3Player.close()
    }
  }

  def clear() {
    Utils.runInSwingThread {
      cp.removeAll()
      val doc = ep.getDocument.asInstanceOf[HTMLDocument]
      doc.setBase(new java.net.URL("file:///" + baseDir))
      setContent(NoText)
      repaint()
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

  def addButton(label: String)(fn: => Unit) {
    val but = new JButton(label)
    but.addActionListener(new ActionListener {
        def actionPerformed(e: ActionEvent) {
          fn
        }
      })
    
    Utils.runInSwingThread {
      add(but)
    }
  }

  def showContinueButton() {
    val but = new JButton("Continue")
    import java.util.concurrent.CountDownLatch
    val latch = new CountDownLatch(1)
    but.addActionListener(new ActionListener {
        def actionPerformed(e: ActionEvent) {
          cp.remove(but)
          latch.countDown()
        }
      })

    Utils.runInSwingThread {
      cp.add(but)
    }
    latch.await()
    stopMp3Player()
    repaint()
  }

  def play(mp3File: String) {
    val f = new File(mp3File)
    val f2 = if (f.exists) f else new File(baseDir + mp3File)

    if (f2.exists) {
      stopMp3Player()
      val is = new FileInputStream(f2)
      mp3Player = new Player(is)
      new Thread(new Runnable {
          def run {
            mp3Player.play
          }
        }).start
    }
  }
}
