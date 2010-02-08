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

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.nodes._

import core.Point

class FigRectangle(bLeft: Point, tRight: Point) extends core.Rectangle(bLeft, tRight)  with FigureShape {
  val pRect = PPath.createRectangle(bLeft.x.toFloat, bLeft.y.toFloat, width.toFloat, height.toFloat)

  protected def piccoloNode = pRect
}
