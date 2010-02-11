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

import core.Point

class FigLine(val canvas: PCanvas, p1: Point, p2: Point) extends core.Line(p1, p2) with FigShape {
  val pLine = new kgeom.PolyLine()
  pLine.addPoint(p1.x.toFloat, p1.y.toFloat)
  pLine.addPoint(p2.x.toFloat, p2.y.toFloat)

  protected val piccoloNode = pLine
}
