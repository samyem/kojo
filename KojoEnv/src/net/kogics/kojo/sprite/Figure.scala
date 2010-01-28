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

package net.kogics.kojo.sprite

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.nodes._
import edu.umd.cs.piccolo.util._
import edu.umd.cs.piccolo.event._
import edu.umd.cs.piccolo.activities.PActivity
import edu.umd.cs.piccolo.activities.PActivity.PActivityDelegate

import javax.swing._
import java.awt._

import net.kogics.kojo.core

class Figure(canvas: SpriteCanvas, initX: Double = 0d, initY: Double = 0) extends core.Figure {
  private val layer = new PLayer
  private val animLayer = new PLayer
  private var currLayer = layer
  private val camera = canvas.getCamera
  val DefaultColor = Color.red
  val DefaultFillColor: Color = null
  val DefaultStroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
  @volatile private var listener: SpriteListener = NoOpListener
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

  def line(x0: Double, y0: Double, x1: Double, y1: Double): PPath = {
    val line = PPath.createLine(x0.toFloat, y0.toFloat, x1.toFloat, y1.toFloat)
    line.setStroke(lineStroke)
    line.setStrokePaint(lineColor)
    currLayer.addChild(line)
    currLayer.repaint()
    line
  }

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

  private [sprite] def setSpriteListener(l: SpriteListener) {
    listener = l
  }

}
