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

package net.kogics.kojo
package figure

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.nodes._
import edu.umd.cs.piccolo.util._
import edu.umd.cs.piccolo.event._
import edu.umd.cs.piccolo.activities.PActivity
import edu.umd.cs.piccolo.activities.PActivity.PActivityDelegate

import javax.swing._
import java.awt._

import core.SpriteListener
import core.NoopSpriteListener

class Figure(canvas: SpriteCanvas, initX: Double = 0d, initY: Double = 0) extends core.Figure {
  private val layer = new PLayer
  private val animLayer = new PLayer
  private var currLayer = layer
  private val camera = canvas.getCamera
  val DefaultColor = Color.red
  val DefaultFillColor: Color = null
  val DefaultStroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
  @volatile private var listener: SpriteListener = NoopSpriteListener
  @volatile var stopAnim: Boolean = _

  var lineColor: Color = _
  var fillColor: Color = _
  var lineStroke: Stroke = _

  camera.addLayer(camera.getLayerCount-1, layer)
  camera.addLayer(camera.getLayerCount-1, animLayer)
  init()
  
  def init() {
    layer.setOffset(initX, initY)
    animLayer.setOffset(initX, initY)
    lineColor = DefaultColor
    fillColor = DefaultFillColor
    lineStroke = DefaultStroke
    stopAnim = false
  }

  def clear {
    layer.removeAllChildren()
    animLayer.removeAllChildren()
    init()
    layer.repaint()
    animLayer.repaint()
  }

  def aclear {
    animLayer.removeAllChildren()
    animLayer.repaint()
  }

  def remove() {
    camera.removeLayer(layer)
    camera.removeLayer(animLayer)
  }

  def setPenColor(color: java.awt.Color) {
    lineColor = color
  }

  def setPenThickness(t: Double) {
    lineStroke = new BasicStroke(t.toFloat, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
  }

  def setFillColor(color: java.awt.Color) {
    fillColor = color
  }

  type P = Point

  def point(x: Double, y: Double): P = {
    val pt = new Point(x,y)
    pt.pPoint.setStroke(lineStroke)
    pt.pPoint.setStrokePaint(lineColor)
    currLayer.addChild(pt.pPoint)
    currLayer.repaint()
    pt
  }

  def line(p1: P, p2: P): Line = {
    val line = new Line(p1, p2)
    line.pLine.setStroke(lineStroke)
    line.pLine.setStrokePaint(lineColor)
    currLayer.addChild(line.pLine)
    currLayer.repaint()
    line
  }

  def line(x0: Double, y0: Double, x1: Double, y1: Double) = line(new Point(x0, y0), new Point(x1, y1))

  def ellipse(left: Double, top: Double, w: Double, h: Double) = {
    val ell = PPath.createEllipse(left.toFloat, top.toFloat, w.toFloat, h.toFloat)
    ell.setStroke(lineStroke)
    ell.setStrokePaint(lineColor)
    currLayer.addChild(ell)
    currLayer.repaint()
    ell
  }

  def animationStep(fn: => Unit) {
    val sketchAnimation = new PActivity(-1) {
      override def activityStep(elapsedTime: Long) {
        if (stopAnim) {
          terminate
          stopAnim = false
          listener.pendingCommandsDone
        }
        else {
          currLayer = animLayer
          try {
            fn
          }
          finally {
            listener.hasPendingCommands
            currLayer.repaint()
            currLayer = layer
          }
        }
      }
    }
    canvas.getRoot.addActivity(sketchAnimation)
  }

  def stop() {
    stopAnim = true
  }

  def onMouseMove(fn: (Double, Double) => Unit) {
    canvas.addInputEventListener(new PBasicInputEventHandler {
        override def mouseMoved(e: PInputEvent) {
          val pos = e.getPosition
          fn(pos.getX, pos.getY)
          currLayer.repaint()
        }
      })
  }

  private [kojo] def setSpriteListener(l: SpriteListener) {
    listener = l
  }

}
