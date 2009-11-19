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
import net.kogics.kojo.geom._
import net.kogics.kojo.core.geom._

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{CountDownLatch, TimeUnit}

object Geometer {
  val handleLayer = new PLayer
}

class Geometer(canvas: SpriteCanvas, fname: String, initX: Double = 0d, initY: Double = 0, bottomLayer: Boolean = false) extends core.Sprite {
  val Log = Logger.getLogger(getClass.getName)
  Log.info("Sprite being created in thread: " + Thread.currentThread.getName)

  private val layer = new PLayer
  private val camera = canvas.getCamera
  // the zeroth layer is for the grid etc
  // bottom sprite layer is at index 1
  if (bottomLayer) camera.addLayer(1, layer) else camera.addLayer(camera.getLayerCount-1, layer)
  private val throttler = new Throttler {}
  @volatile var _animationDelay = 0l

  private val turtleImage = new PImage(Utils.loadImage(fname))
  private val turtle = new PNode
  turtleImage.getTransformReference(true).setToScale(1, -1)
  turtleImage.setOffset(-16, 16)

  val xBeam = PPath.createLine(0, 30, 0, -30)
  xBeam.setStrokePaint(Color.gray)
  val yBeam = PPath.createLine(-20, 0, 50, 0)
  yBeam.setStrokePaint(Color.gray)

  val penPaths = new mutable.ArrayBuffer[PolyLine]
  val pens = makePens
  val DownPen = pens._1
  val UpPen = pens._2
  @volatile var pen: Pen = DownPen

  @volatile var _position: Point2D.Double = _
  @volatile private var theta: Double = _
  @volatile var removed: Boolean = false

  val CommandActor = makeCommandProcessor()
  @volatile var geomObj: DynamicShape = _
  val history = new mutable.Stack[UndoCommand]

  def changePos(x: Double, y: Double) {
    Log.info("Changing position to: " + (x, y))
    _position = new Point2D.Double(x, y)
    turtle.setOffset(x, y)
  }

  def changeHeading(newTheta: Double) {
    theta = newTheta
    turtle.setRotation(theta)
  }

  def init() {
    _animationDelay = 1000l
    changePos(initX, initY)
    if (!turtle.getChildrenReference.contains(turtleImage))
      turtle.addChild(turtleImage)
    layer.addChild(turtle)

    pen.init
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
    internalEnqueueCommand(cmd)
  }

  def internalEnqueueCommand(cmd: Command) {
    CommandActor ! cmd
    throttler.throttle
  }

  def undo() = enqueueCommand(Undo())
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
  def write(text: String) = enqueueCommand(Write(text, cmdBool))
  def visible() = enqueueCommand(Show(cmdBool))
  def point(x: Double, y: Double) = enqueueCommand(Point(x, y, cmdBool))

  def invisible() {
    beamsOff()
    enqueueCommand(Hide(cmdBool))
  }

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
      case 'pathToPolygon => PathToPolygon(latch, cmdBool)
      case 'pathToPGram => PathToPGram(latch, cmdBool)
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

  def pathToPolygon(): DynamicShape = {
    getWorker('pathToPolygon)
    geomObj
  }

  def pathToParallelogram(): DynamicShape = {
    getWorker('pathToPGram)
    geomObj
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
            pen.endMove(pf.x.toFloat, pf.y.toFloat)
            changePos(pf.x, pf.y)
            turtle.repaint()
            doneFn()
          }
        })

