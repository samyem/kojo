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

import util.Utils
import util.Utils.withLock
import org.jfugue.{Rhythm => JFRhythm, _}
import java.util.logging._
import java.util.concurrent.locks.ReentrantLock
import javax.swing.Timer

object FuguePlayer extends Singleton[FuguePlayer] {
  protected def newInstance = new FuguePlayer
}

class FuguePlayer {
  val Log = Logger.getLogger(getClass.getName)
  private var currMusic: Option[Music] = None
  private var currBgMusic: Option[Music] = None
  val playLock = new ReentrantLock
  val started = playLock.newCondition
  val done = playLock.newCondition
  val stopped = playLock.newCondition
  var stopBg = false
  var stopFg = false
  val listener = SpriteCanvas.instance().megaListener // hack!
  @volatile private var timer: Timer = _
  val pumpEvents: Boolean = true

  private def stopAndCreate(voice: core.Voice, n: Int) {
    if (n > 10) {
      throw new IllegalArgumentException("Score repeat count cannot be more than 10")
    }
    
    stopMusic()    
    currMusic = Some(Music(voice, n))
  }
  
  def playMusic(voice: core.Voice, n: Int = 1) {
    withLock(playLock) {
      stopAndCreate(voice, n)
      Utils.runAsync {
        withLock(playLock) {
          started.signal()
          val music = currMusic.get
          playLock.unlock()
          music.play()
          playLock.lock()
          stopFg = false
          currMusic = None
          stopPumpingEvents()
          stopped.signal()
        }
      }
      started.await()
      startPumpingEvents()      
      // make race window smaller
      // race - subsequent call to play/stop happens before async music actually starts playing
      Thread.sleep(100)
    }
  }

  def playMusicUntilDone(voice: core.Voice, n: Int = 1) {
    withLock(playLock) {
      stopAndCreate(voice, n)
      Utils.runAsync {
        withLock(playLock) {
          val music = currMusic.get
          playLock.unlock()
          music.play()
          playLock.lock()
          done.signal()
          stopFg = false
          currMusic = None
          stopPumpingEvents()
          stopped.signal()
        }
      }
      startPumpingEvents()
      done.await()
    }
  }
  
  private def startPumpingEvents() {
    if (pumpEvents) {
      listener.hasPendingCommands()
      timer = Utils.scheduleRec(0.5) {
        listener.hasPendingCommands()
      }
    }
  }

  private def stopPumpingEvents() {
    if (pumpEvents) {
      timer.stop()
      listener.pendingCommandsDone()
      Utils.schedule(0.5) {
        listener.pendingCommandsDone()
      }
    }
  }
  
  def playMusicLoop(voice: core.Voice) {

    def playLoop0() {
      Utils.runAsync {
        withLock(playLock) {
          if (stopBg) {
            stopBg = false
            currBgMusic = None
            stopPumpingEvents()
            stopped.signal()
          }
          else {
            val music = currBgMusic.get
            playLock.unlock()
            music.play
            playLock.lock()
            currBgMusic = Some(Music(voice, 5))
            playLoop0()
          }
        }
      }
    }

    withLock(playLock) {
      stopBgMusic()
      currBgMusic = Some(Music(voice, 5))
      playLoop0()
      startPumpingEvents()
    }       
  }

  def stopMusic() {
    withLock(playLock) {
      if (currMusic.isDefined) {
        stopFg = true
        currMusic.get.stop()
        while(stopFg) {
          stopped.await()
        }
      }
    }
  }

  def stopBgMusic() {
    withLock(playLock) {
      if (currBgMusic.isDefined) {
        stopBg = true
        currBgMusic.get.stop()
        while(stopBg) {
          stopped.await()
        }
      }
    }
  }
}
