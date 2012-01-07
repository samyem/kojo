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
import org.jfugue.{Rhythm => JFRhythm, _}
import java.util.logging._
import java.util.concurrent.locks.ReentrantLock

object MusicPlayer extends Singleton[MusicPlayer] {
  protected def newInstance = new MusicPlayer
}

class MusicPlayer {
  val Log = Logger.getLogger(getClass.getName)
  @volatile private var currMusic: Option[Music] = None
  @volatile private var currBgMusic: Option[Music] = None
  val playLock = new ReentrantLock
  val started = playLock.newCondition
  val done = playLock.newCondition
  var stopBg = false

  private def stopAndCreate(voice: core.Voice, n: Int) {
    if (n > 10) {
      throw new IllegalArgumentException("Score repeat count cannot be more than 10")
    }
    
    stopMusic()    
    currMusic = Some(Music(voice, n))
  }
  
  private def playMusicSignalStart(m: Music) {
    playLock.lock()
    started.signal()
    playLock.unlock()
    m.play()
  }
  
  private def playMusicSignalDone(m: Music) {
    m.play()
    playLock.lock()
    done.signal()
    playLock.unlock()
  }
  
  def playMusic(voice: core.Voice, n: Int = 1) {
    playLock.lock()
    try {
      stopAndCreate(voice, n)
      Utils.runAsync {
        playMusicSignalStart(currMusic.get)
      }
      started.await()
      // make race window smaller
      // race - subsequent call to play/stop happens before async music actually starts playing
      Thread.sleep(100)
    }
    finally {
      playLock.unlock()
    }
  }

  def playMusicLoop(voice: core.Voice) {
    def done() {
      playLock.lock()
      stopBg = false
      currBgMusic = None
      playLock.unlock()
    }
    def playLoop0() {
      playLock.lock()
      try {
        currBgMusic = Some(Music(voice, 5))
        Utils.runAsync {
          if (stopBg) {
            done()
          }
          else {
            currBgMusic.get.play
            playLoop0()
          }
        }
      }
      finally {
        playLock.unlock()
      }
    }
    
    playLock.lock()
    try {
      if (currBgMusic.isDefined) {
        println("Can't play second background voice.")
      }
      else {
        playLoop0()
      }
    }
    finally {
      playLock.unlock()
    }
  }

  def playMusicUntilDone(voice: core.Voice, n: Int = 1) {
    playLock.lock()
    try {
      stopAndCreate(voice, n)
      Utils.runAsync {
        playMusicSignalDone(currMusic.get)
      }
      done.await()
    }
    finally {
      playLock.unlock()
    }
  }

  def stopMusic() {
    playLock.lock()
    try {
      if (currMusic.isDefined) {
        currMusic.get.stop()
        currMusic = None
      }
    }
    finally {
      playLock.unlock()
    }
  }

  def stopBgMusic() {
    playLock.lock()
    try {
      if (currBgMusic.isDefined) {
        stopBg = true
        currBgMusic.get.stop()
      }
    }
    finally {
      playLock.unlock()
    }
  }
}
