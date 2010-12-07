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
    ret.Algo = new Algo(ggbApi)
    ret
  }

  protected def newInstance = new MathWorld
}

class MathWorld {
  @volatile var kojoCtx: KojoCtx = _
  @volatile var ggbApi: GgbAPI = _
  @volatile var Algo: Algo = _

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

  def showGrid() {
    Utils.runInSwingThread {
      ggbApi.setGridVisible(true)
      ggbApi.getKernel.notifyRepaint()
    }
  }

  def hideGrid() {
    Utils.runInSwingThread {
      ggbApi.setGridVisible(false)
      ggbApi.getKernel.notifyRepaint()
    }
  }

  def showAlgebraView() {
    Utils.runInSwingThread {
      ggbApi.getApplication.setShowAlgebraView(true)
      ggbApi.getApplication.updateCenterPanel(true)
      ggbApi.getApplication.setDefaultCursor()
    }
  }

  def hideAlgebraView() {
    Utils.runInSwingThread {
      ggbApi.getApplication.setShowAlgebraView(false)
      ggbApi.getApplication.updateCenterPanel(true)
      ggbApi.getApplication.setDefaultCursor()
    }
  }

  def point(x: Double, y: Double, label: String=null): MwPoint = MwPoint(ggbApi, x, y, Option(label))
  def point(on: MwLine, x: Double, y: Double): MwPoint = MwPoint(ggbApi, on, x, y)

  def line(p1: MwPoint, p2: MwPoint): MwLine = MwLine(ggbApi, p1, p2)

  def lineSegment(p1: MwPoint, p2: MwPoint): MwLineSegment = MwLineSegment(ggbApi, p1, p2)
  def lineSegment(p: MwPoint, len: Double): MwLineSegment = MwLineSegment(ggbApi, p, len)

  def ray(p1: MwPoint, p2: MwPoint): MwRay = MwRay(ggbApi, p1, p2)

  def angle(p1: MwPoint, p2: MwPoint, p3: MwPoint): MwAngle = MwAngle(ggbApi, p1, p2, p3)
  def angle(p1: MwPoint, p2: MwPoint, size: Double): MwAngle = MwAngle(ggbApi, p1, p2, size * math.Pi / 180)
  
  def text(content: String, x: Double, y: Double): MwText = {
    MwText(ggbApi, content, x, y)
  }

  def circle(center: MwPoint, radius: Double): MwCircle = {
    MwCircle(ggbApi, center, radius)
  }

  def figure(name: String) = new MwFigure(name)

  def intersect(l1: MwLine, l2: MwLine): MwPoint  = Algo.intersect(ggbApi, l1, l2)
  def intersect(l: MwLine, c: MwCircle): Seq[MwPoint] = Algo.intersect(ggbApi, l, c)
  def intersect(c: MwCircle, l: MwLine): Seq[MwPoint] = intersect(l, c)
  def intersect(c1: MwCircle, c2: MwCircle): Seq[MwPoint] = Algo.intersect(ggbApi, c1, c2)

  def midpoint(ls: MwLineSegment): MwPoint = Algo.midpoint(ls)
  def perpendicular(l: MwLine, p: MwPoint): MwLine = Algo.perpendicular(l, p)
  def parallel(l: MwLine, p: MwPoint): MwLine = Algo.parallel(l, p)

  def show(shapes: VisualElement*) {
    Utils.runInSwingThread {
      shapes.foreach {s => s.show}
    }
  }
  
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

  def turtle(x: Double, y: Double) = {
    Utils.runInSwingThreadAndWait {
      new MwTurtle(x, y)
    }
  }
}
