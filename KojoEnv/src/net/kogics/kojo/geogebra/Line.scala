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

package net.kogics.kojo.geogebra

import geogebra.kernel.GeoLine
import geogebra.plugin.GgbAPI

object Line {
  def apply(ggbApi: GgbAPI, label: String, p1: Point, p2: Point) = {
    val gLine = ggbApi.getKernel.Line("a", p1.gPoint, p2.gPoint)
    new Line(ggbApi, gLine, p1, p2)
  }
}

class Line(ggbApi: GgbAPI, val gLine: GeoLine, val p1: Point, val p2: Point) extends AbstractShape(ggbApi) with net.kogics.kojo.core.Line {

  ctorDone()

  protected def geogebraElement = gLine
}
