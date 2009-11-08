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

object SpriteCanvas extends Singleton[SpriteCanvas] {
  protected def newInstance = new SpriteCanvas
}

class SpriteCanvas private extends PCanvas with SCanvas {
  val Log = Logger.getLogger(getClass.getName);
  val defLayer = getLayer

  var outputFn: String => Unit = { msg =>
    Log.info(msg)
  }

  setBackground(Color.white)
  setPreferredSize(new Dimension(200, 400))
  setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING)
  setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING)

  var turtles: List[Geometer] = Nil
  var puzzlers: List[Geometer] = Nil

  getCamera.addLayer(Geometer.handleLayer)

  initCamera()

  addComponentListener(new ComponentAdapter {
      override def componentResized(e: ComponentEvent) = initCamera()
    })

  val megaListener = new CompositeListener()
  val turtle = newTurtle()

  val xAxis = PPath.createLine(-250, 0, 250, 0)
  xAxis.setStrokePaint(Color.gray)
  val yAxis = PPath.createLine(0, 250, 0, -250)
  yAxis.setStrokePaint(Color.gray)

  addInputEventListener(new PBasicInputEventHandler {
      override def mouseMoved(e: PInputEvent) {
        val pos = e.getPosition
        StatusDisplayer.getDefault().setStatusText("Mouse Position: (%.0f, %.0f)" format(pos.getX, pos.getY));
      }
    })

  private def initCamera() {
    val size = getSize(null)
    getCamera.getViewTransformReference.setToScale(1, -1)
    getCamera.setViewOffset(size.getWidth/2f, size.getHeight/2f)
  }

  def axesOn() {
    defLayer.addChild(0, xAxis)
    defLayer.addChild(1, yAxis)
    repaint()
  }

  def axesOff() {
    defLayer.removeChild(xAxis)
    defLayer.removeChild(yAxis)
    repaint()
  }

  def afterClear() = {
    // initCamera()
  }

  def forward(n: Double) {
    turtle.forward(n)
  }

  def turn(angle: Double) {
    turtle.turn(angle)
  }

  def clear() {
    stop()
    val latch = new CountDownLatch(1)
    Utils.runInSwingThread {
      turtles.foreach {t => if (t == turtle) t.clear() else t.remove()}
      turtles = List(turtles.last)
      latch.countDown
    }
    latch.await
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

  def right() = turtle.right()
  def left() = turtle.left()

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
//      t = new Sprite(this, "/images/turtle32.png", x, y)
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

  def newGeometer(x: Int = 0, y: Int = 0) = {
    var t: Geometer = null
    val latch = new CountDownLatch(1)
    Utils.runInSwingThread {
      t = new Geometer(this, "/images/turtle32.png", x, y)
      t.setSpriteListener(megaListener)
//      turtles = t :: turtles
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
