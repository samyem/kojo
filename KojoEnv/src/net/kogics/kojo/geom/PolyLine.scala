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

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.util._

import scala.collection._

class PolyLine extends PNode {

  val polyLinePath = new GeneralPath()

  val points = new mutable.ArrayBuffer[Point2D.Float]
  var stroke: Stroke = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
  var strokePaint = Color.blue

  var closed = false
  var constraint: Option[PolygonConstraint] = None

  def addPoint(x: Float, y: Float): Unit = addPoint(new Point2D.Float(x, y))
  def lineTo(x: Float, y: Float) = addPoint(new Point2D.Float(x, y))

  def reset() {
    points.clear()
    if (constraint.isDefined) constraint.get.reset()
  }

  def setStroke(strk: Stroke) {
    stroke = strk
  }

  def setStrokePaint(c: Color) {
    strokePaint = c
  }

  def addPoint(p: Point2D.Float): Unit = {
    points += p
    updateBounds()
  }

  def close() {
    closed = true
  }

  override def paint(paintContext: PPaintContext) {
    val g2 = paintContext.getGraphics()
    val path = getCurrentPolyLinePath()
    val fillPaint = getPaint()

    if (fillPaint != null) {
      g2.setPaint(fillPaint)
      g2.fill(path)
    }

    g2.setStroke(stroke)
    g2.setPaint(strokePaint)
    g2.draw(path)
  }

  def updateBounds() {
    val p = getCurrentPolyLinePath()
    val b = stroke.createStrokedShape(p).getBounds2D()
    super.setBounds(b.getX(), b.getY(), b.getWidth(), b.getHeight())
    repaint()
  }

  def getCurrentPolyLinePath(): GeneralPath = {
    polyLinePath.reset()
    if (points.size == 1) return polyLinePath

    val piter = points.iterator
    if (piter.hasNext) {
      var point = piter.next
      val point0 = point
      polyLinePath.moveTo(point.x, point.y)
      while (piter.hasNext) {
        point = piter.next
        polyLinePath.lineTo(point.x, point.y)
      }
      if (closed) polyLinePath.lineTo(point0.x, point0.y)
    }
    polyLinePath
  }

  override def setBounds(x: Double, y: Double, width: Double, height: Double): Boolean = {
    println("Cannot set bounds")
    false
  }
}
