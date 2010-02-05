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

import geogebra.kernel.GeoSegment
import geogebra.plugin.GgbAPI
import net.kogics.kojo.util.Utils

object LineSegment {
  def apply(ggbApi: GgbAPI, label: String, p1: Point, p2: Point) = {
    net.kogics.kojo.util.Throttler.throttle()
    val lineSegment = Utils.runInSwingThreadAndWait {
      val gLineSegment = ggbApi.getKernel.Segment(label, p1.gPoint, p2.gPoint)
      new LineSegment(ggbApi, gLineSegment, p1, p2)
    }
    lineSegment
  }
}

class LineSegment(ggbApi: GgbAPI, gLineSegment: GeoSegment, _p1: Point, _p2: Point)
extends Line(ggbApi, gLineSegment, _p1, _p2) with net.kogics.kojo.core.LineSegment {

//  ctorDone()

  override protected def geogebraElement = gLineSegment
}
