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
package music

import java.util.concurrent.atomic.AtomicBoolean
import scala.actors._
import scala.actors.Actor._

import org.jfugue.{Rhythm => JFRhythm, _}

case class VoiceDef(v: core.Voice, n: Int, valid: AtomicBoolean)

object MusicPlayer extends Singleton[MusicPlayer] {
  protected def newInstance = new MusicPlayer
}

class MusicPlayer {
  @volatile private var currMusic: Music = _
  @volatile private var validBool = new AtomicBoolean(true)

  val asyncPlayer = actor {
    while(true) {
      receive {
        case VoiceDef(v, n, valid) =>
          if (valid.get) {
            currMusic = Music(v, n)
            currMusic.play()
          }
      }
    }
  }

  def playMusic(voice: core.Voice, n: Int = 1) {
    asyncPlayer ! VoiceDef(voice, n, validBool)
  }

  def stopMusic() {
    validBool.set(false)
    validBool = new AtomicBoolean(true)
    currMusic.stop()
  }
}
