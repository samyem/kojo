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
import java.awt._
import java.awt.geom._
import java.awt.event._
import java.util.logging._

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.nodes._
import edu.umd.cs.piccolo.util._
import edu.umd.cs.piccolo.activities.PActivity
import edu.umd.cs.piccolo.activities.PActivity.PActivityDelegate

import scala.collection._
import scala.actors._
import scala.actors.Actor._

import net.kogics.kojo._
import net.kogics.kojo.util._

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{CountDownLatch, TimeUnit}


abstract sealed class Command(val valid: AtomicBoolean)
case class Forward(n: Double, v: AtomicBoolean) extends Command(v)
case class Turn(angle: Double, v: AtomicBoolean)  extends Command(v)
case class Clear(v: AtomicBoolean) extends Command(v)
case class Remove(v: AtomicBoolean) extends Command(v)
case class PenUp(v: AtomicBoolean) extends Command(v)
case class PenDown(v: AtomicBoolean) extends Command(v)
case class Towards(x: Double, y: Double, v: AtomicBoolean) extends Command(v)
case class JumpTo(x: Double, y: Double, v: AtomicBoolean) extends Command(v)
case class MoveTo(x: Double, y: Double, v: AtomicBoolean) extends Command(v)
case class SetAnimationDelay(d: Long, v: AtomicBoolean) extends Command(v)
case class GetAnimationDelay(latch: CountDownLatch, v: AtomicBoolean) extends Command(v)
case class GetPosition(latch: CountDownLatch, v: AtomicBoolean) extends Command(v)
case class GetHeading(latch: CountDownLatch, v: AtomicBoolean) extends Command(v)
case class SetPenColor(color: Color, v: AtomicBoolean) extends Command(v)
case class SetPenThickness(t: Double, v: AtomicBoolean) extends Command(v)
case class SetFillColor(color: Color, v: AtomicBoolean) extends Command(v)
case class BeamsOn(v: AtomicBoolean) extends Command(v)
case class BeamsOff(v: AtomicBoolean) extends Command(v)
case object CommandDone
case object Stop

trait SpriteListener {
  /**
   * The Sprite has pending commands in its queue
   */
  def hasPendingCommands: Unit

  /**
   * The Sprite has no more pending commands.
   */
  def pendingCommandsDone(): Unit

  def commandStarted(cmd: Command): Unit
  def commandDiscarded(cmd: Command): Unit
  def commandDone(cmd: Command): Unit

}

abstract class AbstractSpriteListener extends SpriteListener {
  def hasPendingCommands: Unit = {}
  def pendingCommandsDone(): Unit = {}
  def commandStarted(cmd: Command): Unit = {}
  def commandDiscarded(cmd: Command): Unit = {}
  def commandDone(cmd: Command): Unit = {}
}

object NoOpListener extends AbstractSpriteListener {}

trait Pen {
  def init(): Unit
  def clear(): Unit
  def positionAt(x: Float, y: Float): Unit
  def startMove(x: Float, y: Float): Unit
  def move(x: Float, y: Float): Unit
  def endMove(x: Float, y: Float): Unit
  def setColor(color: Color): Unit
  def setThickness(t: Double): Unit
  def setFillColor(color: Color): Unit
}

abstract class AbstractPen(sprite: Sprite, penPaths: mutable.ArrayBuffer[PPath], layer: PLayer) extends Pen {
  val Log = Logger.getLogger(getClass.getName);

  val DefaultColor = Color.red
  val DefaultFillColor = null
  val DefaultStroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
  var lineColor: Color = _
  var fillColor: Color = _
  var lineStroke: Stroke = _

  def init() {
    lineColor = DefaultColor
    fillColor = DefaultFillColor
    lineStroke = DefaultStroke
    addNewPath()
  }

  def newPath(): PPath = {
    val penPath = new PPath
    penPath.moveTo(sprite._position.x.toFloat, sprite._position.y.toFloat)
    penPath.setStroke(lineStroke)
    penPath.setStrokePaint(lineColor)
    penPath.setPaint(fillColor)
    penPath
  }

  def addNewPath() {
    val penPath = newPath()
    penPaths += penPath
    layer.addChild(layer.getChildrenCount-1, penPath)
  }

  def setColor(color: Color) {
    lineColor = color
    addNewPath()
  }

