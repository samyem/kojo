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
import java.awt.{Point => _, _}

import net.kogics.kojo.util.Utils
import core._

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

  def dumpNumOfChildren: Int = currLayer.getChildrenCount
  def dumpChild(n: Int): AnyRef = {
    try {
      val c = currLayer.getChild(n)
      if (c.isInstanceOf[net.kogics.kojo.kgeom.PArc]) {
        c.asInstanceOf[net.kogics.kojo.kgeom.PArc]
      }
      else if (c.isInstanceOf[net.kogics.kojo.kgeom.PPoint]) {
        c.asInstanceOf[net.kogics.kojo.kgeom.PPoint]
      }
      else if (c.isInstanceOf[net.kogics.kojo.kgeom.PolyLine]) {
        c.asInstanceOf[net.kogics.kojo.kgeom.PolyLine]
      }
      else if (c.isInstanceOf[edu.umd.cs.piccolo.nodes.PPath]) {
        c.asInstanceOf[edu.umd.cs.piccolo.nodes.PPath]
      }
      else null
    }
    catch { case e => throw e }
  }
  def dumpChildString(n: Int) = {
    try {
      val c = currLayer.getChild(n)
      if (c.isInstanceOf[net.kogics.kojo.kgeom.PArc]) {
        "PArc(" + (c.getX.round + 1) + "," + (c.getY.round + 1) + ")"
      }
      else if (c.isInstanceOf[net.kogics.kojo.kgeom.PPoint]) {
        "PPoint(" + (c.getX.round + 1) + "," + (c.getY.round + 1) + ")"
      }
      else if (c.isInstanceOf[net.kogics.kojo.kgeom.PolyLine]) {
        "PolyLine(" + (c.getX.round + 2) + "," + (c.getY.round + 2) + ")"
      }
      else if (c.isInstanceOf[edu.umd.cs.piccolo.nodes.PPath]) {
        "PPath(" + (c.getX.round + 1) + "," + (c.getY.round + 1) + ")"
      }
      else c.toString
    }
    catch { case e => throw e }
  }
  def dumpLastOfCurrLayer: String = {
    if (currLayer.getChildrenCount > 0) {
      dumpChildString(currLayer.getChildrenCount - 1)
    } else { "<None>" }
  }
  
  // if fgLayer is bigger than bgLayer, (re)painting does not happen very cleanly
  // needs a better fix than the one below
  bgLayer.setBounds(-500, -500, 1000, 1000)

  private val camera = canvas.getCamera
  val DefaultColor = Color.red
  val DefaultFillColor: Color = null
  val DefaultStroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
  @volatile private var listener: SpriteListener = NoopSpriteListener

  var figAnimation: PActivity = _
  var lineColor: Color = _
  var fillColor: Color = _
  var lineStroke: BasicStroke = _

  camera.addLayer(camera.getLayerCount-1, bgLayer)
  camera.addLayer(camera.getLayerCount-1, fgLayer)
  init()

  def init() {
    bgLayer.setOffset(initX, initY)
    fgLayer.setOffset(initX, initY)
    lineColor = DefaultColor
    fillColor = DefaultFillColor
    lineStroke = DefaultStroke
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

  type FPoint = FigPoint
  type FLine = FigLine
  type FEllipse = FigEllipse
  type FArc = FigArc
  type FText = FigText
  type FRectangle = FigRectangle
  type FRRectangle = FigRoundRectangle
  type FPath = FigPath


  def point(x: Double, y: Double): FigPoint = {
    val pt = new FigPoint(canvas, x,y)
    Utils.runInSwingThread {
      pt.pPoint.setStroke(lineStroke)
      pt.pPoint.setStrokePaint(lineColor)
      currLayer.addChild(pt.pPoint)
      currLayer.repaint()
    }
    pt
  }

  def line(p1: Point, p2: Point): FigLine = {
    val line = new FigLine(canvas, p1, p2)
    Utils.runInSwingThread {
      line.pLine.setStroke(lineStroke)
      line.pLine.setStrokePaint(lineColor)
      currLayer.addChild(line.pLine)
      currLayer.repaint()
    }
    line
  }

  def line(x0: Double, y0: Double, x1: Double, y1: Double) = line(new Point(x0, y0), new Point(x1, y1))

  def ellipse(center: Point, w: Double, h: Double): FigEllipse = {
    val ell = new FigEllipse(canvas, center, w, h)
    Utils.runInSwingThread {
      ell.pEllipse.setStroke(lineStroke)
      ell.pEllipse.setStrokePaint(lineColor)
      ell.pEllipse.setPaint(fillColor)
      currLayer.addChild(ell.pEllipse)
      currLayer.repaint()
    }
    ell
  }

  def ellipse(cx: Double, cy: Double, w: Double, h: Double): FigEllipse = {
    ellipse(new Point(cx, cy), w, h)
  }

  def circle(cx: Double, cy: Double, radius: Double) = ellipse(cx, cy, 2*radius, 2*radius)
  
  def circle(cp: Point, radius: Double) = circle(cp.x, cp.y, radius)


  def arc(onEll: Ellipse, start: Double, extent: Double): FigArc = {
    val arc = new FigArc(canvas, onEll, start, extent)
    Utils.runInSwingThread {
      arc.pArc.setStroke(lineStroke)
      arc.pArc.setStrokePaint(lineColor)
      arc.pArc.setPaint(fillColor)
      currLayer.addChild(arc.pArc)
      currLayer.repaint()
    }
    arc

  }

  def arc(cx: Double, cy: Double, w: Double, h: Double, start: Double, extent: Double): FigArc = {
    arc(new Ellipse(new Point(cx, cy), w, h), start, extent)
  }

  def arc(cx: Double, cy: Double, r: Double, start: Double, extent: Double): FigArc = {
    arc(cx, cy, 2*r, 2*r, start, extent)
  }

  def arc(cp: Point, r: Double, start: Double, extent: Double): FArc = {
    arc(cp.x, cp.y, 2*r, 2*r, start, extent)
  }


  def rectangle(bLeft: Point, tRight: Point): FigRectangle = {
    val rect = new FigRectangle(canvas, bLeft, tRight)
    Utils.runInSwingThread {
      rect.pRect.setStroke(lineStroke)
      rect.pRect.setStrokePaint(lineColor)
      rect.pRect.setPaint(fillColor)
      currLayer.addChild(rect.pRect)
      currLayer.repaint()
    }
    rect
  }

  def rectangle(x0: Double, y0: Double, w: Double, h: Double) = rectangle(new Point(x0, y0), new Point(x0+w, y0+h))

  def roundRectangle(p1: Point, p2: Point, rx: Double, ry: Double) = {
    val rrect = new FigRoundRectangle(canvas, p1, p2, rx, ry)
    Utils.runInSwingThread {
      rrect.pRect.setStroke(lineStroke)
      rrect.pRect.setStrokePaint(lineColor)
      rrect.pRect.setPaint(fillColor)
      currLayer.addChild(rrect.pRect)
      currLayer.repaint()
    }
    rrect
  }

  def text(content: String, x: Double, y: Double): FigText = {
    val txt = new FigText(canvas, content, x, y)
    Utils.runInSwingThread {
      txt.pText.setTextPaint(lineColor)
      currLayer.addChild(txt.pText)
      currLayer.repaint()
    }
    txt
  }

  def text(content: String, p: Point): FText = text(content, p.x, p.y)


  def polyLine(path: kgeom.PolyLine): kgeom.PolyLine = {
    Utils.runInSwingThread {
      path.setStroke(lineStroke)
      path.setStrokePaint(lineColor)
      path.setPaint(fillColor)
      currLayer.addChild(path)
      currLayer.repaint()
    }
    path
  }


  def path(descriptor: String): FigPath = {
    val path = new FigPath(canvas, descriptor)
    Utils.runInSwingThread {
      path.pPath.setStroke(lineStroke)
      path.pPath.setStrokePaint(lineColor)
      path.pPath.setPaint(fillColor)
      currLayer.addChild(path.pPath)
      currLayer.repaint
    }
    path
  }


  def ppath(path: PPath) {
    Utils.runInSwingThread {
      currLayer.addChild(path)
      currLayer.repaint()
    }
  }


  def refresh(fn: => Unit) {
    
    Utils.runInSwingThread {
      if (figAnimation != null ) {
        return
      }
      
      figAnimation = new PActivity(-1) {
        override def activityStep(elapsedTime: Long) {
          currLayer = fgLayer
          try {
            fn
            if (isStepping) {
              listener.hasPendingCommands()
            }
          }
          catch {
            case t: Throwable =>
              canvas.outputFn("Problem: " + t.toString())
              stop()
          }
          finally {
            repaint()
            currLayer = bgLayer
          }
        }
      }

      figAnimation.setDelegate(new PActivityDelegate {
          override def activityStarted(activity: PActivity) {}
          override def activityStepped(activity: PActivity) {}
          override def activityFinished(activity: PActivity) {
            listener.pendingCommandsDone()
          }
        })

      canvas.getRoot.addActivity(figAnimation)
    }
  }

  def stopRefresh() = stop()

  def stop() {
    Utils.runInSwingThread {
      if (figAnimation != null) {
        figAnimation.terminate(PActivity.TERMINATE_AND_FINISH)
        figAnimation = null
      }
    }
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

