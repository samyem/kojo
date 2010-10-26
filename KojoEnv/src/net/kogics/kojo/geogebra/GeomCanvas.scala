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

import geogebra.plugin.GgbAPI
import net.kogics.kojo.core._
import net.kogics.kojo.util.Utils


class GeomCanvas(ggbApi: GgbAPI) extends net.kogics.kojo.core.GeomCanvas {
  type GPoint = MwPoint
  type GLine = MwLine
  type GSegment = MwLineSegment
  type GAngle = MwAngle
  type GText = MwText

  @volatile var kojoCtx: KojoCtx = _

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

  def point(label: String, x: Double, y: Double) = MwPoint(ggbApi, label, x, y)
  def point(label: String, on: MwLine, x: Double, y: Double) = MwPoint(ggbApi, label, on, x, y)

  def line(label: String, p1: MwPoint, p2: MwPoint) = MwLine(ggbApi, label, p1, p2)

  def lineSegment(label: String, p1: MwPoint, p2: MwPoint) = MwLineSegment(ggbApi, label, p1, p2)

  def intersect(label: String, l1: MwLine, l2: MwLine): MwPoint = {
    MwPoint(ggbApi, label, l1, l2)
  }

  def angle(label: String, p1: MwPoint, p2: MwPoint, p3: MwPoint): MwAngle = {
    MwAngle(ggbApi, label, p1, p2, p3)
  }

  def text(content: String, x: Double, y: Double): MwText = {
    val txt = MwText(ggbApi, content, x, y)
    txt
  }

  // quick and dirty stuff for now
  import geogebra.kernel._

  def variable(name: String, value: Double, min: Double, max: Double, increment: Double, x: Int, y: Int) {
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
    Utils. runInSwingThread {
      ggbApi.evalCommand(cmd)
    }
  }
}
