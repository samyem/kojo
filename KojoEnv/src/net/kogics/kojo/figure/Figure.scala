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
import net.kogics.kojo.util.Utils

trait PointDesc extends core.Point

object Figure {
  def apply(canvas: SpriteCanvas, initX: Double = 0d, initY: Double = 0): Figure = {
    val fig = Utils.runInSwingThreadAndWait {
      new Figure(canvas, initX, initY)
    }
    fig
  }
}

class Figure private (canvas: SpriteCanvas, initX: Double, initY: Double) extends core.Figure {
  private val bgLayer = new PLayer
  private val fgLayer = new PLayer
  private var currLayer = bgLayer
  
  // if fgLayer is bigger than bgLayer, (re)painting does not happen very cleanly
  // needs a better fix than the one below
  bgLayer.setBounds(-500, -500, 1000, 1000)

  private val camera = canvas.getCamera
  val DefaultColor = Color.red
  val DefaultFillColor: Color = null
  val DefaultStroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
  @volatile private var listener: SpriteListener = NoopSpriteListener
  @volatile var stopAnim: Boolean = _

  var lineColor: Color = _
  var fillColor: Color = _
  var lineStroke: Stroke = _

  camera.addLayer(camera.getLayerCount-1, bgLayer)
  camera.addLayer(camera.getLayerCount-1, fgLayer)
  init()

  def init() {
    bgLayer.setOffset(initX, initY)
    fgLayer.setOffset(initX, initY)
    lineColor = DefaultColor
    fillColor = DefaultFillColor
    lineStroke = DefaultStroke
    stopAnim = false
  }

  def repaint() {
    bgLayer.repaint()
    fgLayer.repaint()
  }

  def clear {
    Utils.runInSwingThread {
      bgLayer.removeAllChildren()
      fgLayer.removeAllChildren()
      init()
      repaint()
    }
  }

  def fgClear {
    Utils.runInSwingThread {
      fgLayer.removeAllChildren()
      repaint()
    }
  }

  def remove() {
    Utils.runInSwingThread {
      camera.removeLayer(bgLayer)
      camera.removeLayer(fgLayer)
    }
  }

  def setPenColor(color: java.awt.Color) {
    Utils.runInSwingThread {
      lineColor = color
    }
  }

  def setPenThickness(t: Double) {
    Utils.runInSwingThread {
      lineStroke = new BasicStroke(t.toFloat, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
    }
  }

  def setFillColor(color: java.awt.Color) {
    Utils.runInSwingThread {
      fillColor = color
    }
  }

  type P = PointDesc

  def point(x: Double, y: Double): P = {
    val pt = new Point(x,y)
    Utils.runInSwingThread {
      pt.pPoint.setStroke(lineStroke)
      pt.pPoint.setStrokePaint(lineColor)
      currLayer.addChild(pt.pPoint)
      currLayer.repaint()
    }
    pt
  }

  def line(p1: P, p2: P): Line = {
    val line = new Line(p1, p2)
    Utils.runInSwingThread {
      line.pLine.setStroke(lineStroke)
      line.pLine.setStrokePaint(lineColor)
      currLayer.addChild(line.pLine)
      currLayer.repaint()
    }
    line
  }

  def line(x0: Double, y0: Double, x1: Double, y1: Double) = line(new LwPoint(x0, y0), new LwPoint(x1, y1))

  def ellipse(center: P, w: Double, h: Double): Ellipse = {
    val ell = new Ellipse(center, w, h)
    Utils.runInSwingThread {
      ell.pEllipse.setStroke(lineStroke)
      ell.pEllipse.setStrokePaint(lineColor)
      ell.pEllipse.setPaint(null)
      currLayer.addChild(ell.pEllipse)
      currLayer.repaint()
    }
    ell
  }

  def ellipse(cx: Double, cy: Double, w: Double, h: Double): Ellipse = {
    ellipse(new LwPoint(cx, cy), w, h)
  }

  def text(content: String, x: Double, y: Double): Text = {
    val txt = new Text(content, x, y)
    Utils.runInSwingThread {
      txt.pText.setTextPaint(lineColor)
      currLayer.addChild(txt.pText)
      currLayer.repaint()
    }
    txt
  }

  def refresh(fn: => Unit) {
    Utils.runInSwingThread {
      val sketchAnimation = new PActivity(-1) {
        override def activityStep(elapsedTime: Long) {
          if (stopAnim) {
            terminate
            stopAnim = false
            listener.pendingCommandsDone
          }
          else {
            currLayer = fgLayer
            try {
              fn
            }
            finally {
              listener.hasPendingCommands
              repaint()
              currLayer = bgLayer
            }
          }
        }
      }
      canvas.getRoot.addActivity(sketchAnimation)
    }
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

