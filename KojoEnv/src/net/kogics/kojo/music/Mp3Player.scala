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

import javazoom.jl.player.Player
import java.io._
import core.KojoCtx
import util.Utils


trait Mp3Player {
  var kojoCtx: KojoCtx
  def showError(msg: String)
  
  @volatile var mp3Player: Option[Player] = None
  @volatile var bgmp3Player: Option[Player] = None
  @volatile var stopBg = false

  private def playHelper(mp3File: String)(fn: (FileInputStream) => Unit) {
    val f = new File(mp3File)
    val f2 = if (f.exists) f else new File(kojoCtx.baseDir + mp3File)

    if (f2.exists) {
      val is = new FileInputStream(f2)
      fn(is)
//      is.close() - player closes the stream
    }
    else {
      showError("MP3 file - %s does not exist" format(mp3File))
    }
  }


  def play(mp3File: String) = playHelper(mp3File) {is =>
    stopMp3Player()
    Utils.runAsync {
      mp3Player = Some(new Player(is))
      mp3Player.get.play
    }
  }
  
  def playLoop(mp3File: String): Unit = playHelper(mp3File) {is =>
    if (bgmp3Player.isDefined && !bgmp3Player.get.isComplete) {
      showError("Can't play second background mp3")
      return
    }

    Utils.runAsync {
      bgmp3Player = Some(new Player(is))
      bgmp3Player.get.play
      if (!stopBg) {
        // loop bg music
        playLoop(mp3File)
      }
      else {
        stopBg = false
      }
    }
  }

  def stopMp3Player() {
    if (mp3Player.isDefined && !mp3Player.get.isComplete) {
      mp3Player.get.close()
      mp3Player = None
    }
  }

  def stopBgMp3Player() {
    if (bgmp3Player.isDefined && !bgmp3Player.get.isComplete) {
      stopBg = true
      bgmp3Player.get.close()
      bgmp3Player = None
    }
  }
}

object KMp3 extends Singleton[KMp3] {
  protected def newInstance = new KMp3
}

class KMp3 extends Mp3Player {
  var kojoCtx: KojoCtx = net.kogics.kojo.KojoCtx.instance
  def showError(msg: String) = println(msg)
}


