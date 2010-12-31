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

import scala.actors._
import scala.actors.Actor._

import org.jfugue.{Rhythm => JFRhythm, _}

case class MusicDef(p: Pattern)
case class VoiceDef(ms: core.Voice, n: Int)

object MusicPlayer extends Singleton[MusicPlayer] {
  protected def newInstance = {
    val p = new MusicPlayer
    p.start()
    p
  }
}

class MusicPlayer extends Actor {
  def act {
    while(true) {
      receive {
        case MusicDef(p) => Music(p).play()
        case VoiceDef(ms, n) => Music(ms, n).play()
      }
    }
  }
}
