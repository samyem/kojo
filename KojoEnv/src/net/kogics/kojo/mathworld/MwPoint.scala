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

package net.kogics.kojo.mathworld

import geogebra.kernel._
import geogebra.plugin.GgbAPI
import net.kogics.kojo.util.Utils

import net.kogics.kojo.core._

object MwPoint {

  val lGen = new LabelGenerator("Pt")

  def apply(ggbApi: GgbAPI, x: Double, y: Double): MwPoint = {
    net.kogics.kojo.util.Throttler.throttle()
    val pt = Utils.runInSwingThreadAndWait {
      new MwPoint(ggbApi, ggbApi.getKernel.Point(lGen.next(), x, y))
    }
    pt
  }

  def apply(ggbApi: GgbAPI, l1: MwLine, l2: MwLine) = {
    val pt = Utils.runInSwingThreadAndWait {
      val gPoint = ggbApi.getKernel.IntersectLines(lGen.next(), l1.gLine, l2.gLine)
      new MwPoint(ggbApi, gPoint)
    }
    pt
  }

  def apply(ggbApi: GgbAPI, l: MwLine, c: MwCircle) = {
    val pts = Utils.runInSwingThreadAndWait {
      val gPoints = ggbApi.getKernel.IntersectLineConic(Array(lGen.next(), lGen.next()), l.gLine, c.gCircle)
      gPoints.map {gPoint => new MwPoint(ggbApi, gPoint)}
    }
    pts
  }

  def apply(ggbApi: GgbAPI, c1: MwCircle, c2: MwCircle) = {
    val pts = Utils.runInSwingThreadAndWait {
      val labels = for (idx <- 1 to 10) yield (lGen.next())
      val gPoints = ggbApi.getKernel.IntersectConics(labels.toArray, c1.gCircle, c2.gCircle)
      gPoints.map {gPoint => new MwPoint(ggbApi, gPoint)}
    }
    pts
  }

  def apply(ggbApi: GgbAPI, on: MwLine, x: Double, y: Double) = {
    val pt = Utils.runInSwingThreadAndWait {
      new MwPoint(ggbApi, ggbApi.getKernel.Point(lGen.next(), on.gLine, x, y))
    }
    pt
  }
}

class MwPoint(val ggbApi: GgbAPI, val gPoint: GeoPoint) extends Point(gPoint.x, gPoint.y) with MwShape with MoveablePoint {

  ctorDone()
  
  def cx = gPoint.x
  def cy = gPoint.y

  def moveTo(x: Double, y: Double) {
    Utils.runInSwingThread {
      gPoint.setCoords(x, y, 1)
      repaint()
    }
  }

  protected def geogebraElement = gPoint
}
