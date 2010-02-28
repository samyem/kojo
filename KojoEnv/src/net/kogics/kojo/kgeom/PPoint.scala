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
package net.kogics.kojo.kgeom

import java.awt._
import java.awt.geom._

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.util._

import scala.collection._

class PPoint(x: Double, y: Double) extends PNode {

  var stroke: Stroke = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
  var strokePaint = Color.blue
  val shape = new Line2D.Double(x, y, x, y)
  updateBounds()
  
  def setStroke(strk: Stroke) {
    stroke = strk
  }

  def setStrokePaint(c: Color) {
    strokePaint = c
  }

  override def paint(paintContext: PPaintContext) {
    val g2 = paintContext.getGraphics()
    g2.setStroke(stroke)
    g2.setPaint(strokePaint)
    g2.draw(shape)
  }

  def updateBounds() {
    val b = stroke.createStrokedShape(shape).getBounds2D()
    super.setBounds(b.getX(), b.getY(), b.getWidth(), b.getHeight())
    repaint()
  }

  override def setBounds(x: Double, y: Double, width: Double, height: Double): Boolean = {
    println("Cannot set bounds")
    false
  }
}
