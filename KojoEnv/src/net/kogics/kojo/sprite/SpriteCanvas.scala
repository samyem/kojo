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

import javax.swing._
import java.awt.{List => _, _}
import java.awt.event._
import java.util.logging._
import java.util.concurrent.CountDownLatch

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.nodes._
import edu.umd.cs.piccolo.util.PPaintContext
import edu.umd.cs.piccolo.event._

import net.kogics.kojo.core.SCanvas
import net.kogics.kojo.Singleton
import net.kogics.kojo.util.Utils

import org.openide.awt.StatusDisplayer
import scala.collection._

object SpriteCanvas extends Singleton[SpriteCanvas] {
  protected def newInstance = new SpriteCanvas
}

class SpriteCanvas private extends PCanvas with SCanvas {
  val Log = Logger.getLogger(getClass.getName);
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

  @volatile var turtles: List[Geometer] = Nil
  var puzzlers: List[Geometer] = Nil

  getCamera.addLayer(Geometer.handleLayer)

  var grid = new PNode()
  initCamera()

  val history = new mutable.Stack[Geometer]()

  addComponentListener(new ComponentAdapter {
      override def componentResized(e: ComponentEvent) = initCamera()
    })

  val megaListener = new CompositeListener()
  val turtle = newTurtle()

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

  addInputEventListener(new PZoomEventHandler {
      override def dragActivityStep(event: PInputEvent) {
        Utils.schedule(0.05) {
          updateGrid()
        }
      }
    })

  addInputEventListener(new PPanEventHandler{
      override def pan(event: PInputEvent) {
        Utils.schedule(0.05) {
          updateGrid()
        }
      }
    })

//  getPanEventHandler().setAutopan(false)

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

  def afterClear() = {
    // initCamera()
  }

  def pushHistory(sprite: Geometer) = synchronized {
    history.push(sprite)
  }

  def popHistory() = synchronized {
    history.pop()
  }

  def clearHistory() = synchronized {
    history.clear()
  }

  def undo() {
    var undoTurtle: Option[Geometer] = None
    synchronized {
      if (history.size > 0) {
        undoTurtle = Some(history.top)
      }
    }

    if (undoTurtle.isDefined) {
      // this will also pop the turtle from the canvas history
      // need to do it from within the sprite because users can
      // do a direct undo on a turtle and bypass the canvas
      undoTurtle.get.syncUndo()
    }
  }

  def hasUndoHistory = synchronized {history.size > 0}

  def clear() {
    stop()
    val latch = new CountDownLatch(1)
    Utils.runInSwingThread {
      turtles.foreach {t => if (t == turtle) t.clear() else t.remove()}
      turtles = List(turtles.last)
      latch.countDown
    }
    latch.await
    turtle.waitFor
    clearHistory()
  }

  def clearPuzzlers() {
    stop()
    val latch = new CountDownLatch(1)
    Utils.runInSwingThread {
      puzzlers.foreach {t => t.remove()}
      puzzlers = Nil
      latch.countDown
    }
    latch.await
  }

  def forward(n: Double) = turtle.forward(n)
  def turn(angle: Double) = turtle.turn(angle)
  def penUp() = turtle.penUp()
  def penDown() = turtle.penDown()
  def setPenColor(color: Color) = turtle.setPenColor(color)
  def setPenThickness(t: Double) = turtle.setPenThickness(t)
  def setFillColor(color: Color) = turtle.setFillColor(color)

  def towards(x: Double, y: Double) = turtle.towards(x, y)
  def position: (Double, Double) = turtle.position
  def heading: Double = turtle.heading
  
  def jumpTo(x: Double, y: Double) = turtle.jumpTo(x, y)
  def moveTo(x: Double, y: Double) = turtle.moveTo(x, y)

  def animationDelay = turtle.animationDelay
  def setAnimationDelay(d: Long) {
    turtle.setAnimationDelay(d)
  }

  def beamsOn() = turtle.beamsOn()
  def beamsOff() = turtle.beamsOff()

  def write(text: String) = turtle.write(text)

  def visible() = turtle.visible()
  def invisible() = turtle.invisible()

  def point(x: Double, y: Double) = turtle.point(x, y)
  def pathToPolygon() = turtle.pathToPolygon()
  def pathToParallelogram() = turtle.pathToParallelogram()

  def stop() = {
    val latch = new CountDownLatch(1)
    Utils.runInSwingThread {
      puzzlers.foreach {t => t.stop}
      turtles.foreach {t => t.stop}
      latch.countDown
    }
    latch.await
  }

  def turtle0 = turtle

  def newTurtle(x: Int = 0, y: Int = 0) = {
    var t: Geometer = null
    val latch = new CountDownLatch(1)
    Utils.runInSwingThread {
      t = new Geometer(this, "/images/turtle32.png", x, y)
      t.setSpriteListener(megaListener)
      turtles = t :: turtles
      latch.countDown()
    }
    latch.await
    this.repaint()
    t
  }

  def newPuzzler(x: Int = 0, y: Int = 0) = {
    var t: Geometer = null
    val latch = new CountDownLatch(1)
    Utils.runInSwingThread {
      t = new Geometer(this, "/images/puzzler32.png", x, y, true)
      t.setSpriteListener(megaListener)
      t.setPenThickness(1)
      t.setPenColor(Color.blue)
      t.setAnimationDelay(10)
      puzzlers = t :: puzzlers
      latch.countDown()
    }
    latch.await
    this.repaint()
    t
  }

  def setSpriteListener(l: SpriteListener) {
    megaListener.setListener(l)
  }

  class CompositeListener extends SpriteListener {
    var startCount = 0
    @volatile var realListener: SpriteListener = NoOpListener

    def setListener(l: SpriteListener) {
      if (realListener != NoOpListener) throw new RuntimeException("SpriteCanvas - cannot reset listener")
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
    }

    def commandDone(cmd: Command): Unit = synchronized {
      startCount -= 1
    }
  }
}