  def setThickness(t: Double) {
    lineStroke = new BasicStroke(t.toFloat, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
    addNewPath()
  }
  def setFillColor(color: Color) {
    fillColor = color
    addNewPath()
  }

  def addToLayer() = {
    penPaths.foreach {penPath => layer.addChild(layer.getChildrenCount-1, penPath)}
  }

  def clear() = {
    penPaths.foreach { penPath =>
      penPath.reset()
      layer.removeChild(penPath)
    }
    penPaths.clear
  }

  def positionAt(x: Float, y: Float) = penPaths.last.moveTo(x, y)
}

class UpPen(sprite: Sprite, penPaths: mutable.ArrayBuffer[PPath], layer: PLayer) extends AbstractPen(sprite, penPaths, layer) {
  def startMove(x: Float, y: Float) {}
  def move(x: Float, y: Float) {}
  def endMove(x: Float, y: Float) {
    penPaths.last.moveTo(x, y)
  }
}

class DownPen(sprite: Sprite, penPaths: mutable.ArrayBuffer[PPath], layer: PLayer) extends AbstractPen(sprite, penPaths, layer) {
  var tempLine = new PPath
  val lineAnimationColor = Color.orange

  def startMove(x: Float, y: Float) {
    tempLine.setStroke(lineStroke)
    tempLine.setStrokePaint(lineAnimationColor)
    tempLine.moveTo(x, y)
    layer.addChild(layer.getChildrenCount-1, tempLine)
  }
  def move(x: Float, y: Float) {
    tempLine.lineTo(x, y)
    tempLine.repaint()
  }
  def endMove(x: Float, y: Float) {
    layer.removeChild(tempLine)
    tempLine.reset
    penPaths.last.lineTo(x, y)
    penPaths.last.repaint()
  }
}

class Sprite(canvas: SpriteCanvas, fname: String, initX: Double = 0d, initY: Double = 0, bottomLayer: Boolean = false) extends core.Sprite {
  val Log = Logger.getLogger(getClass.getName);
  Log.info("Sprite being created in thread: " + Thread.currentThread.getName)

  private val layer = new PLayer
  private val camera = canvas.getCamera
  if (bottomLayer) camera.addLayer(0, layer) else camera.addLayer(layer)
  private val throttler = new Throttler {}
  @volatile var _animationDelay = 0l

  private val turtleImage = new PImage(Utils.loadImage(fname))
  private val turtle = new PNode
  turtle.addChild(turtleImage)
  turtleImage.getTransformReference(true).setToScale(1, -1)
  turtleImage.setOffset(-16, 16)

  val xBeam = PPath.createLine(0, 30, 0, -30)
  xBeam.setStrokePaint(Color.gray)
  val yBeam = PPath.createLine(-20, 0, 50, 0)
  yBeam.setStrokePaint(Color.gray)

  val (downPen, upPen) = makePens
  @volatile var pen: Pen = downPen

  layer.addChild(turtle)

  var _position: Point2D.Double = _
  private var theta: Double = _
  @volatile var removed: Boolean = false

  val CommandActor = makeCommandProcessor()

  def init() {
    _animationDelay = 1000l
    _position = new Point2D.Double(initX, initY)
    pen.init
    turtle.setOffset(initX, initY)
    resetRotation
  }

  init

  @volatile var cmdBool = new AtomicBoolean(true)
  @volatile var listener: SpriteListener = NoOpListener

  def deg2radians(angle: Double) = angle * Math.Pi / 180
  def rad2degrees(angle: Double) = angle * 180 / Math.Pi
  def thetaDegrees = rad2degrees(theta)
  def thetaRadians = theta

  def realWorker(fn: (() => Unit) => Unit) {
    Utils.runInSwingThread { 
      fn { () =>
        CommandActor ! CommandDone
      }
    }
  }

  def enqueueCommand(cmd: Command) {
    if (removed) return
    
    CommandActor ! cmd
    throttler.throttle
  }

  def forward(n: Double) = enqueueCommand(Forward(n, cmdBool))
  def turn(angle: Double) = enqueueCommand(Turn(angle, cmdBool))
  def clear() = enqueueCommand(Clear(cmdBool))
  def penUp() = enqueueCommand(PenUp(cmdBool))
  def penDown() = enqueueCommand(PenDown(cmdBool))
  def towards(x: Double, y: Double) = enqueueCommand(Towards(x, y, cmdBool))
  def jumpTo(x: Double, y: Double) = enqueueCommand(JumpTo(x, y, cmdBool))
  def moveTo(x: Double, y: Double) = enqueueCommand(MoveTo(x, y, cmdBool))
  def setAnimationDelay(d: Long) = enqueueCommand(SetAnimationDelay(d, cmdBool))
  def setPenColor(color: Color) = enqueueCommand(SetPenColor(color, cmdBool))
  def setPenThickness(t: Double) = enqueueCommand(SetPenThickness(t, cmdBool))
  def setFillColor(color: Color) = enqueueCommand(SetFillColor(color, cmdBool))
  def beamsOn() = enqueueCommand(BeamsOn(cmdBool))
  def beamsOff() = enqueueCommand(BeamsOff(cmdBool))

