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

trait Throttler {
  val MaxDelay = 1000 // ms
  val ThrottleThreshold = 10 // ms
  @volatile var lastCallTime: Long = System.currentTimeMillis
  @volatile var avgDelay = 1000f

  /**
   * Slow things down if stuff is happening too quickly
   * Meant to slow down runaway computation inside the interpreter, so that the
   * user can at least close down the app without too much of a problem
   */
  def throttle {
    val currTime = System.currentTimeMillis
    val delta =  currTime - lastCallTime
    lastCallTime = currTime

    avgDelay = (avgDelay + delta) / 2

    if (avgDelay > MaxDelay) avgDelay = MaxDelay
    else if (avgDelay < ThrottleThreshold) Thread.sleep(1) // Throws interrupted exception if the thread has been interrupted
  }
}

