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
import scala.{math => Math}

import net.kogics.kojo._
import net.kogics.kojo.util._
import net.kogics.kojo.kgeom._
import net.kogics.kojo.core._

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{CountDownLatch, TimeUnit}

class Turtle(canvas: SpriteCanvas, fname: String, initX: Double = 0d,
             initY: Double = 0, hidden: Boolean = false, bottomLayer: Boolean = false) extends core.Turtle {

  import TurtleHelper._

  private val Log = Logger.getLogger(getClass.getName)
//  Log.info("Turtle being created in thread: " + Thread.currentThread.getName)

  private val layer = new PLayer
  def tlayer: PLayer = layer
  private val camera = canvas.getCamera
  if (bottomLayer) camera.addLayer(0, layer) else camera.addLayer(layer)
  @volatile private [turtle] var _animationDelay = 0l

  private val turtleImage = new PImage(Utils.loadImage(fname))
  private val turtle = new PNode
  def camScale = canvas.camScale
  
  private val xBeam = PPath.createLine(0, 30, 0, -30)
  xBeam.setStrokePaint(Color.gray)
  private val yBeam = PPath.createLine(-20, 0, 50, 0)
  yBeam.setStrokePaint(Color.gray)

  private [kojo] val penPaths = new mutable.ArrayBuffer[PolyLine]
  @volatile private var lineColor: Color = _
  @volatile private var fillColor: Paint = _
  @volatile private var lineStroke: Stroke = _
  @volatile private var font: Font = _

  private val pens = makePens
  private val DownPen = pens._1
  private val UpPen = pens._2
  @volatile private[kojo] var pen: Pen = _

  @volatile private var _position: Point2D.Double = _
  @volatile private var theta: Double = _
  @volatile private var removed: Boolean = false

  @volatile private var geomObj: DynamicShape = _
  private val savedStyles = new mutable.Stack[Style]
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
    distance(_position.x, _position.y, x, y)
  }

  private def towardsHelper(x: Double, y: Double): Double = {
    thetaTowards(_position.x, _position.y, x, y, theta)
  }

  def delayFor(dist: Double): Long = {
    if (_animationDelay < 1) {
      return _animationDelay
    }
    
    // _animationDelay is delay for 100 steps;
    // Here we calculate delay for specified distance
    val speed = 100f / _animationDelay
    val delay = Math.abs(dist) / speed
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

  def initTImage() {
    turtleImage.getTransformReference(true).setToScale(1/camScale, 1/camScale)
    turtleImage.translate(-16, -16)
  }
  
  private [turtle] def init() {
    _animationDelay = 1000l
    changePos(initX, initY)
    initTImage()
    layer.addChild(turtle)

    pen = DownPen
    pen.init()
    resetRotation()

    if (hidden) {
      hideWorker()
    }
    else {
      showWorker()
    }
    beamsOffWorker()
  }

  init()

  @volatile private var listener: TurtleListener = NoopTurtleListener

  private def thetaDegrees = Utils.rad2degrees(theta)
  private def thetaRadians = theta

  def forward(n: Double) = realForward(n)
  def turn(angle: Double) = realTurn(angle)
  def clear() = realClear()
  
  def penUp() = realPenUp()
  def penDown() = realPenDown()
  def towards(x: Double, y: Double) = realTowards(x, y)
  def jumpTo(x: Double, y: Double) = realJumpTo(x, y)
  def moveTo(x: Double, y: Double) = realMoveTo(x, y)
  def setPenColor(color: Color) = realSetPenColor(color)
  def setFillColor(color: Paint) = realSetFillColor(color)
  def saveStyle() = realSaveStyle()
  def restoreStyle() = realRestoreStyle()
  def beamsOn() = realBeamsOn()
  def beamsOff() = realBeamsOff()
  def write(text: String) = realWrite(text)
  def visible() = realShow()
  def invisible() = realHide()
  def playSound(voice: Voice) = realPlaySound(voice)
  def setAnimationDelay(d: Long) = {
    if (d < 0) {
      throw new IllegalArgumentException("Negative delay not allowed")
    }
    realSetAnimationDelay(d)
  }
  def setPenThickness(t: Double) = {
    if (t < 0) {
      throw new IllegalArgumentException("Negative thickness not allowed")
    }
    realSetPenThickness(t)
  }
  def setPenFontSize(n: Int) = {
    if (n < 0) {
      throw new IllegalArgumentException("Negative font size not allowed")
    }
    realSetFontSize(n)
  }

  def remove() = {
    realRemove()
    removed = true
  }

  def animationDelay = _animationDelay

  def position: Point = Utils.runInSwingThreadAndWait {
    new Point(_position.getX, _position.getY)
  }

  def heading: Double = Utils.runInSwingThreadAndWait {
    thetaDegrees
  }

  def style: Style = Utils.runInSwingThreadAndWait {
    currStyle
  }

  private def currStyle = Style(pen.getColor, pen.getThickness, pen.getFillColor, pen.getFontSize)

  def state: SpriteState = Utils.runInSwingThreadAndWait {
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

    SpriteState(JDouble.doubleToLongBits(_position.x), JDouble.doubleToLongBits(_position.y),
                JDouble.doubleToLongBits(thetaDegrees),
                pen.getColor, JDouble.doubleToLongBits(pen.getThickness), pen.getFillColor,
                pen,
                textNodes,
                isVisible, areBeamsOn)
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
  
  private def realForward(n: Double): Unit = {
    def newPoint = {
      val p0 = _position
      val p1 = posAfterForward(p0.x, p0.y, theta, n)
      new Point2D.Double(p1._1, p1._2)
    }

    def endMove(pf: Point2D.Double) {
      pen.endMove(pf.x, pf.y)
      changePos(pf.x, pf.y)
      turtle.repaint()
    }

    if (Utils.doublesEqual(n, 0, 0.001)) {
      return
    }
    
    val aDelay = delayFor(n)

    if (aDelay < 10) {
      if (aDelay > 1) {
        Thread.sleep(aDelay)
      }
      Utils.runInSwingThread {
        val pf = newPoint
        endMove(pf)
      }
    }
    else {
      val latch = new CountDownLatch(1)
      Utils.runInSwingThread {
        def manualEndMove(pt: Point2D.Double) {
          endMove(pt)
          latch.countDown()
        }

        val p0 = _position
        var pf = newPoint
        pen.startMove(p0.x, p0.y)

        val lineAnimation = new PActivity(aDelay) {
          override def activityStep(elapsedTime: Long) {
            val frac = elapsedTime.toDouble / aDelay
            val currX = p0.x * (1-frac) + pf.x * frac
            val currY = p0.y * (1-frac) + pf.y * frac
            pen.move(currX, currY)
            turtle.setOffset(currX, currY)
            turtle.repaint()
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
      latch.await()
    }
  }

  private def realTurn(angle: Double) = Utils.runInSwingThread {
    val newTheta = thetaAfterTurn(angle, theta)
    changeHeading(newTheta)
    turtle.repaint()
  }

  private def realClear() = Utils.runInSwingThread {
    pen.clear()
    layer.removeAllChildren() // get rid of stuff not written by pen, like text nodes
    init()
    // showWorker() - if we want an invisible turtle to show itself after a clear
    turtle.repaint()
    canvas.afterClear()
  }

  private def realRemove() {
    pen.clear
    layer.removeChild(turtle)
    camera.removeLayer(layer)
  }

  private def realPenUp() = Utils.runInSwingThread {
    pen = UpPen
  }

  private def realPenDown() = Utils.runInSwingThread {
    if (pen != DownPen) {
      pen = DownPen
      pen.updatePosition()
    }
  }

  private def realTowards(x: Double, y: Double) = Utils.runInSwingThread {
    val newTheta = towardsHelper(x, y)
    changeHeading(newTheta)
    turtle.repaint()
  }

  private def realJumpTo(x: Double, y: Double) = Utils.runInSwingThread {
    changePos(x, y)
    pen.updatePosition()
    turtle.repaint()
  }

  private def realMoveTo(x: Double, y: Double) = Utils.runInSwingThread {
    val newTheta = towardsHelper(x, y)
    changeHeading(newTheta)
    realForward(distanceTo(x,y))
  }

  private def realSetAnimationDelay(d: Long) {
    _animationDelay = d
  }

  private def realGetWorker() {
  }

  private def realSetPenColor(color: Color) = Utils.runInSwingThread {
    pen.setColor(color)
  }

  private def realSetPenThickness(t: Double) = Utils.runInSwingThread {
    pen.setThickness(t)
  }

  private def realSetFontSize(n: Int) = Utils.runInSwingThread {
    pen.setFontSize(n)
  }

  private def realSetFillColor(color: Paint) = Utils.runInSwingThread {
    pen.setFillColor(color)
  }

  private def realSaveStyle() = Utils.runInSwingThread {
    savedStyles.push(currStyle)
  }

  private def realRestoreStyle() = Utils.runInSwingThread {
    if (savedStyles.size == 0) {
      throw new IllegalStateException("No saved style to restore")
    }
    val style = savedStyles.pop()
    pen.setStyle(style)
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

  private def realBeamsOn() = Utils.runInSwingThread {
    beamsOnWorker()
  }

  private def realBeamsOff() = Utils.runInSwingThread {
    beamsOffWorker()
  }

  private def realWrite(text: String) = Utils.runInSwingThread {
    pen.write(text)
//    turtle.repaint()
  }

  private def realHide() = Utils.runInSwingThread {
    hideWorker()
  }

  private def realShow() = Utils.runInSwingThread {
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

  private def realPlaySound(voice: core.Voice) = Utils.runInSwingThread {
    import music._
    try {
      Music(voice).play()
    }
    catch {
      case e: Exception => canvas.outputFn("Turtle Error while playing sound:\n" + e.getMessage)
    }
  }

  private def resetRotation() {
    changeHeading(Utils.deg2radians(90))
  }

  private [kojo] def stop() {
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

  abstract class AbstractPen extends Pen {
    val Log = Logger.getLogger(getClass.getName);

    val turtle = Turtle.this
    val CapThick = BasicStroke.CAP_ROUND
    val CapThin = BasicStroke.CAP_BUTT
    val JoinThick = BasicStroke.JOIN_ROUND
    val JoinThin = BasicStroke.JOIN_BEVEL
    val DefaultColor = Color.red
    val DefaultFillColor = null
    def DefaultStroke = {
      val t = 2/camScale
      val (cap, join) = capJoin(t)
      new BasicStroke(t.toFloat, cap, join)
    }
    val DefaultFont = new Font(new PText().getFont.getName, Font.PLAIN, 18)

    private def capJoin(t: Double) = {
      val Cap = if (t * camScale < 1) CapThin else CapThick
      val Join = if (t * camScale < 1) JoinThin else JoinThick
      (Cap, Join)
    }
    
    def init() {
      lineColor = DefaultColor
      fillColor = DefaultFillColor
      lineStroke = DefaultStroke
      font = DefaultFont
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

    protected def addNewPath() {
      val penPath = newPath()
      penPaths += penPath
      layer.addChild(layer.getChildrenCount-1, penPath)
    }

    protected def removeLastPath() {
      val penPath = penPaths.last
      penPaths.remove(penPaths.size-1)
      layer.removeChild(penPath)
    }

    def getColor = lineColor
    def getFillColor = fillColor
    def getThickness = lineStroke.asInstanceOf[BasicStroke].getLineWidth
    def getFontSize = font.getSize
    
    private def rawSetAttrs(color: Color, thickness: Double, fColor: Paint, fontSize: Int) {
      lineColor = color
      val (cap, join) = capJoin(thickness)
      lineStroke = new BasicStroke(thickness.toFloat, cap, join)
      fillColor = fColor
      font = new Font(new PText().getFont.getName, Font.PLAIN, fontSize)
    }

    def setColor(color: Color) {
      lineColor = color
      addNewPath()
    }

    def setThickness(t: Double) {
      val (cap, join) = capJoin(t)
      lineStroke = new BasicStroke(t.toFloat, cap, join)
      addNewPath()
    }

    def setFontSize(n: Int) {
      font = new Font(new PText().getFont.getName, Font.PLAIN, n)
      addNewPath()
    }

    def setFillColor(color: Paint) {
      fillColor = color
      addNewPath()
    }

    def setStyle(style: Style) {
      rawSetAttrs(style.penColor, style.penThickness, style.fillColor, style.fontSize)
      addNewPath()
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
    def write(text: String) {}
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

    def write(text: String) {
      val ptext = Utils.textNode(text, _position.x, _position.y, canvas.camScale)
      ptext.setFont(font)
      ptext.setTextPaint(pen.getColor)
      layer.addChild(layer.getChildrenCount-1, ptext)
      ptext.repaint()
    }
  }
}