  def remove() = {
    enqueueCommand(Remove(cmdBool))
    removed = true
  }

  def getWorker(action: Symbol) {
    val latch = new CountDownLatch(1)
    val cmd = action match {
      case 'animationDelay => GetAnimationDelay(latch, cmdBool)
      case 'position => GetPosition(latch, cmdBool)
      case 'heading => GetHeading(latch, cmdBool)
    }

    enqueueCommand(cmd)

    var done = latch.await(10, TimeUnit.MILLISECONDS)
    while(!done) {
      listener.hasPendingCommands
      done = latch.await(10, TimeUnit.MILLISECONDS)
    }
    
    listener.pendingCommandsDone
  }

  def animationDelay: Long = {
    getWorker('animationDelay)
    _animationDelay
  }

  def position: (Double, Double) = {
    getWorker('position)
    (_position.getX, _position.getY)
  }

  def heading: Double = {
    getWorker('heading)
    thetaDegrees
  }

  def realForward(n: Double) {
    val p0 = _position
    val delX = Math.cos(theta) * n
    val delY = Math.sin(theta) * n
    val pf = new Point2D.Double(p0.x + delX, p0.y + delY)

    realWorker { doneFn =>
      if (Utils.doublesEqual(n, 0, 0.001)) {
        doneFn()
        return
      }

      pen.startMove(p0.x.toFloat, p0.y.toFloat)

      val lineAnimation = new PActivity(_animationDelay) {
        override def activityStep(elapsedTime: Long) {
          val frac = if (_animationDelay == 0) 1d else elapsedTime.toDouble / _animationDelay
          val currX = p0.x * (1-frac) + pf.x * frac
          val currY = p0.y * (1-frac) + pf.y * frac
          pen.move(currX.toFloat, currY.toFloat)
          turtle.setOffset(currX, currY)
          turtle.repaint()
        }
      }

      lineAnimation.setDelegate(new PActivityDelegate {
          override def activityStarted(activity: PActivity) {}
          override def activityStepped(activity: PActivity) {}
          override def activityFinished(activity: PActivity) {
            _position = pf
            pen.endMove(pf.x.toFloat, pf.y.toFloat)
            turtle.setOffset(pf.x, pf.y)
            turtle.repaint()
            doneFn()
          }
        })

      canvas.getRoot.addActivity(lineAnimation)
    }
  }

  def realTurn(angle: Double) {
    realWorker { doneFn =>
      theta += deg2radians(angle)
      if (theta < 0) theta = theta % (2*Math.Pi) + 2*Math.Pi
      else if (theta > 2*Math.Pi) theta = theta % (2*Math.Pi)
      turtle.setRotation(theta)
      turtle.repaint()
      doneFn()
    }
  }

  def realClear() {
    realWorker { doneFn =>
      pen.clear()
      init()
      turtle.repaint()
      canvas.afterClear()
      doneFn()
    }
  }

  def realRemove() {
    realWorker { doneFn =>
      pen.clear
      layer.removeChild(turtle)
      camera.removeLayer(layer)
      doneFn()
      CommandActor ! Stop
    }
  }

  def realPenUp() {
    pen = upPen
    CommandActor ! CommandDone
  }

  def realPenDown() {
    pen = downPen
    CommandActor ! CommandDone
  }

  def realTowards(x: Double, y: Double) {
    val (x0, y0) = (_position.x, _position.y)
    val delX = x - x0
    val delY = y - y0
    var newTheta = if (Utils.doublesEqual(delX,0,0.001)) {
      if (Utils.doublesEqual(delY,0,0.001)) theta
      else if (delY > 0) Math.Pi/2
      else 3*Math.Pi/2
    }
    else {
      var nt2 = Math.atan(delY/delX)
      if (delX < 0 && delY > 0) nt2 += Math.Pi
      else if (delX < 0 && delY < 0) nt2 += Math.Pi
      else if (delX > 0 && delY < 0) nt2 += 2* Math.Pi
      nt2
    }

    realWorker { doneFn =>
      theta = newTheta
      turtle.setRotation(theta)
      turtle.repaint()
      doneFn()
    }
  }

  def realJumpTo(x: Double, y: Double) {
    realWorker { doneFn =>
      _position.setLocation(x, y)
      pen.positionAt(x.toFloat, y.toFloat)
      turtle.setOffset(x, y)
      turtle.repaint()
      doneFn()
    }
  }

  def realMoveTo(x: Double, y: Double) {
    def distanceTo(x: Double, y: Double): Double = {
      val (x0,y0) = (_position.x, _position.y)
      val delX = Math.abs(x-x0)
      val delY = Math.abs(y-y0)
      Math.sqrt(delX * delX + delY * delY)
    }

    realTowards(x, y)
    CommandActor.receive {
      case CommandDone =>
    }
    realForward(distanceTo(x,y))
  }

  def realSetAnimationDelay(d: Long) {
    _animationDelay = d
    CommandActor ! CommandDone
  }

  def realGetWorker() {
    CommandActor ! CommandDone
  }

  def realSetPenColor(color: Color) {
    realWorker { doneFn =>
      pen.setColor(color)
      doneFn()
    }
  }

  def realSetPenThickness(t: Double) {
    realWorker { doneFn =>
      pen.setThickness(t)
      doneFn()
    }
  }

  def realSetFillColor(color: Color) {
    realWorker { doneFn =>
      pen.setFillColor(color)
      doneFn()
    }
  }

  def realBeamsOn() {
    realWorker { doneFn =>
      turtle.addChild(0, xBeam)
      turtle.addChild(1, yBeam)
      turtle.repaint()
      doneFn()
    }
  }

  def realBeamsOff() {
    realWorker { doneFn =>
      turtle.removeChild(xBeam)
      turtle.removeChild(yBeam)
      turtle.repaint()
      doneFn()
    }
  }

  def right() = turn(-90)
  def left() = turn(90)

  def resetRotation() {
    theta = deg2radians(90)
    turtle.setRotation(theta)
  }

  def stop() {
    cmdBool.set(false)
    cmdBool = new AtomicBoolean(true)
  }

  def setSpriteListener(l: SpriteListener) {
//    if (listener != NoOpListener) throw new RuntimeException("Cannot re-set Sprite listener")
    listener = l
  }

  def makePens(): (Pen, Pen) = {
    val paths = new mutable.ArrayBuffer[PPath]
    val downPen = new DownPen(this, paths, layer)
    val upPen = new UpPen(this, paths, layer)
    (downPen, upPen)
  }

  def makeCommandProcessor() = actor {

    def processGetCommand(cmd: Command, latch: CountDownLatch)(fn: => Unit) {
      processCommand(cmd)(fn)
      latch.countDown()
    }

    def processCommand(cmd: Command)(fn: => Unit) {
      if (cmd.valid.get) {
        listener.hasPendingCommands
        listener.commandStarted(cmd)
        try {
          fn
          receive {
            case CommandDone =>
          }
        }
        finally {
          listener.commandDone(cmd)
        }
      }
      else {
        listener.commandDiscarded(cmd)
      }
//      Log.info("Command Handled. Mailbox size: " + mailboxSize)
      if (mailboxSize == 0) listener.pendingCommandsDone
    }

    var done = false

    loopWhile(!done) {
      react {
        case Stop =>
          done = true
        case cmd @ Forward(n, b) =>
          processCommand(cmd) {
            realForward(n)
          }
        case cmd @ Turn(angle, b) =>
          processCommand(cmd) {
            realTurn(angle)
          }
        case cmd @ Clear(b) =>
          processCommand(cmd) {
            realClear
          }
        case cmd @ Remove(b) =>
          processCommand(cmd) {
            realRemove
          }
        case cmd @ PenUp(b) =>
          processCommand(cmd) {
            realPenUp
          }
        case cmd @ PenDown(b) =>
          processCommand(cmd) {
            realPenDown
          }
        case cmd @ Towards(x, y, b) =>
          processCommand(cmd) {
            realTowards(x, y)
          }
        case cmd @ JumpTo(x, y, b) =>
          processCommand(cmd) {
            realJumpTo(x, y)
          }
        case cmd @ MoveTo(x, y, b) =>
          processCommand(cmd) {
            realMoveTo(x, y)
          }
        case cmd @ SetAnimationDelay(d, b) =>
          processCommand(cmd) {
            realSetAnimationDelay(d)
          }
        case cmd @ GetAnimationDelay(l, b) =>
          processGetCommand(cmd, l) {
            realGetWorker()
          }
        case cmd @ GetPosition(l, b) =>
          processGetCommand(cmd, l) {
            realGetWorker()
          }
        case cmd @ GetHeading(l, b) =>
          processGetCommand(cmd, l) {
            realGetWorker()
          }
        case cmd @ SetPenColor(color, b) =>
          processCommand(cmd) {
            realSetPenColor(color)
          }
        case cmd @ SetPenThickness(t, b) =>
          processCommand(cmd) {
            realSetPenThickness(t)
          }
        case cmd @ SetFillColor(color, b) =>
          processCommand(cmd) {
            realSetFillColor(color)
          }
        case cmd @ BeamsOn(b) =>
          processCommand(cmd) {
            realBeamsOn
          }
        case cmd @ BeamsOff(b) =>
          processCommand(cmd) {
            realBeamsOff
          }
      }
    }
  }
}

