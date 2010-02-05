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

import geogebra.kernel.GeoAngle
import geogebra.plugin.GgbAPI

object Angle {
  def apply(ggbApi: GgbAPI, label: String, p1: Point, p2: Point, p3: Point) = {
    new Angle(ggbApi, ggbApi.getKernel.Angle(label, p1.gPoint, p2.gPoint, p3.gPoint))
  }
}

class Angle(ggbApi: GgbAPI, val gAngle: GeoAngle) extends AbstractShape(ggbApi) with net.kogics.kojo.core.Angle {

  showNameValueInLabel()
  ctorDone()

  protected def geogebraElement = gAngle
}
