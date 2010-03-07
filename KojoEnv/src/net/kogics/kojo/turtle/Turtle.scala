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
  @volatile private [turtle] var _animationDelay = 0l

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

  private [turtle] def changePos(x: Double, y: Double) {
    _position = new Point2D.Double(x, y)
    turtle.setOffset(x, y)
  }

  private def changeHeading(newTheta: Double) {
    theta = newTheta
    turtle.setRotation(theta)
  }

  def distanceTo(x: Double, y: Double): Double = {
    val (x0,y0) = (_position.x, _position.y)
    val delX = Math.abs(x-x0)
    val delY = Math.abs(y-y0)
    Math.sqrt(delX * delX + delY * delY)
  }

  def delayFor(dist: Double): Long = {
    if (_animationDelay < 1) {
      return _animationDelay
    }
    
    // _animationDelay is delay for 100 steps;
    // Here we calculate delay for specified distance
    val speed = 100f / _animationDelay
    val delay = dist / speed
    delay.round
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

  private def thetaDegrees = Utils.rad2degrees(theta)
  private def thetaRadians = theta

  private def enqueueCommand(cmd: Command) {
    if (removed) return
    listener.hasPendingCommands
    listener.commandStarted(cmd)
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
  def setPenColor(color: Color) = enqueueCommand(SetPenColor(color, cmdBool))
  def setFillColor(color: Color) = enqueueCommand(SetFillColor(color, cmdBool))
  def beamsOn() = enqueueCommand(BeamsOn(cmdBool))
  def beamsOff() = enqueueCommand(BeamsOff(cmdBool))
  def write(text: String) = enqueueCommand(Write(text, cmdBool))
  def visible() = enqueueCommand(Show(cmdBool))
  def invisible() = enqueueCommand(Hide(cmdBool))
  def setAnimationDelay(d: Long) = {
    if (d < 0) {
      throw new IllegalArgumentException("Negative delay not allowed")
    }
    enqueueCommand(SetAnimationDelay(d, cmdBool))
  }
  def setPenThickness(t: Double) = {
    if (t < 0) {
      throw new IllegalArgumentException("Negative thickness not allowed")
    }
    enqueueCommand(SetPenThickness(t, cmdBool))
  }

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
      case 'state => GetState(latch, cmdBool)
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

  def state: SpriteState = {
    def textNodes: scala.List[PText] = {
      var nodes: scala.List[PText] = Nil
      val iter = layer.getChildrenIterator
      while (iter.hasNext) {
        val child = iter.next
        if (child.isInstanceOf[PText]) {
          nodes = child.asInstanceOf[PText] :: nodes
        }
      }
      nodes
    }

    import java.lang.{Double => JDouble}

    getWorker('state)
    SpriteState(JDouble.doubleToLongBits(_position.x), JDouble.doubleToLongBits(_position.y),
                JDouble.doubleToLongBits(thetaDegrees),
                pen.getColor, JDouble.doubleToLongBits(pen.getThickness), pen.getFillColor,
                pen,
                textNodes,
                isVisible, areBeamsOn)
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

  private def realWorker3(cmd: Command)(fn: (() => Unit) => Unit) {
    Utils.runInSwingThread {
      fn {() => asyncCmdDone(cmd)}
    }
  }

  private def realWorker4(cmd: Command)(fn:  => Unit) {
    realWorker3 (cmd) { doneFn =>
      fn
      doneFn()
    }
  }

  private def asyncCmdDone(cmd: Command) {
    listener.commandDone(cmd)
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
  
  private def realForwardCustom(n: Double, cmd: Command) {

    def saveUndoCmd() {
      pushHistory(UndoChangeInPos((_position.x, _position.y)))
    }

    def newPoint = {
      val p0 = _position
      val delX = Math.cos(theta) * n
      val delY = Math.sin(theta) * n
      new Point2D.Double(p0.x + delX, p0.y + delY)
    }

    def endMove(pf: Point2D.Double) {
      pen.endMove(pf.x, pf.y)
      changePos(pf.x, pf.y)
      turtle.repaint()
    }

    if (Utils.doublesEqual(n, 0, 0.001)) {
      asyncCmdDone(cmd)
      return
    }

    val aDelay = delayFor(n)

    if (aDelay < 10) {
      if (aDelay > 1) {
        Thread.sleep(aDelay)
      }

      realWorker4(cmd) {
        saveUndoCmd()
        val pf = newPoint
        endMove(pf)
      }
    }
    else {
      realWorker { doneFn =>
        def manualEndMove(pt: Point2D.Double) {
          endMove(pt)
          asyncCmdDone(cmd)
          doneFn()
        }
        saveUndoCmd()
        val p0 = _position
        var pf = newPoint
        pen.startMove(p0.x, p0.y)

        val lineAnimation = new PActivity(aDelay) {
          override def activityStep(elapsedTime: Long) {
            val frac = elapsedTime.toDouble / aDelay
            val currX = p0.x * (1-frac) + pf.x * frac
            val currY = p0.y * (1-frac) + pf.y * frac
            if (cmd.valid.get) {
              pen.move(currX, currY)
              turtle.setOffset(currX, currY)
              turtle.repaint()
            }
            else {
              pf = new Point2D.Double(currX, currY)
              terminate()
            }
          }
        }

        lineAnimation.setDelegate(new PActivityDelegate {
            override def activityStarted(activity: PActivity) {}
            override def activityStepped(activity: PActivity) {}
            override def activityFinished(activity: PActivity) {
              manualEndMove(pf)
            }
          })

        canvas.getRoot.addActivity(lineAnimation)
      }
    }
  }

  private def realTurn(angle: Double, cmd: Command) {
    pushHistory(UndoChangeInHeading(theta))
    var newTheta = theta + Utils.deg2radians(angle)
    if (newTheta < 0) newTheta = newTheta % (2*Math.Pi) + 2*Math.Pi
    else if (newTheta > 2*Math.Pi) newTheta = newTheta % (2*Math.Pi)
    changeHeading(newTheta)
    turtle.repaint()
  }

  private def realClear() {
    pen.clear()
    layer.removeAllChildren() // get rid of stuff not written by pen, like text nodes
    init()
    turtle.repaint()
    canvas.afterClear()
  }

  private def realRemove() {
    pen.clear
    layer.removeChild(turtle)
    camera.removeLayer(layer)
  }

  private def realPenUp(cmd: Command) {
    pushHistory(UndoPenState(pen))
    pen = UpPen
  }

  private def realPenDown(cmd: Command) {
    if (pen != DownPen) {
      pushHistory(UndoPenState(pen))
      pen = DownPen
      pen.updatePosition()
    }
  }

  private def towardsHelper(x: Double, y: Double): Double = {
    val (x0, y0) = (_position.x, _position.y)
    val delX = x - x0
    val delY = y - y0
    if (Utils.doublesEqual(delX,0,0.001)) {
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
  }

  private def realTowards(x: Double, y: Double, cmd: Command) {
    pushHistory(UndoChangeInHeading(theta))
    val newTheta = towardsHelper(x, y)
    changeHeading(newTheta)
    turtle.repaint()
  }

  private def realJumpTo(x: Double, y: Double, cmd: Command) {

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
    pushHistory(undoCmd)

    changePos(x, y)
    pen.updatePosition()
    turtle.repaint()
  }

  private def realMoveToCustom(x: Double, y: Double, cmd: Command) {
    def undoCmd = CompositeUndoCommand(
      scala.List(
        UndoChangeInPos((_position.x, _position.y)),
        UndoChangeInHeading(theta)
      )
    )

    realWorker2 {
      pushHistory(undoCmd)
      val newTheta = towardsHelper(x, y)
      changeHeading(newTheta)
    }
    realForwardCustom(distanceTo(x,y), cmd)
  }

  private def realSetAnimationDelay(d: Long, cmd: Command) {
    _animationDelay = d
  }

  private def realGetWorker() {
  }

  private def realSetPenColor(color: Color, cmd: Command) {
    pushHistory(UndoPenAttrs(pen.getColor, pen.getThickness, pen.getFillColor))
    pen.setColor(color)
  }

  private def realSetPenThickness(t: Double, cmd: Command) {
    pushHistory(UndoPenAttrs(pen.getColor, pen.getThickness, pen.getFillColor))
    pen.setThickness(t)
  }

  private def realSetFillColor(color: Color, cmd: Command) {
    pushHistory(UndoPenAttrs(pen.getColor, pen.getThickness, pen.getFillColor))
    pen.setFillColor(color)
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

  private def realBeamsOn(cmd: Command) {
    beamsOnWorker()
  }

  private def realBeamsOff(cmd: Command) {
    beamsOffWorker()
  }

  private def realWrite(text: String, cmd: Command) {
    val ptext = new PText(text)
    pushHistory(UndoWrite(ptext))
    ptext.getTransformReference(true).setToScale(1, -1)
    ptext.setOffset(_position.x, _position.y)
    ptext.setFont(Turtle.writeFont)
    layer.addChild(layer.getChildrenCount-1, ptext)
    ptext.repaint()
    turtle.repaint()
  }

  private def realHide(cmd: Command) {
    pushHistory(UndoVisibility(isVisible, areBeamsOn))
    hideWorker()
  }

  private def realShow(cmd: Command) {
    pushHistory(UndoVisibility(isVisible, areBeamsOn))
    showWorker()
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
    changeHeading(Utils.deg2radians(90))
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

    val throttler = new Throttler(1)

    def processGetCommand(cmd: Command, latch: CountDownLatch)(fn: => Unit) {
      processCommandSync(cmd)(fn)
      latch.countDown()
    }

    def processCommandSync(cmd: Command)(fn: => Unit) {
//      Log.info("Command Being Processed: %s." format(cmd))
      if (cmd.valid.get) {
        throttler.throttle()

        try {
          realWorker2 {
            fn
          }
        }
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

    def processCommand(cmd: Command)(fn: => Unit) {
      if (cmd.valid.get) {
        throttler.throttle()

        realWorker4(cmd) {
          fn
        }
      }
      else {
        listener.commandDiscarded(cmd)
      }
    }

    def processCommandCustom(cmd: Command)(fn: => Unit) {
      if (cmd.valid.get) {
        throttler.throttle()

        fn
      }
      else {
        listener.commandDiscarded(cmd)
      }
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

    def realUndoCustom(undoCmd: Command) {
      realWorker4(undoCmd) {
        if (!history.isEmpty) {
          val cmd = popHistory()
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
          processCommandCustom(cmd) {
            realForwardCustom(n, cmd)
          }
        case cmd @ Turn(angle, b) =>
          processCommand(cmd) {
            realTurn(angle, cmd)
          }
        case cmd @ Clear(b) =>
          processCommandSync(cmd) {
            realClear
          }
        case cmd @ Remove(b) =>
          processCommandSync(cmd) {
            realRemove
          }
          exit()
        case cmd @ PenUp(b) =>
          processCommand(cmd) {
            realPenUp(cmd)
          }
        case cmd @ PenDown(b) =>
          processCommand(cmd) {
            realPenDown(cmd)
          }
        case cmd @ Towards(x, y, b) =>
          processCommand(cmd) {
            realTowards(x, y, cmd)
          }
        case cmd @ JumpTo(x, y, b) =>
          processCommand(cmd) {
            realJumpTo(x, y, cmd)
          }
        case cmd @ MoveTo(x, y, b) =>
          processCommandCustom(cmd) {
            realMoveToCustom(x, y, cmd)
          }
        case cmd @ SetAnimationDelay(d, b) =>
          // block till delay is set to avoid race condition
          // in functions like realForward which look at
          // animation delay in the actor thread before deciding what to do
          processCommandSync(cmd) {
            realSetAnimationDelay(d, cmd)
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
            realSetPenColor(color, cmd)
          }
        case cmd @ SetPenThickness(t, b) =>
          processCommand(cmd) {
            realSetPenThickness(t, cmd)
          }
        case cmd @ SetFillColor(color, b) =>
          processCommand(cmd) {
            realSetFillColor(color, cmd)
          }
        case cmd @ BeamsOn(b) =>
          processCommand(cmd) {
            realBeamsOn(cmd)
          }
        case cmd @ BeamsOff(b) =>
          processCommand(cmd) {
            realBeamsOff(cmd)
          }
        case cmd @ Write(text, b) =>
          processCommand(cmd) {
            realWrite(text, cmd)
          }
        case cmd @ Show(b) =>
          processCommand(cmd) {
            realShow(cmd)
          }
        case cmd @ Hide(b) =>
          processCommand(cmd) {
            realHide(cmd)
          }
        case cmd @ Undo =>
          processCommandCustom(cmd) {
            realUndoCustom(cmd)
          }
        case cmd @ GetState(l, b) =>
          processGetCommand(cmd, l) {
            realGetWorker()
          }
      }
    }
  }

  abstract class AbstractPen extends Pen {
    val Log = Logger.getLogger(getClass.getName);

    val turtle = Turtle.this
    val RoundCap = BasicStroke.CAP_ROUND
    val ButtCap = BasicStroke.CAP_BUTT
    val Join = BasicStroke.JOIN_ROUND
    val DefaultColor = Color.red
    val DefaultFillColor = null
    val DefaultStroke = new BasicStroke(2, RoundCap, Join)

    def init() {
      lineColor = DefaultColor
      fillColor = DefaultFillColor
      lineStroke = DefaultStroke
      addNewPath()
    }

    def newPath(): PolyLine = {
      val penPath = new PolyLine()
      penPath.addPoint(turtle._position.x, turtle._position.y)
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
      val Cap = if (thickness < 1) ButtCap else RoundCap
      lineStroke = new BasicStroke(thickness.toFloat, Cap, Join)
      fillColor = fColor
    }

    def setColor(color: Color) {
      lineColor = color
      addNewPath()
    }

    def setThickness(t: Double) {
      val Cap = if (t < 1) ButtCap else RoundCap
      lineStroke = new BasicStroke(t.toFloat, Cap, Join)
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
    def startMove(x: Double, y: Double) {}
    def move(x: Double, y: Double) {}
    def endMove(x: Double, y: Double) {}
    def updatePosition() {}
    def undoMove() {}
  }

  class DownPen extends AbstractPen {
    var tempLine = new PPath
    val lineAnimationColor = Color.orange

    def startMove(x: Double, y: Double) {
      tempLine.setStroke(lineStroke)
      tempLine.setStrokePaint(lineAnimationColor)
      tempLine.moveTo(x.toFloat, y.toFloat)
      layer.addChild(layer.getChildrenCount-1, tempLine)
    }
    def move(x: Double, y: Double) {
      tempLine.lineTo(x.toFloat, y.toFloat)
      tempLine.repaint()
    }
    def endMove(x: Double, y: Double) {
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
}