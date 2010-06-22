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

class FigEllipse(val canvas: PCanvas, center: Point, w: Double, h: Double) extends core.Ellipse(center, w, h)  with FigShape {
  val pEllipse = PPath.createEllipse((center.x-w/2).toFloat, (center.y-h/2).toFloat, w.toFloat, h.toFloat)

  protected val piccoloNode = pEllipse
}