      canvas.getRoot.addActivity(lineAnimation)
    }
  }

  def realTurn(angle: Double) {
    realWorker { doneFn =>
      var newTheta = theta + deg2radians(angle)
      if (newTheta < 0) newTheta = newTheta % (2*Math.Pi) + 2*Math.Pi
      else if (newTheta > 2*Math.Pi) newTheta = newTheta % (2*Math.Pi)
      changeHeading(newTheta)
      turtle.repaint()
      doneFn()
    }
  }

  def realClear() {
    realWorker { doneFn =>
      pen.clear()
      layer.removeAllChildren() // get rid of stuff not written by pen, like text nodes
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
    }
  }

  def realPenUp() {
    pen = UpPen
    CommandActor ! CommandDone
  }

  def realPenDown() {
    if (pen != DownPen) {
      pen = DownPen
      pen.updatePosition()
    }
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
    else if (Utils.doublesEqual(delY,0,0.001)) {
      if (delX > 0) 0
      else Math.Pi
    }
    else {
      var nt2 = Math.atan(delY/delX)
      if (delX < 0 && delY > 0) nt2 += Math.Pi
      else if (delX < 0 && delY < 0) nt2 += Math.Pi
      else if (delX > 0 && delY < 0) nt2 += 2* Math.Pi
      nt2
    }

    realWorker { doneFn =>
      changeHeading(newTheta)
      turtle.repaint()
      doneFn()
    }
  }

  def realJumpTo(x: Double, y: Double) {
    realWorker { doneFn =>
      changePos(x, y)
      pen.updatePosition()
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

  def realWrite(text: String) {
    realWorker { doneFn =>
      val ptext = new PText(text)
      history.push(UndoWrite(ptext))
      ptext.getTransformReference(true).setToScale(1, -1)
      ptext.setOffset(_position.x, _position.y)
      layer.addChild(layer.getChildrenCount-1, ptext)
      ptext.repaint()
      doneFn()
    }
  }

  def realHide() {
    realWorker { doneFn =>
      if (turtle.getChildrenReference.contains(turtleImage)) {
        turtle.removeChild(turtleImage)
        turtle.repaint()
      }
      doneFn()
    }
  }

  def realShow() {
    realWorker { doneFn =>
      if (!turtle.getChildrenReference.contains(turtleImage)) {
        turtle.addChild(turtleImage)
        turtle.repaint()
      }
      doneFn()
    }
  }

  def realPoint(x: Double, y: Double) {
    realJumpTo(x, y)
    CommandActor.receive {
      case CommandDone =>
    }
    realWorker { doneFn =>
      pen.startMove(x.toFloat, y.toFloat)
      pen.endMove(x.toFloat, y.toFloat)
      doneFn()
    }
  }

  def realPathToPolygon() {
    realWorker { doneFn =>
      geomObj = null
      history.clear()
      try {
        val pgon = new PolygonConstraint(penPaths.last, Geometer.handleLayer, canvas.outputFn)
        pgon.addHandles()
        pgon.repaint()
        geomObj = new DynamicShapeImpl(pgon)
        pen.addNewPath()
      }
      catch {
        case e: IllegalArgumentException => canvas.outputFn(e.getMessage)
      }
      doneFn()
    }
  }

  def realPathToPGram() {
    realWorker { doneFn =>
      geomObj = null
      history.clear()
      try {
        val pgon = new PGramConstraint(penPaths.last, Geometer.handleLayer, canvas.outputFn)
        pgon.addHandles()
        pgon.repaint()
        geomObj = new DynamicShapeImpl(pgon)
        pen.addNewPath()
      }
      catch {
        case e: IllegalArgumentException => canvas.outputFn(e.getMessage)
      }
      doneFn()
    }
  }

  def realUndoChangeInPos(oldPos: (Double, Double)) {
    pen.undoMove()
    changePos(oldPos._1, oldPos._2)
    turtle.repaint()
  }

  def realUndoJump(oldPos: (Double, Double)) {
    pen.removeLastPath()
    changePos(oldPos._1, oldPos._2)
    turtle.repaint()
  }

  def realUndoChangeInHeading(oldHeading: Double) {
    changeHeading(oldHeading)
    turtle.repaint()
  }

  def realUndoPenAttrs(color: Color, thickness: Double, fillColor: Color) {
    canvas.outputFn("Undoing Pen attribute (Color/Thickness/FillColor) change.\n")
    pen.removeLastPath()
    pen.rawSetAttrs(color, thickness, fillColor)
  }

  def realUndoPenState(apen: Pen) {
    canvas.outputFn("Undoing Pen State (Up/Down) change.\n")
    apen match {
      case UpPen =>
        pen = UpPen
        pen.removeLastPath()
      case DownPen =>
        pen = DownPen
    }
  }

  def realUndoWrite(ptext: PText) {
    layer.removeChild(ptext)
  }

  def resetRotation() {
    changeHeading(deg2radians(90))
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
    val downPen = new DownPen()
    val upPen = new UpPen()
    (downPen, upPen)
  }

  def makeCommandProcessor() = actor {

    def processGetCommand(cmd: Command, latch: CountDownLatch)(fn: => Unit) {
      processCommand(cmd)(fn)
      latch.countDown()
    }

    def processCommand(cmd: Command, undoCmd: Option[UndoCommand] = None)(fn: => Unit) {
      if (cmd.valid.get) {
        listener.hasPendingCommands
        listener.commandStarted(cmd)

        if (undoCmd.isDefined) history.push(undoCmd.get)

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

    def undoHandler: PartialFunction[UndoCommand, Unit] = {
      case cmd @ UndoChangeInPos((x, y)) =>
        realUndoChangeInPos((x, y))
      case cmd @ UndoJump((x, y)) =>
        realUndoJump((x, y))
      case cmd @ UndoChangeInHeading(oldHeading) =>
        realUndoChangeInHeading(oldHeading)
      case cmd @ UndoPenAttrs(color, thickness, fillColor) =>
        realUndoPenAttrs(color, thickness, fillColor)
      case cmd @ UndoPenState(apen) =>
        realUndoPenState(apen)
      case cmd @ UndoWrite(ptext) =>
        realUndoWrite(ptext)
      case cmd @ CompositeCommand(cmds) =>
        realHandleCompositeCommand(cmds)
    }

    def realUndo() {
      realWorker { doneFn =>
        if (!history.isEmpty) {
          val cmd = history.pop()
          Log.info("Popped command from history: " + cmd)
          undoHandler(cmd)
        }
        doneFn()
      }
    }

    def realHandleCompositeCommand(cmds: scala.List[UndoCommand]) {
      cmds.foreach {cmd => undoHandler(cmd)}
    }

    var done = false

    loopWhile(!done) {
      react {
        case Stop =>
          done = true
        case cmd @ Forward(n, b) =>
          processCommand(cmd, Some(UndoChangeInPos((_position.x, _position.y)))) {
            realForward(n)
          }
        case cmd @ Turn(angle, b) =>
          processCommand(cmd, Some(UndoChangeInHeading(theta))) {
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
          processCommand(cmd, Some(UndoPenState(pen))) {
            realPenUp
          }
        case cmd @ PenDown(b) =>
          processCommand(cmd, Some(UndoPenState(pen))) {
            realPenDown
          }
        case cmd @ Towards(x, y, b) =>
          processCommand(cmd, Some(UndoChangeInHeading(theta))) {
            realTowards(x, y)
          }
        case cmd @ JumpTo(x, y, b) =>
          val undoCmd = 
            if (pen == UpPen)
              CompositeCommand(
              scala.List(
                UndoPenState(pens._2),
                UndoChangeInPos((_position.x, _position.y))
              )
            )
          else 
            CompositeCommand(
              scala.List(
                UndoPenState(pens._2),
                UndoChangeInPos((_position.x, _position.y)),
                UndoPenState(pens._1)
              )
            )
          processCommand(cmd, Some(undoCmd)) {
            realJumpTo(x, y)
          }
        case cmd @ MoveTo(x, y, b) =>
          val undoCmd = CompositeCommand(
            scala.List(
              UndoChangeInPos((_position.x, _position.y)),
              UndoChangeInHeading(theta)
            )
          )
          processCommand(cmd, Some(undoCmd)) {
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
          processCommand(cmd, Some(UndoPenAttrs(pen.getColor, pen.getThickness, pen.getFillColor))) {
            realSetPenColor(color)
          }
        case cmd @ SetPenThickness(t, b) =>
          processCommand(cmd, Some(UndoPenAttrs(pen.getColor, pen.getThickness, pen.getFillColor))) {
            realSetPenThickness(t)
          }
        case cmd @ SetFillColor(color, b) =>
          processCommand(cmd, Some(UndoPenAttrs(pen.getColor, pen.getThickness, pen.getFillColor))) {
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
        case cmd @ Write(text, b) =>
          processCommand(cmd) {
            realWrite(text)
          }
        case cmd @ Show(b) =>
          processCommand(cmd) {
            realShow()
          }
        case cmd @ Hide(b) =>
          processCommand(cmd) {
            realHide()
          }
        case cmd @ Point(x, y, b) =>
          processCommand(cmd) {
            realPoint(x, y)
          }
        case cmd @ PathToPolygon(l, b) =>
          processGetCommand(cmd, l) {
            realPathToPolygon()
          }
        case cmd @ PathToPGram(l, b) =>
          processGetCommand(cmd, l) {
            realPathToPGram()
          }
        case cmd @ Undo() =>
          processCommand(cmd) {
            realUndo()
          }
      }
    }
  }

  abstract class AbstractPen extends Pen {
    val Log = Logger.getLogger(getClass.getName);

    val sprite = Geometer.this
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

    def newPath(): PolyLine = {
      val penPath = new PolyLine()
      penPath.addPoint(sprite._position.x.toFloat, sprite._position.y.toFloat)
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

    def removeLastPath() {
      val penPath = penPaths.last
      penPaths.remove(penPaths.size-1)
      layer.removeChild(penPath)
    }

    def getColor = lineColor
    def getFillColor = fillColor
    def getThickness = lineStroke.asInstanceOf[BasicStroke].getLineWidth

    def rawSetAttrs(color: Color, thickness: Double, fColor: Color) {
      lineColor = color
      lineStroke = new BasicStroke(thickness.toFloat, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
      fillColor = fColor
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
      penPaths.clear()
      history.clear()
    }

    def updatePosition() = {
      addNewPath()
    }
  }

  class UpPen extends AbstractPen {
    def startMove(x: Float, y: Float) {}
    def move(x: Float, y: Float) {}
    def endMove(x: Float, y: Float) {
//      penPaths.last.moveTo(x, y)
    }
    def undoMove() {}
  }

  class DownPen extends AbstractPen {
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

    def undoMove() {
      penPaths.last.removeLastPoint()
      penPaths.last.repaint()
    }
  }

  class DynamicShapeImpl(pgon: GeometricConstraint) extends DynamicShape {
    def showAngles() = pgon.showAngles()
    def hideAngles() = pgon.hideAngles()
    def showLengths() = pgon.showLengths()
    def hideLengths() = pgon.hideLengths()
    def trackVars(fn: scala.collection.mutable.Map[String, Float] => Unit) = pgon.trackVars(fn)
  }
}