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
  import turtle.TurtleHelper._

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
      init()
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

  // turtle like stuff
  import java.awt.Color

  private var position: MwPoint = _
  private var theta: Double = _
  private var penColor: Color = _
  private var penThickness: Int = _
  private var penIsDown: Boolean = _
  private var angleShow: Boolean = _
  private var externalAngleShow: Boolean = _
  private var lastLine: Option[MwLineSegment] = _
  private var polyPoints: List[MwPoint] = _

  def init() {
    penColor = Color.red
    penThickness = 2
    penIsDown = true
    angleShow = false
    externalAngleShow = false
    lastLine = None
    polyPoints = Nil
    setPos(point(0, 0))
    setHeading(90)
  }

  private def forwardLine(p0: MwPoint, p1: MwPoint) {
    setPosition(p1)
    if (penIsDown) {
      val ls = lineSegment(p0, position)
      ls.setColor(penColor)
      ls.setLineThickness(penThickness)
      ls.show()

      if (angleShow && lastLine.isDefined) {
        val a = angle(lastLine.get.p1, p0, position)
        a.showValueInLabel()
        a.show()
      }

      if (externalAngleShow && lastLine.isDefined) {
        val a = angle(position, p0, lastLine.get.p1)
        a.showValueInLabel()
        a.show()
      }

      if (polyPoints != Nil) {
        polyPoints = position :: polyPoints
      }

      lastLine = Some(ls)
    }
  }

  def forward(n: Double) {
    Utils.runInSwingThread {
      val p0 = position
      val delX = math.cos(theta) * n
      val delY = math.sin(theta) * n
      val p1 = point(position.x + delX, position.y + delY)
      forwardLine(p0, p1)
    }
  }

  def turn(angle: Double) {
    Utils.runInSwingThread {
      theta = thetaAfterTurn(angle, theta)
    }
  }

  // should be called on swing thread
  private def setPos(p: MwPoint) {
    position = p
    if (penIsDown) {
      position.setColor(Color.green)
      position.show()
    }
  }

  def setPosition(p: MwPoint) {
    Utils.runInSwingThread {
      position.setColor(Color.blue)
      setPos(p)
    }
  }

  def setHeading(angle: Double) {
    Utils.runInSwingThread {
      setRotation(Utils.deg2radians(angle))
    }
  }

  private def setRotation(angle: Double) {
    theta = angle
  }

  def setPenColor(color: Color) {
    Utils.runInSwingThread {
      penColor = color
    }
  }

  def setPenThickness(t: Int) {
    Utils.runInSwingThread {
      penThickness = t
    }
  }

  def moveTo(x: Double, y: Double) {
    Utils.runInSwingThread {
      setRotation(thetaTowards(position.x, position.y, x, y, theta))
      forward(distance(position.x, position.y, x, y))
    }
  }

  def penUp() {
    Utils.runInSwingThread {
      penIsDown = false
    }
  }

  def penDown() {
    Utils.runInSwingThread {
      penIsDown = true
      position.show()
    }
  }

  def setLabel(l: String) {
    Utils.runInSwingThread {
      position.setLabel(l)
    }
  }

  def showAngles() {
    Utils.runInSwingThread {
      angleShow = true
    }
  }

  def hideAngles() {
    Utils.runInSwingThread {
      angleShow = false
    }
  }

  def showExternalAngles() {
    Utils.runInSwingThread {
      externalAngleShow = true
    }
  }

  def hideExternalAngles() {
    Utils.runInSwingThread {
      externalAngleShow = false
    }
  }

  def left(angle: Double) = turn(angle)
  def right(angle: Double) = turn(-angle)
  def left(): Unit = left(90)
  def right(): Unit = right(90)

  def back(n: Double) = forward(-n)

  def beginPoly() {
    Utils.runInSwingThread {
      polyPoints = List(position)
    }
  }

  def endPoly() {
    Utils.runInSwingThread {
      if (polyPoints.size > 2) {
        val pp = polyPoints.reverse
        setRotation(thetaTowards(position.x, position.y, pp(0).x, pp(0).y, theta))
        val p0 = position
        forwardLine(p0, pp(0))

        // make angles at first vertex of poly
        if (penIsDown) {
          if (angleShow) {
            val a2 = angle(p0, position, pp(1))
            a2.showValueInLabel()
            a2.show()
          }

          if (externalAngleShow) {
            val a2 = angle(pp(1), position, p0)
            a2.showValueInLabel()
            a2.show()
          }
        }
      }
      polyPoints = Nil
    }
  }
}
