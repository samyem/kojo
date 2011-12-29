/*
 * Copyright (C) 2011 Lalit Pant <pant.lalit@gmail.com>
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
package music

import javax.swing.Timer
import javazoom.jl.player.Player
import java.io._
import util.Utils


trait Mp3Player {
  val pumpEvents: Boolean
  def showError(msg: String)
  val listener = SpriteCanvas.instance().megaListener // hack!

  @volatile var mp3Player: Option[Player] = None
  @volatile var bgmp3Player: Option[Player] = None
  @volatile var stopBg = false
  val baseDir = net.kogics.kojo.KojoCtx.instance.baseDir

  private def playHelper(mp3File: String)(fn: (FileInputStream) => Unit) {
    val f = new File(mp3File)
    val f2 = if (f.exists) f else new File(baseDir + mp3File)

    if (f2.exists) {
      val is = new FileInputStream(f2)
      fn(is)
//      is.close() - player closes the stream
    }
    else {
      showError("MP3 file - %s does not exist" format(mp3File))
    }
  }

  def play(mp3File: String) = synchronized {
    stopMp3Player()
    playHelper(mp3File) { is =>
      mp3Player = Some(new Player(is))
      Utils.runAsync {
        mp3Player.get.play
      }
    }
  }
  
  @volatile private var timer: Timer = _

  def playLoop(mp3File: String): Unit = synchronized {
    if (bgmp3Player.isDefined) {
      showError("Can't play second background mp3")
      return
    }
    
    def done() {
      stopBg = false
      bgmp3Player = None
      if (pumpEvents) {
        timer.stop()
        listener.pendingCommandsDone()
      }
    }

    def playLoop0() {
      playHelper(mp3File) { is =>
        bgmp3Player = Some(new Player(is))

        Utils.runAsync {
          if (stopBg) {
            done()
          }
          else {
            bgmp3Player.get.play
            playLoop0()
          }
        }
      }
    }
    
    if (pumpEvents) {
      listener.hasPendingCommands()
      timer = Utils.scheduleRec(0.5) {
        listener.hasPendingCommands()
      }
    }
    playLoop0()
  }

  def stopMp3Player() = synchronized {
    if (mp3Player.isDefined) {
      if (!mp3Player.get.isComplete) {
        mp3Player.get.close()
      }
      mp3Player = None
    }
  }

  def stopBgMp3Player()  = synchronized {
    if (bgmp3Player.isDefined) {
      stopBg = true
      if (!bgmp3Player.get.isComplete) {
        bgmp3Player.get.close()
      }
    }
  }
}

object KMp3 extends Singleton[KMp3] {
  protected def newInstance = new KMp3
}

class KMp3 extends Mp3Player {
  val pumpEvents = true
  def showError(msg: String) = println(msg)
}


