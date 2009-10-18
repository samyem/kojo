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
package net.kogics.kojo.core

trait SpriteMover {
  def forward(n: Double): Unit
  def turn(angle: Double): Unit
  def right(): Unit
  def left(): Unit
  def penUp(): Unit
  def penDown(): Unit
  def setPenColor(color: java.awt.Color): Unit
  def setPenThickness(t: Double): Unit
  def setFillColor(color: java.awt.Color): Unit
  def towards(x: Double, y: Double)
  def position: (Double, Double)
  def heading: Double
  def jumpTo(x: Double, y: Double)
  def moveTo(x: Double, y: Double)
  def animationDelay: Long
  def setAnimationDelay(d: Long)
  def beamsOn(): Unit
  def beamsOff(): Unit
}
