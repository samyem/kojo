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
package figure

import edu.umd.cs.piccolo.PCanvas

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.nodes._

import core.Point

class FigRoundRectangle(val canvas: PCanvas, bLeft: Point, tRight: Point, rx: Double, ry: Double) extends core.RoundRectangle(bLeft, tRight, rx, ry)  with FigShape {
  val pRect = PPath.createRoundRectangle(bLeft.x.toFloat, bLeft.y.toFloat, width.toFloat, height.toFloat, rx.toFloat, ry.toFloat)

  protected val piccoloNode = pRect
}
