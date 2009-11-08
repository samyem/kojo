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

import org.villane.vecmath._

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.util._
import edu.umd.cs.piccolo.event._
import edu.umd.cs.piccolo.nodes._
import edu.umd.cs.piccolox.handles._
import edu.umd.cs.piccolox.util._

import net.kogics.kojo.util._
import net.kogics.kojo.core.geom._

class PolygonConstraint(shape: PolyLine, handleLayer: PLayer, outputFn: String=>Unit) extends BasePolygonConstraint(shape, handleLayer, outputFn) {

  def init(shape: PolyLine) {
    if (!pointsSame(points(0), points.last))
      throw new IllegalArgumentException("Unable to convert Path to Polygon - not a closed shape\n")
  }
  
  def addHandles() {
    for (i <- 0 until points.size) {
      addHandle(points(i))
    }

    handleLayer.repaint()
  }

  def addHandle(point: Point2D.Float) {
    val l = new PLocator() {
      override def locateX() = point.x
      override def locateY() = point.y
    }

    val h = new PHandle(l) {
      override def dragHandle(aLocalDimension: PDimension, aEvent: PInputEvent) {
        localToParent(aLocalDimension)
        point.setLocation(point.x + aLocalDimension.getWidth(), point.y
                          + aLocalDimension.getHeight())

        relocateHandle()
        this.repaint()
        updateAngles()
        updateLengths()
        shape.updateBounds()
      }
    }

    handleLayer.addChild(h)
    handles += h
  }
}
