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

import util.Utils

class MwTurtle(x: Double, y: Double) {
  import turtle.TurtleHelper._
  import java.awt.Color
  val Mw = MathWorld.instance()
  import Mw._

  val MarkerSize = 0.2
  private var headingMarker: MwPoint = _
  private var position: MwPoint = _
  private var theta: Double = _
  private var penColor: Color = _
  private var penThickness: Int = _
  private var penIsDown: Boolean = _
  private var angleShow: Boolean = _
  private var externalAngleShow: Boolean = _
  private var polyPoints: List[MwPoint] = _

  private var lines: List[MwLineSegment] = _
  private var angles: List[MwAngle] = _

  init()

  def lastLine: Option[MwLineSegment] = lines match {
    case Nil => None
    case _ => Some(lines.head)
  }

  def init() {
    penColor = Color.red
    penThickness = 2
    penIsDown = true
    angleShow = false
    externalAngleShow = false
    polyPoints = Nil
    headingMarker = point(0, 0)
    headingMarker.setColor(Color.orange)
    headingMarker.show()
    setPos(point(x, y))
    setHeading(90)
    lines = Nil
    angles = Nil
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
        angles = a :: angles
      }

      if (externalAngleShow && lastLine.isDefined) {
        val a = angle(position, p0, lastLine.get.p1)
        a.showValueInLabel()
        a.show()
        angles = a :: angles
      }

      if (polyPoints != Nil) {
        polyPoints = position :: polyPoints
      }

      lines = ls :: lines
    }
  }

  def forwardXy(p0: MwPoint, theta: Double, n: Double) = {
    val delX = math.cos(theta) * n
    val delY = math.sin(theta) * n
    (p0.x + delX, p0.y + delY)
  }

  def forward(n: Double) {
    Utils.runInSwingThread {
      val p0 = position
      val xy = forwardXy(p0, theta, n)
      forwardLine(p0, point(xy._1, xy._2))
    }
  }

  def turn(angle: Double) {
    Utils.runInSwingThread {
      setRotation(thetaAfterTurn(angle, theta))
    }
  }

  private def updateHMarker() {
    val xy = forwardXy(position, theta, MarkerSize)
    headingMarker.moveTo(xy._1, xy._2)
  }

  // should be called on swing thread
  private def setPos(p: MwPoint) {
    position = p
    if (penIsDown) {
      position.setColor(Color.green)
      position.show()
      updateHMarker()
    }
  }

  def setPosition(x: Double, y: Double) {
    setPosition(point(x, y))
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
    updateHMarker()
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
            angles = a2 :: angles
          }

          if (externalAngleShow) {
            val a2 = angle(pp(1), position, p0)
            a2.showValueInLabel()
            a2.show()
            angles = a2 :: angles
          }
        }
      }
      polyPoints = Nil
    }
  }

  def findLine(label: String) = {
    Utils.runInSwingThreadAndWait {
      lines.find {l => l.p1.label + l.p2.label == label} match {
        case Some(l) => l
        case None => throw new RuntimeException("Unknown line: " + label)
      }
    }
  }

  def findAngle(label: String) = {
    Utils.runInSwingThreadAndWait {
      angles.find {a => a.p1.label + a.p2.label + a.p3.label == label} match {
        case Some(a) => a
        case None => throw new RuntimeException("Unknown angle: " + label)
      }
    }
  }
}
