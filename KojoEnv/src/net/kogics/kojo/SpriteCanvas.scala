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

import javax.swing._
import java.awt.{List => _, _}
import java.awt.event._
import java.util.logging._

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.nodes._
import edu.umd.cs.piccolo.util.PPaintContext
import edu.umd.cs.piccolo.event._

import net.kogics.kojo.core.SCanvas
import net.kogics.kojo.util.Utils

import org.openide.awt.StatusDisplayer

import scala.collection._
import scala.{math => Math}

import figure.Figure
import turtle.Turtle
import turtle.TurtleListener
import turtle.NoopTurtleListener
import turtle.Command
import core.Style

object SpriteCanvas extends InitedSingleton[SpriteCanvas] {
  def initedInstance(kojoCtx: KojoCtx) = synchronized {
    instanceInit()
    val ret = instance()
    ret.kojoCtx = kojoCtx
    ret
  }

  protected def newInstance = new SpriteCanvas
}

class SpriteCanvas private extends PCanvas with SCanvas {
  val Log = Logger.getLogger(getClass.getName);
  @volatile var kojoCtx: KojoCtx = _

  val defLayer = getLayer
  val AxesColor = new Color(100, 100, 100)
  val GridColor = new Color(200, 200, 200)

  var outputFn: String => Unit = { msg =>
    Log.info(msg)
  }

