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

import net.kogics.kojo.util._
import net.kogics.kojo.core.geom._

trait VarsListener {
  def varsChanged(vars: mutable.Map[String, Float])
}

abstract class BasePolygonConstraint(shape: PolyLine, handleLayer: PLayer, outputFn: String=>Unit) extends GeometricConstraint {

  val points = shape.points
  shape.constraint = Some(this)

  val handles = new mutable.ArrayBuffer[PHandle]
  val angles = new mutable.ArrayBuffer[PText]
  val lengths = new mutable.ArrayBuffer[PText]
  val vars = new mutable.HashMap[String, Float]

  val labelFont = new Font(new PText().getFont.getName, Font.PLAIN, 15)

  var varsListener: Option[VarsListener] = None

  init(shape)

  shape.removeLastPoint()
  shape.close

  addAngles()
  addLengths()

  def init(shape: PolyLine)

  def setVarsListener(l: VarsListener) {
    varsListener = Some(l)
  }

  def repaint() = shape.repaint()

  def pointsSame(p1: Point2D.Float, p2: Point2D.Float): Boolean = {
    Utils.doublesEqual(p1.x, p2.x, 0.001) && Utils.doublesEqual(p1.y, p2.y, 0.001)
  }

  def clearVisualElements() {
    Utils.runInSwingThread {
      angles.clear
      lengths.clear
      handles.foreach {h => handleLayer.removeChild(h)}
    }
  }

  def addLengths() {
    points.foreach {p =>
      val length = new PText()
      length.setVisible(false)
      length.addAttribute("label", "L" + LabelCounters.nextLengthCounter)
      length.setFont(labelFont)
      length.getTransformReference(true).setToScale(1, -1)
      shape.addChild(length)
      lengths += length
    }
  }

  def addAngles() {
    points.foreach {p =>
      val angle = new PText()
      angle.setVisible(false)
      angle.addAttribute("label", "A" + LabelCounters.nextAngleCounter)
      angle.setFont(labelFont)
      angle.getTransformReference(true).setToScale(1, -1)
      shape.addChild(angle)
      angles += angle
    }
  }

  def showLengths() {
    Utils.runInSwingThread {
      lengths.foreach {l => l.setVisible(true)}
      updateLengths()
      shape.updateBounds()
    }
  }

  def hideLengths() {
    Utils.runInSwingThread {
      lengths.foreach {l => l.setVisible(false)}
      shape.updateBounds()
    }
  }

  def showAngles() {
    Utils.runInSwingThread {
      angles.foreach {a => a.setVisible(true)}
      updateAngles()
      shape.updateBounds()
    }
  }

  def hideAngles() {
    Utils.runInSwingThread {
      angles.foreach {a => a.setVisible(false)}
      shape.updateBounds()
    }
  }

  def updateLengths() {
    def nextPoint(i: Int) = {
      if (i == points.size-1) points(0)
      else points(i+1)
    }

    for (i <- 0 until lengths.size) {
      updateLength(points(i), nextPoint(i), lengths(i))
    }
  }

  def updateLength(p0: Point2D.Float, p1: Point2D.Float, length: PText) {
    if (!length.getVisible) return
    
    val len = Vector2(p1.x-p0.x, p1.y-p0.y).length
    val label = length.getAttribute("label").asInstanceOf[String]
    vars(label) = len.toFloat
    length.setText("%s=%.1f" format(label, len))
    length.setOffset((p0.x+p1.x)/2, (p0.y+p1.y)/2)
    if (varsListener.isDefined) varsListener.get.varsChanged(vars)
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
    if (!angle.getVisible) return
    
    val vec1 = new Vector2(p1.x-p0.x, p1.y-p0.y)
    val vec2 = new Vector2(p2.x-p0.x, p2.y-p0.y)
    val dotp = vec1.dot(vec2)
    val l1 = vec1.length
    val l2 = vec2.length

    val theta = Math.acos(dotp/(l1*l2)) * 180 / Math.Pi
    val label = angle.getAttribute("label").asInstanceOf[String]
    vars(label) = theta.toFloat
    angle.setText("%s=%.0f" format(label, theta))
    angle.setOffset(p0.x+3, p0.y-3)
    if (varsListener.isDefined) varsListener.get.varsChanged(vars)
  }

  def trackVars(fn: mutable.Map[String, Float] => Unit) {
    setVarsListener(new VarsListener {
        def varsChanged(vars: mutable.Map[String, Float]) {
          try {
            fn(vars)
          }
          catch {
            case e: java.util.NoSuchElementException => outputFn("Problem: " + e.getMessage())
          }
        }
      })
  }
}
