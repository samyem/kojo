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

trait SCanvas extends SpriteMover {
  def clear(): Unit
  def clearPuzzlers(): Unit
  def newTurtle(x: Int, y: Int): Sprite
  def newPuzzler(x: Int, y: Int): Sprite
  def turtle0: Sprite
  def gridOn(): Unit
  def gridOff(): Unit
}
