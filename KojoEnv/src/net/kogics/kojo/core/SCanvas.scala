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

trait SCanvas {
  val turtle0: Turtle
  val figure0: Figure
  def clear(): Unit
  def clearPuzzlers(): Unit
  def newTurtle(x: Int, y: Int): Turtle
  def newPuzzler(x: Int, y: Int): Turtle
  def gridOn(): Unit
  def gridOff(): Unit
  def newFigure(x: Int, y: Int): Figure
  def zoom(factor: Double, cx: Double, cy: Double)
  def undo(): Unit
  def setBackgroundColor(color: java.awt.Color)
}
