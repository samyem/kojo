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

package net.kogics.kojo.geogebra

import geogebra.kernel._
import geogebra.plugin.GgbAPI
import net.kogics.kojo.util.Utils

object Point {

  def apply(ggbApi: GgbAPI, label: String, x: Double, y: Double) = {
    net.kogics.kojo.util.Throttler.throttle()
    val pt = Utils.runInSwingThreadAndWait {
      new Point(ggbApi, ggbApi.getKernel.Point(label, x, y))
    }
    pt
  }

  def apply(ggbApi: GgbAPI, label: String, l1: Line, l2: Line) = {
    val pt = Utils.runInSwingThreadAndWait {
      val gPoint = ggbApi.getKernel.IntersectLines(label, l1.gLine, l2.gLine)
      new Point(ggbApi, gPoint)
    }
    pt
  }

  def apply(ggbApi: GgbAPI, label: String, on: Line, x: Double, y: Double) = {
    val pt = Utils.runInSwingThreadAndWait {
      new Point(ggbApi, ggbApi.getKernel.Point(label, on.gLine, x, y))
    }
    pt
  }
}

class Point(ggbApi: GgbAPI, val gPoint: GeoPoint) extends AbstractShape(ggbApi) with net.kogics.kojo.core.Point {

  val x = gPoint.x
  val y = gPoint.y

  ctorDone()
  
  override def cx = gPoint.x
  override def cy = gPoint.y

  def moveTo(x: Double, y: Double) {
    Utils.runInSwingThread {
      gPoint.setCoords(x, y, 1)
      repaint()
    }
  }

  protected def geogebraElement = gPoint
}