  setBackground(Color.white)
  setPreferredSize(new Dimension(200, 400))
  setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING)
  setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING)
  setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING)

  var turtles: List[Turtle] = Nil
  var puzzlers: List[Turtle] = Nil
  var figures: List[Figure] = Nil

  getCamera.addLayer(Turtle.handleLayer)

  var grid = new PNode()
  initCamera()

  val history = new mutable.Stack[Turtle]()

  addComponentListener(new ComponentAdapter {
      override def componentResized(e: ComponentEvent) = initCamera()
    })

  val megaListener = new CompositeListener()
  val turtle = newTurtle()
  val figure = newFigure()

  val panHandler = new PPanEventHandler() {
//    setAutopan(false)
    override def pan(event: PInputEvent) {
      super.pan(event)
      Utils.schedule(0.05) {
        updateGrid()
      }
    }
    
    override def dragActivityStep(event: PInputEvent) {
      super.dragActivityStep(event)
      Utils.schedule(0.05) {
        updateGrid()
      }
    }
  }

  val zoomHandler = new PZoomEventHandler {
    override def dragActivityStep(event: PInputEvent) {
      super.dragActivityStep(event)
      Utils.schedule(0.05) {
        updateGrid()
      }
    }
  }

  setPanEventHandler(panHandler)
  setZoomEventHandler(zoomHandler)

  addInputEventListener(new PBasicInputEventHandler {
      override def mouseMoved(e: PInputEvent) {
        val pos = e.getPosition
        val prec0 = Math.round(getCamera.getViewTransformReference.getScale) - 1
        val prec = {
          if (prec0 < 0) 0
          else if (prec0 > 18) 18
          else prec0
        }
        val statusStr = "Mouse Position: (%%.%df, %%.%df)" format(prec, prec)
        StatusDisplayer.getDefault().setStatusText(statusStr format(pos.getX, pos.getY));
      }
    })

  private def initCamera() {
    val size = getSize(null)
    getCamera.getViewTransformReference.setToScale(1, -1)
    getCamera.setViewOffset(size.getWidth/2f, size.getHeight/2f)
    updateGrid()
  }

  def updateGrid() {
    if (!defLayer.getChildrenReference.contains(grid))
      return
    
    val viewBounds = getCamera.getViewBounds()
    val width = viewBounds.width.toFloat
    val height = viewBounds.height.toFloat
    val vbx = viewBounds.x.toFloat
    val vby = viewBounds.y.toFloat

    val screenCenter = new java.awt.geom.Point2D.Float(vbx + width/2, vby + height/2)

    val numxGridlines = Math.ceil(height / 100).toInt + 4
    val numyGridlines = Math.ceil(width / 100).toInt + 4

    val yStart = {
      val y = viewBounds.y
      if (y < 0) Math.floor(y/100) * 100
      else Math.ceil(y/100) * 100
    } - 200

    val xStart = {
      val x = viewBounds.x
      if (x < 0) Math.floor(x/100) * 100
      else Math.ceil(x/100) * 100
    } - 200

    grid.removeAllChildren()

    for (i <- 0 until numxGridlines) {
      val gridline = PPath.createLine(screenCenter.x-width/2, (yStart + 100*i).toFloat, screenCenter.x+width/2, (yStart + 100*i).toFloat)
      gridline.setStrokePaint(GridColor)
      grid.addChild(gridline)
    }

    for (i <- 0 until numyGridlines) {
      val gridline = PPath.createLine((xStart + 100*i).toFloat, screenCenter.y+height/2, (xStart + 100*i).toFloat, screenCenter.y-height/2)
      gridline.setStrokePaint(GridColor)
      grid.addChild(gridline)
    }

    val xAxis = PPath.createLine(screenCenter.x-width/2, 0, screenCenter.x+width/2, 0)
    xAxis.setStrokePaint(AxesColor)
    val yAxis = PPath.createLine(0, screenCenter.y+height/2, 0, screenCenter.y-height/2)
    yAxis.setStrokePaint(AxesColor)
    grid.addChild(xAxis)
    grid.addChild(yAxis)
  }

  def gridOn() {
    Utils.runInSwingThread {
      if (!defLayer.getChildrenReference.contains(grid)) {
        defLayer.addChild(grid)
        updateGrid()
        repaint()
      }
    }
  }

  def gridOff() {
    Utils.runInSwingThread {
      if (defLayer.getChildrenReference.contains(grid)) {
        defLayer.removeChild(grid)
        repaint()
      }
    }
  }

  def zoom(factor: Double, cx: Double, cy: Double) {
    Utils.runInSwingThread {
      val size = getSize(null)
      getCamera.getViewTransformReference.setToScale(factor, -factor)
      getCamera.getViewTransformReference.setOffset(size.getWidth/2d - cx*factor, size.getHeight/2d + cy*factor)
      updateGrid()
      repaint()
    }
  }

  def zoomXY(xfactor: Double, yfactor: Double, cx: Double, cy: Double) {
    Utils.runInSwingThread {
      val size = getSize(null)
      getCamera.getViewTransformReference.setToScale(xfactor, -yfactor)
      getCamera.getViewTransformReference.setOffset(
        size.getWidth / 2d - cx * xfactor.abs,
        size.getHeight / 2d + cy * yfactor.abs
      )
      updateGrid()
      repaint()
    }
  }

  def afterClear() = {
    // initCamera()
  }

  def pushHistory(turtle: Turtle) = synchronized {
    history.push(turtle)
  }

  def popHistory() = synchronized {
    history.pop()
  }

  def clearHistory() = synchronized {
    history.clear()
  }

  def undo() {
    // The top level undo command is not meant to be used within a script
    // (unless the script has only undo commands and runs after the previous 
    // script has stopped). It should be used interactively as a single command.
    // If it is used in a script, race conditions will ensue:
    // - for single turtles: the command to be undone might not have run yet
    // - for multiple turtles: for a single entry on the turtle history stack,
    //   the corresponding turtle might get the undo command twice; due to this,
    //   another turtle might not get the undo command at all. Result - a big undo
    //   loop will not fully undo a painting
    var undoTurtle: Option[Turtle] = None
    synchronized {
      if (history.size > 0) {
        undoTurtle = Some(history.top)
      }
    }

    if (undoTurtle.isDefined) {
      // this will also pop the turtle from the canvas history
      // need to do it from within the turtle because users can
      // do a direct undo on a turtle and bypass the canvas
      undoTurtle.get.syncUndo()
    }
  }

  def hasUndoHistory = synchronized {history.size > 0}

  def ensureVisible() {
    kojoCtx.makeCanvasVisible()
  }

  def clear() {
    ensureVisible()
    stop()
    Utils.runInSwingThreadAndWait {
      turtles.foreach {t => if (t == turtle) t.clear() else t.remove()}
      turtles = List(turtles.last)

      figures.foreach {f => if (f == figure) f.clear() else f.remove()}
      figures = List(figures.last)
    }
//    turtle.waitFor
    clearHistory()
  }

  def clearPuzzlers() {
    stop()
    Utils.runInSwingThreadAndWait {
      puzzlers.foreach {t => t.remove()}
      puzzlers = Nil
    }
  }

  def stop() = {
    Utils.runInSwingThreadAndWait {
      puzzlers.foreach {t => t.stop}
      turtles.foreach {t => t.stop}
      figures.foreach {f => f.stop}
    }
  }

  val turtle0 = turtle
  val figure0 = figure

  def newFigure(x: Int = 0, y: Int = 0) = {
    val fig = Utils.runInSwingThreadAndWait {
      val f = Figure(this, x, y)
      f.setSpriteListener(megaListener)
      figures = f :: figures
      f
    }
    this.repaint()
    fig
  }

  def newTurtle(x: Int = 0, y: Int = 0) = {
    val ttl = Utils.runInSwingThreadAndWait {
      val t = new Turtle(this, "/images/turtle32.png", x, y)
      t.setTurtleListener(megaListener)
      turtles = t :: turtles
      t
    }
    this.repaint()
    ttl
  }

  def newPuzzler(x: Int = 0, y: Int = 0) = {
    val pzl = Utils.runInSwingThreadAndWait {
      val t = new Turtle(this, "/images/puzzler32.png", x, y, true)
      t.setTurtleListener(megaListener)
      t.setPenThickness(1)
      t.setPenColor(Color.blue)
      t.setAnimationDelay(10)
      puzzlers = t :: puzzlers
      t
    }
    this.repaint()
    pzl
  }

  def setTurtleListener(l: TurtleListener) {
    megaListener.setListener(l)
  }

  class CompositeListener extends TurtleListener {
    var startCount = 0
    @volatile var realListener: TurtleListener = NoopTurtleListener

    def setListener(l: TurtleListener) {
      if (realListener != NoopTurtleListener) throw new RuntimeException("SpriteCanvas - cannot reset listener")
      realListener = l
    }

    def hasPendingCommands: Unit = synchronized {
//      Log.info("Has Pending commands.")
      realListener.hasPendingCommands
    }

    def pendingCommandsDone(): Unit = synchronized {
//      Log.info("Pending commands done. Start count: " + startCount)
      if (startCount == 0) realListener.pendingCommandsDone
    }

    def commandStarted(cmd: Command): Unit = synchronized {
      startCount += 1
    }
    
    def commandDiscarded(cmd: Command): Unit = synchronized {
      startCount -= 1
      if (startCount == 0) realListener.pendingCommandsDone
    }

    def commandDone(cmd: Command): Unit = synchronized {
      startCount -= 1
      if (startCount == 0) realListener.pendingCommandsDone
    }
  }
}
