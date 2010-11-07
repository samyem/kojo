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
package mathworld

import geogebra.plugin.GgbAPI
import net.kogics.kojo.core._
import net.kogics.kojo.util._

object MathWorld extends InitedSingleton[MathWorld] {
  def initedInstance(kojoCtx: KojoCtx, ggbApi: GgbAPI) = synchronized {
    instanceInit()
    val ret = instance()
    ret.kojoCtx = kojoCtx
    ret.ggbApi = ggbApi
    ret
  }

  protected def newInstance = new MathWorld
}

class MathWorld {
//  type GPoint = MwPoint
//  type GLine = MwLine
//  type GSegment = MwLineSegment
//  type GAngle = MwAngle
//  type GText = MwText
//  type GCircle = MwCircle

  @volatile var kojoCtx: KojoCtx = _
  @volatile var ggbApi: GgbAPI = _

  def ensureVisible() {
    kojoCtx.makeMathWorldVisible()
  }

  def clear() {
    Utils.runInSwingThread {
      ensureVisible()
      ggbApi.getApplication.setSaved()
      ggbApi.getApplication.fileNew()
    }
  }

  def showAxes() {
    Utils.runInSwingThread {
      ggbApi.setAxesVisible(true, true)
      ggbApi.getKernel.notifyRepaint()
    }
  }

  def hideAxes() {
    Utils.runInSwingThread {
      ggbApi.setAxesVisible(false, false)
      ggbApi.getKernel.notifyRepaint()
    }
  }

  def point(x: Double, y: Double) = MwPoint(ggbApi, x, y)
  def point(on: MwLine, x: Double, y: Double) = MwPoint(ggbApi, on, x, y)

  def line(p1: MwPoint, p2: MwPoint) = MwLine(ggbApi, p1, p2)

  def lineSegment(p1: MwPoint, p2: MwPoint) = MwLineSegment(ggbApi, p1, p2)
  def lineSegment(p: MwPoint, len: Double) = MwLineSegment(ggbApi, p, len)

  def intersect(l1: MwLine, l2: MwLine) = MwPoint(ggbApi, l1, l2)

  def intersect(l: MwLine, c: MwCircle) = MwPoint(ggbApi, l, c)

  def angle(p1: MwPoint, p2: MwPoint, p3: MwPoint) = MwAngle(ggbApi, p1, p2, p3)

  def angle(p1: MwPoint, p2: MwPoint, size: Double) = MwAngle(ggbApi, p1, p2, size * math.Pi / 180)
  
  def text(content: String, x: Double, y: Double): MwText = {
    MwText(ggbApi, content, x, y)
  }

  def circle(center: MwPoint, radius: Double): MwCircle = {
    MwCircle(ggbApi, center, radius)
  }

  def figure(name: String) = new MwFigure(name)

  // quick and dirty stuff for now
  import geogebra.kernel._

  def variable(name: String, value: Double, min: Double, max: Double, increment: Double, x: Int, y: Int) {
    Throttler.throttle()
    Utils. runInSwingThread {
      val number = new GeoNumeric(ggbApi.getConstruction)
      number.setEuclidianVisible(true)
      number.setSliderLocation(x, y)
      number.setAbsoluteScreenLocActive(true)
      number.setIntervalMin(min)
      number.setIntervalMax(max)
      number.setAnimationStep(increment)
      number.setValue(value)
      number.setLabel(name)
      number.setLabelMode(GeoElement.LABEL_NAME_VALUE)
      number.setLabelVisible(true)
      number.update()
    }
  }

  def evaluate(cmd: String) {
    Throttler.throttle()
    Utils. runInSwingThread {
      ggbApi.evalCommand(cmd)
    }
  }
}
