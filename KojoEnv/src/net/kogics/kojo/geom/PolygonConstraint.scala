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
package net.kogics.kojo.geom

import java.awt._
import java.awt.geom._

import scala.collection._
import org.villane.vecmath._

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.util._
import edu.umd.cs.piccolo.event._
import edu.umd.cs.piccolo.nodes._
import edu.umd.cs.piccolox.handles._
import edu.umd.cs.piccolox.util._

import net.kogics.kojo.core.geom._
import net.kogics.kojo.util._

class PolygonConstraint(shape: PolyLine, handleLayer: PLayer) extends PolygonView {

  val points = shape.points
  shape.constraint = Some(this)

  val angles = new mutable.ArrayBuffer[PText]
  val handles = new mutable.ArrayBuffer[PHandle]

  if (!pointsSame(points(0), points.last))
    throw new IllegalArgumentException("Unable to convert Path to Polygon - not a closed shape")

  points.remove(points.size-1)
  shape.close

  val angleFont = new Font(new PText().getFont.getName, Font.BOLD, 15)

  def addAngles() {
    points.foreach {p =>
      val angle = new PText()
      angle.setFont(angleFont)
      angle.getTransformReference(true).setToScale(1, -1)
      shape.addChild(angle)
      angles += angle
    }
  }

  def pointsSame(p1: Point2D.Float, p2: Point2D.Float): Boolean = {
    Utils.doublesEqual(p1.x, p2.x, 0.001) && Utils.doublesEqual(p1.y, p2.y, 0.001)
  }

  def reset() {
    angles.clear
    handles.foreach {h => handleLayer.removeChild(h)}
  }

  def showAngles() {
    addAngles()
    updateAngles()
    shape.updateBounds()
  }

  def updateAngles() {

    def nextPoint(i: Int) = {
      if (i == points.size-1) points(0)
      else points(i+1)
    }

    def prevPoint(i: Int) = {
      if (i == 0) points(points.size-1)
      else points(i-1)
    }

    for (i <- 0 until angles.size) {
      updateAngle(points(i), prevPoint(i), nextPoint(i), angles(i))
    }
  }

  def updateAngle(p0: Point2D.Float, p1: Point2D.Float, p2: Point2D.Float, angle: PText) {
    val vec1 = new Vector2(p1.x-p0.x, p1.y-p0.y)
    val vec2 = new Vector2(p2.x-p0.x, p2.y-p0.y)
    val dotp = vec1.dot(vec2)
    val l1 = vec1.length
    val l2 = vec2.length

    val theta = Math.acos(dotp/(l1*l2)) * 180 / Math.Pi
    angle.setText("%.0f" format(theta))
    angle.setOffset(p0.x+3, p0.y-3)
//    println("------------")
//    println("Point0: " + p0)
//    println("Point1: " + p1)
//    println("Point2: " + p2)
//    println("Theta1: " + vec1.theta * 180 / Math.Pi)
//    println("Theta2: " + vec2.theta * 180 / Math.Pi)
//    println("Cross Prod: " + vec1.cross(vec2))
  }

  def addHandles() {
    def linkedPoint(i: Int) = {
      if (i == 0) points(points.size-1)
      else points(i-1)
    }

    for (i <- 0 until points.size) {
      addHandle(points(i), linkedPoint(i))
    }

    handleLayer.repaint()
  }

  def addHandle(point: Point2D.Float, linkedPoint: Point2D.Float) {
    val l = new PLocator() {
      override def locateX() = point.x
      override def locateY() = point.y
    }

    val h = new PHandle(l) {
      override def dragHandle(aLocalDimension: PDimension, aEvent: PInputEvent) {
        localToParent(aLocalDimension)
        point.setLocation(point.x + aLocalDimension.getWidth(), point.y
                          + aLocalDimension.getHeight())

//        linkedPoint.setLocation(linkedPoint.x + aLocalDimension.getWidth(), linkedPoint.y
//                                + aLocalDimension.getHeight())

        relocateHandle()
        updateAngles()
        shape.updateBounds()
      }
    }

    handleLayer.addChild(h)
    handles += h
  }

  def repaint() = shape.repaint()

}
