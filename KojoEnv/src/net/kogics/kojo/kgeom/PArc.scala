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

class PArc(cx: Double, cy: Double, w: Double, h: Double, start: Double, extent: Double) extends PNode {

  var stroke: Stroke = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
  var strokePaint = Color.blue
  val arc = new Arc2D.Float((cx-w/2).toFloat, (cy-h/2).toFloat, w.toFloat, h.toFloat, start.toFloat, extent.toFloat, Arc2D.PIE)
  updateBounds()
  
  def setStroke(strk: Stroke) {
    stroke = strk
  }

  def setStrokePaint(c: Color) {
    strokePaint = c
  }

  override def paint(paintContext: PPaintContext) {
    val g2 = paintContext.getGraphics()
    val fillPaint = getPaint()

    if (fillPaint != null) {
      g2.setPaint(fillPaint)
      g2.fill(arc)
    }

    g2.setStroke(stroke)
    g2.setPaint(strokePaint)
    g2.draw(arc)
  }

  def updateBounds() {
    val b = stroke.createStrokedShape(arc).getBounds2D()
    super.setBounds(b.getX(), b.getY(), b.getWidth(), b.getHeight())
    repaint()
  }

  override def setBounds(x: Double, y: Double, width: Double, height: Double): Boolean = {
    println("Cannot set bounds")
    false
  }
}
