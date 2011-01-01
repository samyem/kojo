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

import org.jfugue.{Rhythm => JFRhythm, _}
import util.Utils
import javax.swing.Timer

object Music {
  def apply(mString: String) = new Music(new Pattern(mString))

  def apply(p: Pattern) = new Music(p)

  def apply(voice: core.Voice, n: Int = 1) = {
    new Music(voice.pattern(n))
  }
}

class Music(pattern: Pattern) {
  val listener = SpriteCanvas.instance().megaListener // hack!
  @volatile private var player: Player = _
  @volatile private var timer: Timer = _

  def play() {
    listener.hasPendingCommands()
    player = new Player()

    timer = Utils.scheduleRec(0.5) {
      listener.hasPendingCommands()
    }

    player.play(pattern)
    done()
  }

  def stop() {
    if (player.isPlaying) {
      player.stop()
    }
    done()
  }

  private def done() {
    timer.stop()
    player.close()
    listener.pendingCommandsDone()
  }
}
