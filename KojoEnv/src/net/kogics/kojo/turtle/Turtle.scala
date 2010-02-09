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
package turtle

import javax.swing._
import java.awt.{Point => _, _}
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
import net.kogics.kojo.kgeom._
import net.kogics.kojo.core._

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{CountDownLatch, TimeUnit}

object Turtle {
  val handleLayer = new PLayer
  val writeFont = new Font(new PText().getFont.getName, Font.PLAIN, 15)
}

class Turtle(canvas: SpriteCanvas, fname: String, initX: Double = 0d,
             initY: Double = 0, bottomLayer: Boolean = false) extends core.Turtle {
  private val Log = Logger.getLogger(getClass.getName)
  Log.info("Turtle being created in thread: " + Thread.currentThread.getName)

  private val layer = new PLayer
  private val camera = canvas.getCamera
  // the zeroth layer is for the grid etc
  // bottom sprite layer is at index 1
  if (bottomLayer) camera.addLayer(1, layer) else camera.addLayer(camera.getLayerCount-1, layer)
  @volatile private var _animationDelay = 0l

  private val turtleImage = new PImage(Utils.loadImage(fname))
  private val turtle = new PNode
  turtleImage.getTransformReference(true).setToScale(1, -1)
  turtleImage.setOffset(-16, 16)

  private val xBeam = PPath.createLine(0, 30, 0, -30)
  xBeam.setStrokePaint(Color.gray)
  private val yBeam = PPath.createLine(-20, 0, 50, 0)
  yBeam.setStrokePaint(Color.gray)

  private val penPaths = new mutable.ArrayBuffer[PolyLine]
  @volatile private var lineColor: Color = _
  @volatile private var fillColor: Color = _
  @volatile private var lineStroke: Stroke = _

  private val pens = makePens
  private val DownPen = pens._1
  private val UpPen = pens._2
  @volatile private var pen: Pen = _

  @volatile private var _position: Point2D.Double = _
  @volatile private var theta: Double = _
  @volatile private var removed: Boolean = false

  private val CommandActor = makeCommandProcessor()
  @volatile private var geomObj: DynamicShape = _
  private val history = new mutable.Stack[UndoCommand]
  @volatile private var isVisible: Boolean = _
  @volatile private var areBeamsOn: Boolean = _

  private def changePos(x: Double, y: Double) {
    _position = new Point2D.Double(x, y)
    turtle.setOffset(x, y)
  }

  private def changeHeading(newTheta: Double) {
    theta = newTheta
    turtle.setRotation(theta)
  }

  def dumpState() {
    Utils.runInSwingThread {
      val output = canvas.outputFn
      val cIter = layer.getChildrenReference.iterator
      output("Turtle Layer (%d children):\n" format(layer.getChildrenReference.size))
      while (cIter.hasNext) {
        val node = cIter.next.asInstanceOf[PNode]
        output(stringRep(node))
      }
    }
  }

  private def stringRep(node: PNode): String = node match {
    case l: PolyLine => 
      new StringBuilder().append("  Polyline:\n").append("    Points: %s\n" format l.points).toString
    case n: PNode =>
      new StringBuilder().append("  PNode:\n").append("    Children: %s\n" format n.getChildrenReference).toString
  }

  private def clearHistory() = history.clear()

  private def pushHistory(cmd: UndoCommand) {
    canvas.pushHistory(this)
    history.push(cmd)
  }

  private def popHistory(): UndoCommand = {
    canvas.popHistory()
    history.pop()
  }

  private [turtle] def init() {
    _animationDelay = 1000l
    clearHistory()
    changePos(initX, initY)
    layer.addChild(turtle)

    pen = DownPen
    pen.init
    resetRotation

    showWorker()
    beamsOffWorker()
  }

  init

  @volatile private var cmdBool = new AtomicBoolean(true)
  @volatile private var listener: TurtleListener = NoopTurtleListener

  private [turtle] def deg2radians(angle: Double) = angle * Math.Pi / 180
  private [turtle] def rad2degrees(angle: Double) = angle * 180 / Math.Pi
  private [turtle] def thetaDegrees = rad2degrees(theta)
  private [turtle] def thetaRadians = theta

  private def enqueueCommand(cmd: Command) {
    if (removed) return
    CommandActor ! cmd
    Throttler.throttle()
  }

  def syncUndo() {
    undo()
    // wait for undo to get done by reading animation delay value synchronously
    animationDelay
  }

  def undo() = enqueueCommand(Undo)
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
  def invisible() = enqueueCommand(Hide(cmdBool))

  def remove() = {
    enqueueCommand(Remove(cmdBool))
    removed = true
  }

  private def getWorker(action: Symbol) {
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

  def position: Point = {
    getWorker('position)
    new Point(_position.getX, _position.getY)
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

  // invoke fn in GUI thread, supplying it doneFn to call at the end,
  // and wait for doneFn to get called
  // Kinda like SwingUtilities.invokeAndWait, except that it uses actor messages
  // and blocks inside actor.receive() - to give the actor thread pool a chance
  // to grow with the number of turtles
  private def realWorker(fn: (() => Unit) => Unit) {
    Utils.runInSwingThread {
      fn {() => workDone()}
    }
    waitForDoneMsg()
  }

  // invoke fn in GUI thread, and call doneFn after it is done
  private def realWorker2(fn:  => Unit) {
    realWorker { doneFn =>
      fn
      doneFn()
    }
  }

  private def workDone() {
    CommandActor ! CommandDone
  }

  private def waitForDoneMsg() {
    CommandActor.receive {
      case CommandDone =>
    }
  }

  // real* methods are called in the Agent thread
  // realWorker allows them to do work in the GUI thread
  // but they need to call doneFn() at the end to carry on
  // after the GUI thread is done - because the processCommand method
  // waits for a CommandDone msg after calling the work funtion -
  // and doneFn sends this msg to the actor from the GUI thread
  // In an earlier version of the code, a latch was used to synchronize between the
  // GUI and actor threads. But after a certain Scala 2.8.0 nightly build, this
  // resulted in thread starvation in the Actor thread-pool
  
  private def realForward(n: Double) {
    val p0 = _position
    val delX = Math.cos(theta) * n
    val delY = Math.sin(theta) * n
    val pf = new Point2D.Double(p0.x + delX, p0.y + delY)

    realWorker { doneFn =>

      def endMove() {
        pen.endMove(pf.x.toFloat, pf.y.toFloat)
        changePos(pf.x, pf.y)
        turtle.repaint()
        doneFn()
      }

      if (Utils.doublesEqual(n, 0, 0.001)) {
        doneFn()
      }
      else if (_animationDelay < 10) {
        endMove()
      }
      else {
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
              endMove()
            }
          })

        canvas.getRoot.addActivity(lineAnimation)
      }
    }
  }

  private def realTurn(angle: Double) {
    realWorker2 {
      var newTheta = theta + deg2radians(angle)
      if (newTheta < 0) newTheta = newTheta % (2*Math.Pi) + 2*Math.Pi
      else if (newTheta > 2*Math.Pi) newTheta = newTheta % (2*Math.Pi)
      changeHeading(newTheta)
      turtle.repaint()
    }
  }

  private def realClear() {
    realWorker2 {
      pen.clear()
      layer.removeAllChildren() // get rid of stuff not written by pen, like text nodes
      init()
      turtle.repaint()
      canvas.afterClear()
    }
  }

  private def realRemove() {
    realWorker2 {
      pen.clear
      layer.removeChild(turtle)
      camera.removeLayer(layer)
    }
  }

  private def realPenUp() {
    realWorker2 {
      pen = UpPen
    }
  }

  private def realPenDown() {
    realWorker2 {
      if (pen != DownPen) {
        pen = DownPen
        pen.updatePosition()
      }
    }
  }

  private def realTowards(x: Double, y: Double) {
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

    realWorker2 {
      changeHeading(newTheta)
      turtle.repaint()
    }
  }

  private def realJumpTo(x: Double, y: Double) {
    realWorker2 {
      changePos(x, y)
      pen.updatePosition()
      turtle.repaint()
    }
  }

  private def realMoveTo(x: Double, y: Double) {
    def distanceTo(x: Double, y: Double): Double = {
      val (x0,y0) = (_position.x, _position.y)
      val delX = Math.abs(x-x0)
      val delY = Math.abs(y-y0)
      Math.sqrt(delX * delX + delY * delY)
    }

    realTowards(x, y)
    realForward(distanceTo(x,y))
  }

  private def realSetAnimationDelay(d: Long) {
    _animationDelay = d
  }

  private def realGetWorker() {
  }

  private def realSetPenColor(color: Color) {
    realWorker2 {
      pen.setColor(color)
    }
  }

  private def realSetPenThickness(t: Double) {
    realWorker2 {
      pen.setThickness(t)
    }
  }

  private def realSetFillColor(color: Color) {
    realWorker2 {
      pen.setFillColor(color)
    }
  }

  private def beamsOnWorker() {
    if (!areBeamsOn) {
      turtle.addChild(0, xBeam)
      turtle.addChild(1, yBeam)
      turtle.repaint()
      areBeamsOn = true
    }
  }

  private def beamsOffWorker() {
    if (areBeamsOn) {
      turtle.removeChild(xBeam)
      turtle.removeChild(yBeam)
      turtle.repaint()
      areBeamsOn = false
    }
  }

  private def realBeamsOn() {
    realWorker2 {
      beamsOnWorker()
    }
  }

  private def realBeamsOff() {
    realWorker2 {
      beamsOffWorker()
    }
  }

  private def realWrite(text: String) {
    val ptext = new PText(text)
    pushHistory(UndoWrite(ptext))
    realWorker2 {
      ptext.getTransformReference(true).setToScale(1, -1)
      ptext.setOffset(_position.x, _position.y)
      ptext.setFont(Turtle.writeFont)
      layer.addChild(layer.getChildrenCount-1, ptext)
      ptext.repaint()
      turtle.repaint()
    }
  }

  private def realHide() {
    realWorker2 {
      hideWorker()
    }
  }

  private def realShow() {
    realWorker2 {
      showWorker()
    }
  }

  private def hideWorker() {
    if (isVisible) {
      turtle.removeChild(turtleImage)
      beamsOffWorker()
      turtle.repaint()
      isVisible = false
    }
  }

  private def showWorker() {
    if (!isVisible) {
      turtle.addChild(turtleImage)
      turtle.repaint()
      isVisible = true
    }
  }

  private def realPathToPolygon() {
    realWorker2 {
      geomObj = null
      clearHistory()
      try {
        val pgon = new PolygonConstraint(penPaths.last, Turtle.handleLayer, canvas.outputFn)
        pgon.addHandles()
        pgon.repaint()
        geomObj = new DynamicShapeImpl(pgon)
        pen.addNewPath()
      }
      catch {
        case e: IllegalArgumentException => canvas.outputFn(e.getMessage)
      }
    }
  }

  private def realPathToPGram() {
    realWorker2 {
      geomObj = null
      clearHistory()
      try {
        val pgon = new PGramConstraint(penPaths.last, Turtle.handleLayer, canvas.outputFn)
        pgon.addHandles()
        pgon.repaint()
        geomObj = new DynamicShapeImpl(pgon)
        pen.addNewPath()
      }
      catch {
        case e: IllegalArgumentException => canvas.outputFn(e.getMessage)
      }
    }
  }

  // undo methods are called in the GUI thread via realUndo
  private def undoChangeInPos(oldPos: (Double, Double)) {
    pen.undoMove()
    changePos(oldPos._1, oldPos._2)
    turtle.repaint()
  }

  private def undoChangeInHeading(oldHeading: Double) {
    changeHeading(oldHeading)
    turtle.repaint()
  }

  private def undoPenAttrs(color: Color, thickness: Double, fillColor: Color) {
    canvas.outputFn("Undoing Pen attribute (Color/Thickness/FillColor) change.\n")
    pen.removeLastPath()
    pen.rawSetAttrs(color, thickness, fillColor)
  }

  private def undoPenState(apen: Pen) {
    canvas.outputFn("Undoing Pen State (Up/Down) change.\n")
    apen match {
      case UpPen =>
        pen = UpPen
        pen.removeLastPath()
      case DownPen =>
        pen = DownPen
    }
  }

  private def undoWrite(ptext: PText) {
    layer.removeChild(ptext)
  }

  private def undoVisibility(visible: Boolean, beamsOn: Boolean) {
    if (visible) showWorker()
    else hideWorker()

    if (beamsOn) beamsOnWorker()
    else beamsOffWorker()
  }

  private [turtle] def resetRotation() {
    changeHeading(deg2radians(90))
  }

  private [kojo] def stop() {
    cmdBool.set(false)
    cmdBool = new AtomicBoolean(true)
  }

  private [kojo] def setTurtleListener(l: TurtleListener) {
//    if (listener != NoOpListener) throw new RuntimeException("Cannot re-set Turtle listener")
    listener = l
  }

  private def makePens(): (Pen, Pen) = {
    val downPen = new DownPen()
    val upPen = new UpPen()
    (downPen, upPen)
  }

  private def makeCommandProcessor() = actor {

    def processGetCommand(cmd: Command, latch: CountDownLatch)(fn: => Unit) {
      processCommand(cmd)(fn)
      latch.countDown()
    }

    def processCommand(cmd: Command, undoCmd: Option[UndoCommand] = None)(fn: => Unit) {
//      Log.info("Command Being Processed: %s." format(cmd))
      if (cmd.valid.get) {
        listener.hasPendingCommands
        listener.commandStarted(cmd)

        if (undoCmd.isDefined) pushHistory(undoCmd.get)

        try {
          fn
        }
//        catch {
//          case t: Throwable =>
//            canvas.outputFn("Problem: " + t.toString())
//        }
        finally {
          listener.commandDone(cmd)
        }
      }
      else {
        listener.commandDiscarded(cmd)
      }
//      Log.info("Command Handled: %s. Mailbox size: %d" format(cmd, mailboxSize))
      if (mailboxSize == 0) listener.pendingCommandsDone
    }

    def undoHandler: PartialFunction[UndoCommand, Unit] = {
      case cmd @ UndoChangeInPos((x, y)) =>
        undoChangeInPos((x, y))
      case cmd @ UndoChangeInHeading(oldHeading) =>
        undoChangeInHeading(oldHeading)
      case cmd @ UndoPenAttrs(color, thickness, fillColor) =>
        undoPenAttrs(color, thickness, fillColor)
      case cmd @ UndoPenState(apen) =>
        undoPenState(apen)
      case cmd @ UndoWrite(ptext) =>
        undoWrite(ptext)
      case cmd @ UndoVisibility(visible, areBeamsOn) =>
        undoVisibility(visible, areBeamsOn)
      case cmd @ CompositeUndoCommand(cmds) =>
        handleCompositeCommand(cmds)
    }

    def realUndo() {
      if (!history.isEmpty) {
        val cmd = popHistory()
        realWorker2 {
          undoHandler(cmd)
        }
      }
    }

    def handleCompositeCommand(cmds: scala.List[UndoCommand]) {
      cmds.foreach {cmd => undoHandler(cmd)}
    }

    loop {
      react {
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
          exit()
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
              UndoChangeInPos((_position.x, _position.y))
          else
            CompositeUndoCommand(
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
          val undoCmd = CompositeUndoCommand(
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
          processCommand(cmd, Some(UndoVisibility(isVisible, areBeamsOn))) {
            realShow()
          }
        case cmd @ Hide(b) =>
          processCommand(cmd, Some(UndoVisibility(isVisible, areBeamsOn))) {
            realHide()
          }
        case cmd @ PathToPolygon(l, b) =>
          processGetCommand(cmd, l) {
            realPathToPolygon()
          }
        case cmd @ PathToPGram(l, b) =>
          processGetCommand(cmd, l) {
            realPathToPGram()
          }
        case cmd @ Undo =>
          processCommand(cmd) {
            realUndo()
          }
      }
    }
  }

  abstract class AbstractPen extends Pen {
    val Log = Logger.getLogger(getClass.getName);

    val turtle = Turtle.this
    val DefaultColor = Color.red
    val DefaultFillColor = null
    val DefaultStroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)

    def init() {
      lineColor = DefaultColor
      fillColor = DefaultFillColor
      lineStroke = DefaultStroke
      addNewPath()
    }

    def newPath(): PolyLine = {
      val penPath = new PolyLine()
      penPath.addPoint(turtle._position.x.toFloat, turtle._position.y.toFloat)
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
    }
  }

  class UpPen extends AbstractPen {
    def startMove(x: Float, y: Float) {}
    def move(x: Float, y: Float) {}
    def endMove(x: Float, y: Float) {}
    def updatePosition() {}
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

    def updatePosition() {
      addNewPath()
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