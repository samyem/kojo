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
package net.kogics.kojo.util

object Throttler {
  val systemThrottler = new Throttler(1)
  
  def throttle() = systemThrottler.throttle()
}

class Throttler(size: Int) {

  // Throttling is always supposed to happen on the Interp
  // thread. So - there's no volatile or synchronized stuff in this class

  val MaxDelay = 1000 // ms
  val ThrottleThreshold = 10 // ms
  val MaxUninterruptibleCalls = 10
  var lastCallTime: Long = System.currentTimeMillis
  var avgDelay = 1000f
  var uninterruptibleCalls = 0

  /**
   * Slow things down if stuff is happening too quickly
   * Meant to slow down runaway computation inside the interpreter, so that the
   * user can interrupt the runaway thread
   */
  def throttle() {
    val currTime = System.currentTimeMillis
    val delta =  currTime - lastCallTime
    lastCallTime = currTime

    avgDelay = (avgDelay + delta) / 2

    if (avgDelay < ThrottleThreshold) {
      allowInterruption()
    }
    else {
      uninterruptibleCalls += 1
      if (avgDelay > MaxDelay) {
        avgDelay = MaxDelay
      }
    }

    if (uninterruptibleCalls > MaxUninterruptibleCalls) {
      allowInterruption()
    }
  }

  def allowInterruption() {
    uninterruptibleCalls = 0
    Thread.sleep(size) // Throws interrupted exception if the thread has been interrupted
  }
}

