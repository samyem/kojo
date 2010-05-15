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
package xscala

import java.io._
import java.awt.Color
import scala.actors.Actor
import scala.tools.nsc.{Interpreter, InterpreterResults => IR, Settings}

import java.util.logging._

import net.kogics.kojo.util._
import net.kogics.kojo.core._

import org.openide.ErrorManager;

class ScalaCodeRunner(ctx: RunContext, tCanvas: SCanvas, geomCanvas: GeomCanvas) extends CodeRunner {
  val Log = Logger.getLogger(getClass.getName)
  val outputHandler = new InterpOutputHandler(ctx)
  val codeRunner = startCodeRunner()

  def println(s: String) = ctx.println(s)

  def runCode(code: String) = {
    // Runs on swing thread
    codeRunner ! RunCode(code)
  }

  def interruptInterpreter() = InterruptionManager.interruptInterpreter()

  case object Init
  case class RunCode(code: String)
  case class MethodCompletionRequest(str: String)
  case class VarCompletionRequest(str: String)
  case class KeywordCompletionRequest(str: String)
  case class CompletionResponse(data: (List[String], Int))

  def methodCompletions(str: String): (List[String], Int) = {
    val resp = (codeRunner !? MethodCompletionRequest(str)).asInstanceOf[CompletionResponse]
    resp.data
  }

  def varCompletions(str: String): (List[String], Int) = {
    val resp = (codeRunner !? VarCompletionRequest(str)).asInstanceOf[CompletionResponse]
    resp.data
  }

  def keywordCompletions(str: String): (List[String], Int) = {
    val resp = (codeRunner !? KeywordCompletionRequest(str)).asInstanceOf[CompletionResponse]
    resp.data
  }

  def startCodeRunner(): Actor = {
    val actor = new InterpActor
    actor.start()
    actor ! Init
    actor
  }

  object InterruptionManager {
    @volatile var interpreterThread: Option[Thread] = None
    @volatile var interruptTimer: Option[javax.swing.Timer] = None

    def interruptionInProgress = interruptTimer.isDefined

    def interruptInterpreter() {
      Log.info("Interruption of Interpreter Requested")
      // Runs on swing thread
      if (interruptionInProgress) {
        Log.info("Interruption in progress. Bailing out")
        return
      }

      println("Attempting to stop Script...\n")

      if (interpreterThread.isDefined) {
        Log.info("Interrupting Interpreter thread...")
        interruptTimer = Some(Utils.schedule(4) {
            // don't need to clean out interrupt state because Kojo needs to be shut down anyway
            // and in fact, cleaning out the interrupt state will mess with a delayed interruption
            Log.info("Interrupt timer fired")
            println("Unable to stop script.\nPlease restart the Kojo Environment unless you see a 'Script Stopped' message soon.\n")
          })
        outputHandler.interpOutputSuppressed = true
        interpreterThread.get.interrupt
      }
      else {
        println("Animation Stopped.\n")
      }
    }

    def onInterpreterStart() {
      // Runs on Actor pool thread
      // we store the thread every time the interp runs
      // allows interp to switch between react and receive without impacting
      // interruption logic
      interpreterThread = Some(Thread.currentThread)
    }

    def onInterpreterFinish() {
      Log.info("Interpreter Done notification received")
      // Runs on Actor pool thread
      // might not be called for runaway computations
      // in which case Kojo has to be restarted
      if (interruptTimer.isDefined) {
        Log.info("Cancelling interrupt timer")
        interruptTimer.get.stop
        interruptTimer = None
        outputHandler.interpOutputSuppressed = false
        println("Script Stopped.\n")
      }
      interpreterThread = None
    }
  }

  class InterpActor extends Actor {
    
    var interp: Interpreter = _
    val varPattern = java.util.regex.Pattern.compile("\\bvar\\b")

    def safeProcess(fn: => Unit) {
      try {
        fn
      }
      catch {
        case t: Throwable => 
          ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, t);
      }
    }

    def safeProcessCompletionReq(fn: => (List[String], Int)) {
      try {
        reply(CompletionResponse(fn))
      }
      catch {
        case t: Throwable =>
          Log.warning("Problem finding completions: " + t.getMessage)
          reply(CompletionResponse(List(), 0))
      }
    }

    def act() {
      while(true) {
        receive {
          // Runs on Actor pool thread.
          // while(true) receive - ensures we stay on the same thread

          case Init =>
            safeProcess {
              initInterp()
            }

          case RunCode(code) =>
            try {
              Log.info("CodeRunner actor running code:\n---\n%s\n---\n" format(code))
              InterruptionManager.onInterpreterStart()
              ctx.onInterpreterStart(code)

              val ret = interpret(code)
              Log.info("CodeRunner actor done running code. Return value %s" format (ret.toString))

              if (ret == IR.Incomplete) showIncompleteCodeMsg(code)

              if (ret == IR.Success) {
                ctx.onRunSuccess()
              }
              else {
                if (InterruptionManager.interruptionInProgress) ctx.onRunSuccess() // user cancelled running code; no errors
                else ctx.onRunError()
              }
            }
            catch {
              case t: Throwable => Log.log(Level.SEVERE, "Interpreter Problem", t)
                ctx.onRunInterpError
            }
            finally {
              Log.info("CodeRunner actor doing final handling for code.")
              InterruptionManager.onInterpreterFinish()
            }

          case MethodCompletionRequest(str) =>
            safeProcessCompletionReq {
              methodCompletions(str)
            }

          case VarCompletionRequest(str) =>
            safeProcessCompletionReq {
              varCompletions(str)
            }

          case KeywordCompletionRequest(str) =>
            safeProcessCompletionReq {
              keywordCompletions(str)
            }
        }
      }
    }

    def initInterp() {
      val iSettings = new Settings()
      iSettings.classpath.append(createCp(
          List("modules/ext/scala-library.jar",
               "modules/ext/scala-compiler.jar",
               "modules/net-kogics-kojo.jar",
               "modules/ext/piccolo2d-core-1.3-SNAPSHOT.jar",
               "modules/ext/piccolo2d-extras-1.3-SNAPSHOT.jar",
               "modules/geogebra.jar"
          )
        ))

      interp = new Interpreter(iSettings, new GuiPrintWriter()) {
        override protected def parentClassLoader = classOf[ScalaCodeRunner].getClassLoader
      }
      interp.setContextClassLoader()

      outputHandler.withOutputSuppressed {
        interp.bind("predef", "net.kogics.kojo.xscala.ScalaCodeRunner", ScalaCodeRunner.this)
        interp.interpret("val builtins = predef.Builtins")
        interp.interpret("import predef.Builtins._")
        interp.bind("turtle0", "net.kogics.kojo.core.Turtle", tCanvas.turtle0)
        interp.interpret("val Canvas = predef.CanvasAPI")
        interp.interpret("val Staging = predef.StagingAPI")
        interp.bind("Mw", "net.kogics.kojo.core.GeomCanvas", geomCanvas)
      }

      ctx.onInterpreterInit()
    }

    def createCp(xs: List[String]): String = {
      val ourCp = new StringBuilder

//      val oldCp = System.getProperty("java.class.path")
//      The above is the system classpath with a lot of Netbeans jars at the end
      val oldCp = System.getenv("CLASSPATH")
      if (oldCp != null) {
        ourCp.append(oldCp)
        ourCp.append(File.pathSeparatorChar)
      }

      // allow another way to customize classpath
      val kojoCp = System.getenv("KOJO_CLASSPATH")
      if (kojoCp != null) {
        ourCp.append(kojoCp)
        ourCp.append(File.pathSeparatorChar)
      }

      val prefix = Utils.installDir

      xs.foreach {x =>
        ourCp.append(prefix)
        ourCp.append(File.separatorChar)
        ourCp.append(x)
        ourCp.append(File.pathSeparatorChar)
      }
      ourCp.toString
    }

    def interpretLine(lines: List[String]): IR.Result = lines match {
      case Nil => IR.Success
      case code :: tail =>
//        Log.info("Interpreting code: %s\n" format(code))
        interp.interpret(code) match {
          case IR.Error       => IR.Error
          case IR.Success     => interpretLine(lines.tail)
          case IR.Incomplete  =>
            tail match {
              case Nil => IR.Incomplete
              case code2 :: tail2 => interpretLine(code + "\n" + code2 :: tail2)
            }
        }
    }

    def interpretLineByLine(code: String): IR.Result = {
      val lines = code.split("\r?\n").toList.filter(line => line.trim() != "" && !line.trim().startsWith("//"))
//            Log.info("Code Lines: " + lines)
      interpretLine(lines)
    }

    def interpretAllLines(code: String): IR.Result = interp.interpret(code)

    def interpret(code: String): IR.Result = {
      if (needsLineByLineInterpretation(code)) interpretLineByLine(code)
      else interpretAllLines(code)
    }

    def needsLineByLineInterpretation(code: String): Boolean = {
      varPattern.matcher(code).find()
    }

    def showIncompleteCodeMsg(code: String) {
      val msg = """
      |error: Incomplete code fragment
      |You probably have a missing brace/bracket somewhere in your script
      """.stripMargin
      ctx.reportErrorMsg(msg)
    }

    import CodeCompletionUtils._
    var _builtinsCompletions: List[String] = Nil

    def builtinsCompletions = {
      if (_builtinsCompletions == Nil) {
        _builtinsCompletions = completions("builtins")
      }
      _builtinsCompletions
    }

    def completions(identifier: String) = {
      Log.fine("Finding Identifier completions for: " + identifier)
      val completions = outputHandler.withOutputSuppressed {
        interp.methodsOf(identifier).distinct.filter {s => !MethodDropFilter.contains(s)}
      }
      Log.fine("Completions: " + completions)
      completions
    }

    def methodCompletions(str: String): (List[String], Int) = {
      val (oIdentifier, oPrefix) = findIdentifier(str)
      val prefix = if(oPrefix.isDefined) oPrefix.get else ""
      if (oIdentifier.isDefined) {
        (completions(oIdentifier.get).filter {s => s.startsWith(prefix)}, prefix.length)
      }
      else {
        val c1s = builtinsCompletions.filter {s => s.startsWith(prefix)}
        Log.fine("Filtered builtins completions for prefix '%s' - %s " format(prefix, c1s))
        (c1s, prefix.length)
      }
    }

    def varCompletions(str: String): (List[String], Int) = {

      def varFilter(s: String) = !VarDropFilter.contains(s) && !InternalVarsRe.matcher(s).matches

      val (oIdentifier, oPrefix) = findIdentifier(str)
      val prefix = if(oPrefix.isDefined) oPrefix.get else ""
      if (oIdentifier.isDefined) {
        (Nil, 0)
      }
      else {
        val c2s = interp.unqualifiedIds.filter {s => s.startsWith(prefix) && varFilter(s)}
        (c2s, prefix.length)
      }
    }

    def keywordCompletions(str: String): (List[String], Int) = {
      val (oIdentifier, oPrefix) = findIdentifier(str)
      val prefix = if(oPrefix.isDefined) oPrefix.get else ""
      if (oIdentifier.isDefined) {
        (Nil, 0)
      }
      else {
        val c2s = Keywords.filter {s => s != null && s.startsWith(prefix)}
        (c2s, prefix.length)
      }
    }
  }

  class GuiWriter extends Writer {
    override def write(s: String) {
      outputHandler.showInterpOutput(s)
    }

    def write(cbuf: Array[Char], off: Int, len: Int) {
      outputHandler.showInterpOutput(new String(cbuf, off, len))
    }

    def close() {}
    def flush() {}
  }

  class GuiPrintWriter() extends PrintWriter(new GuiWriter(), false) {

    override def write(s: String) {
      // intercept string writes and forward to the GuiWriter's string write() method
      out.write(s)
    }
  }

  object Builtins extends SCanvas with TurtleMover {
    type Turtle = core.Turtle
    type Color = java.awt.Color
    type Point = net.kogics.kojo.core.Point
    
    PuzzleLoader.init()
    val Random = new java.util.Random
    val turtle0 = tCanvas.turtle0
    val figure0 = tCanvas.figure0

    def color(r: Int, g: Int, b: Int) = new Color(r, g, b)
    def color(rgbHex: Int) = new Color(rgbHex)
    def random(upperBound: Int) = Random.nextInt(upperBound)
    def randomDouble(upperBound: Int) = Random.nextDouble * upperBound
    
    def print(obj: Any): Unit = println(obj.toString)
    def println(obj: Any): Unit = println(obj.toString)

    def println(s: String): Unit = {
      // Runs on Actor pool (interpreter) thread
      ScalaCodeRunner.this.println(s + "\n")
      Throttler.throttle()
    }

    def readln(prompt: String): String = ctx.readInput(prompt)
    def readInt(prompt: String): Int = readln(prompt).toInt
    def readDouble(prompt: String): Double = readln(prompt).toDouble
    def showScriptInOutput() = ctx.showScriptInOutput()
    def hideScriptInOutput() = ctx.hideScriptInOutput()
    def showVerboseOutput() = ctx.showVerboseOutput()
    def hideVerboseOutput() = ctx.hideVerboseOutput()

    def version = println("Scala " + scala.tools.nsc.Properties.versionString)

    def help() = {
      println("""You can press Ctrl-Space in the script window at any time to see available commands and functions.

Here's a partial list of available commands:
  forward(numSteps) - Move the turtle forward a given number of steps
  back(numSteps) - Move the turtle back a given number of steps
  turn(angle) - Turn the turtle through a specified angle. Angles are positive for counter-clockwise turns
  right() - Turn the turtle right
  right(angle) - Turn the turtle right through a specified angle
  left() - Turn the turtle left
  left(angle) - Turn the turtle left through a specified angle
  towards(x, y) - Turn the turtle towards the point (x, y)
  moveTo(x, y) - Move the turtle to the point (x, y)
  setHeading(angle) - Set the turtle's heading (the direction in which it is pointing)
  setPosition(x, y) - Set the turtle's position (without making it draw any lines). The turtle's heading is not changed
  home() - Move the turtle to its original location, and make it point north
  write(text) - Make the turtle write the specified text at its current location
  undo() - Undo the last turtle command

  clear() - Clear the screen. To bring the turtle to the center after this step, just resize the turtle canvas
  zoom(factor, cx, cy) - Zoom in by the given factor, and position (cx, cy) at the center of the turtle canvas

  penDown() - Make the turtle put its pen down - to draw lines as it moves. The pen is down by default
  penUp() - Make the turtle not draw lines as it moves
  setPenColor(color) - Specify the color of the pen that the turtle draws with
  setPenThickness(thickness) - Specify the thickness (as an number) of the pen that the turtle draws with
  setFillColor(color) - Specify the fill color of the areas drawn by the turtle

  listPuzzles() - show the names of the puzzles available in the system
  loadPuzzle(name) - load the named puzzle
  clearPuzzlers() - clear out the puzzler turtles and the puzzles from the screen

  gridOn() - Show a grid on the canvas
  gridOff() - Hide the grid
  beamsOn() - Show crossbeams centered on the turtle - to help with solving puzzles
  beamsOff() - Hide the turtle crossbeams
  invisible() - Hide the turtle
  visible() - Make the hidden turtle visible again

  newTurtle(x, y) - Make a new turtle located at the point (x, y)
  turtle0 - gives you a handle to the original turtle.

  showScriptInOutput() - Display scripts in the output window when they run
  hideScriptInOutput() - Do not display scripts in the output window

  version - Display the version of Scala being used
  repeat(n) {} - Repeat commands within braces n number of times
  println(string) - Display the given string in the output window
  readln(promptString) - Display the given prompt in the output window and read a line that the user enters
  readInt(promptString) - Display the given prompt in the output window and read an Integer value that the user enters
  readDouble(promptString) - Display the given prompt in the output window and read a Double-precision Real value that the user enters
  random(upperBound) - Return a random Integer between 0 (inclusive) and upperBound (exclusive)
  randomDouble(upperBound) - Return a random Double-precision Real between 0 (inclusive) and upperBound (exclusive)
  inspect(obj) - explore the internal fields of the given object
""")
    }

    def repeat(n: Int) (fn: => Unit) {
      for (i <- 1 to n) {
        fn
        Throttler.throttle()
      }
    }

    def gridOn() = tCanvas.gridOn()
    def gridOff() = tCanvas.gridOff()
    def zoom(factor: Double, cx: Double, cy: Double) = tCanvas.zoom(factor, cx, cy)

    def forward() = println("Please provide the distance to move forward - e.g. forward(100)")
    def back() = println("Please provide the distance to move back - e.g. back(100)")
    def turn() = println("Please provide the angle to turn in degrees - e.g. turn(45)")
    def towards() = println("Please provide the coordinates of the point that the turtle should turn towards - e.g. towards(100, 100)")
    def moveTo() = println("Please provide the coordinates of the point that the turtle should move to - e.g. moveTo(100, 100)")
    def setPenColor() = println("Please provide the color of the pen that the turtle should draw with - e.g setPenColor(blue)")
    def setPenThickness() = println("Please provide the thickness of the pen that the turtle should draw with - e.g setPenThickness(1)")
    def setFillColor() = println("Please provide the fill color for the areas drawn by the turtle - e.g setFillColor(yellow)")

    val blue = Color.blue
    val red = Color.red
    val yellow = Color.yellow
    val green = Color.green
    val orange = Color.orange
    val purple = new Color(0x740f73)
    val pink = Color.pink
    val brown = new Color(0x583a0b)
    val black = Color.black
    val white = Color.white


    // I tried implicits to support automatic delegation. That works as expected,
    // but is not too friendly for the user - they need to say something like
    // builtins.forward(100) as opposed to forward(100). Also - completion needs extra support
    // implicit def toSCanvas(builtins: Builtins): SCanvas = builtins.tCanvas

    def forward(n: Double) = turtle0.forward(n)
    def turn(angle: Double) = turtle0.turn(angle)
    def clear() = tCanvas.clear()
    def clearPuzzlers() = tCanvas.clearPuzzlers()
    def penUp() = turtle0.penUp()
    def penDown() = turtle0.penDown()
    def setPenColor(color: Color) = turtle0.setPenColor(color)
    def setPenThickness(t: Double) = turtle0.setPenThickness(t)
    def setFillColor(color: Color) = turtle0.setFillColor(color)
    def saveStyle() = turtle0.saveStyle()
    def restoreStyle() = turtle0.restoreStyle()
    def style: Style = turtle0.style
    def beamsOn() = turtle0.beamsOn()
    def beamsOff() = turtle0.beamsOff()
    def write(text: String) = turtle0.write(text)
    def visible() = turtle0.visible()
    def invisible() = turtle0.invisible()

    def towards(x: Double, y: Double) = turtle0.towards(x, y)
    def position: Point = turtle0.position
    def heading: Double = turtle0.heading

    def jumpTo(x: Double, y: Double) = turtle0.jumpTo(x, y)
    def moveTo(x: Double, y: Double) = turtle0.moveTo(x, y)

    def animationDelay = turtle0.animationDelay
    def setAnimationDelay(d: Long) = turtle0.setAnimationDelay(d)

    def newTurtle() = tCanvas.newTurtle(0, 0)
    def newTurtle(x: Int, y: Int) = tCanvas.newTurtle(x, y)

    def newPuzzler(x: Int, y: Int) = tCanvas.newPuzzler(x, y)
    def undo() = tCanvas.undo()

    def inspect(obj: AnyRef) = ctx.inspect(obj)

    def listPuzzles = PuzzleLoader.listPuzzles
    def clearOutput() = ctx.clearOutput()

    def newFigure(x: Int, y: Int) = tCanvas.newFigure(x, y)

    def loadPuzzle(name: String) {
      val oPuzzleFn = PuzzleLoader.readPuzzle(name)
      if (oPuzzleFn.isDefined) {
        val code = oPuzzleFn.get + """

          def go() {
            val pTurtle = newPuzzler(0,0)
            puzzle(pTurtle)
          }
          go()
      """
        runCode(code)

        val code2 = """
          clearOutput()
          println("Puzzle Description")
        """
        runCode(code2)
      }
      else {
        println("Puzzle not available: " + name)
      }
    }
  }

  object StagingAPI {
    /* DISCLAIMER
     Parts of this interface is written to approximately
     conform to the Processing API as described in the
     reference at <URL: http://processing.org/reference/>.
     The implementation code is the work of Peter Lewerin
     <peter.lewerin@tele2.se> and is not in any way
     derived from the Processing source.
    */
    //W#summary Developer home-page for the Staging Module
    //W
    //W=Introduction=
    //W
    //WThe Staging Module is currently being developed by Peter Lewerin.
    //WThe original impetus came from a desire to run Processing-style code in Kojo.
    //W
    //WAt this point, the shape hierarchy is the most complete part, but
    //Wutilities for color definition, time keeping etc are being added.
    import core._
    import math._

    type Color = java.awt.Color

    //W
    //W=Points=
    //W
    //WStaging uses an enriched version of {{{net.kogics.kojo.core.Point}}} for
    //Wcoordinates.  A companion object provides _apply_ and _unapply_ methods.
    //WTuples of {{{Double}}}s or {{{Int}}}s are implicitly converted to
    //W{{{Point}}}s where applicable.
    type Point = net.kogics.kojo.core.Point
    object Point {
      def apply(x: Double, y: Double) = new Point(x, y)
      def unapply(p: Point) = Some((p.x, p.y))
    }

    var mouseX = 0.
    var mouseY = 0.
    def onMouseMove(fn: (Double, Double) => Unit) {
      tCanvas.figure0.onMouseMove(fn)
    }
    def onMouseOver(left: Int, top: Int, right: Int, bottom: Int)(fn: (Double, Double) => Unit) {
      onMouseMove { (x: Double, y: Double) =>
        if (x >= left && y <= top && x <= right && y >= bottom) fn(x, y)
      }
    }

    //W
    //W=Screen=
    //W
    //WStaging defines an object {{{Screen}}} that provides methods to set
    //Wbackground color and screen size.
    object Screen {
      var width = 0
      var height = 0
      def size(width: Int, height: Int) = {
        this.width = width
        this.height = height
        // TODO make less ad-hoc
        tCanvas.zoom(560 / height, width / 2, height / 2)
        onMouseOver(0, height, width, 0) { (x: Double, y: Double) =>
          mouseX = x
          mouseY = y
        }
        (width, height)
      }
    }

    val O = Point(0, 0)
    def M = Point(Screen.width / 2, Screen.height / 2)
    def E = Point(Screen.width, Screen.height)

    implicit def tupleDToPoint(tuple: (Double, Double)) = Point(tuple._1, tuple._2)
    implicit def tupleIToPoint(tuple: (Int, Int)) = Point(tuple._1, tuple._2)

    //W=Shapes=
    //W
    //W==Summary==
    //W|| *Class* or trait     || *Extends*                 || *Defines* (methods in italics)  ||
    //W|| Shape                ||                           || _draw_                          ||
    //W
    //W==Shape (trait)==
    //W
    //W{{{Shape}}} is the base type for all shapes.  Every class that extends
    //Wit must implement the nullary method _draw_.  This method should create
    //Wan instance of the shape and add it to the canvas.
    //W
    //W_Not implemented yet: shapes should remember the colors and stroke style
    //Wused and any transforms applied, and apply them again whenever _draw_ is
    //Wcalled._
    trait Shape {
      def draw: Shape
    }
    //W|| Rounded              ||                           || curvature, _radiusX_, _radiusY_ ||
    //W
    //W==Rounded (trait)==
    //W
    //W{{{Rounded}}} is a base type for shapes with rounded parts.  Every class
    //Wthat extends it must have a value member, curvature, of type {{{Point}}}.
    //WIt defines _radiusX_, _radiusY_ as access methods to the _x_ and _y_
    //Wcomponents of curvature.
    trait Rounded {
      val curvature: Point
      def radiusX = curvature.x
      def radiusY = curvature.y
    }
    //W|| !BaseShape           || Shape                     || origin, _toLine_                ||
    //W
    //W==!BaseShape (trait)==
    //W
    //W{{{BaseShape}}} is the base type for shapes that have a point of origin.
    //WThis value member of type {{{Point}}} defines the lower left corner of
    //Wthe shape bounds (except for {{{Elliptical}}} shapes, see below).
    trait BaseShape extends Shape {
      val origin: Point
      def toLine(p: Point): Line = Line(origin, p)
    }

    //W|| !PolyShape           || Shape                   || points, _toPolyline_, _toPolygon_ ||
    //W
    //W==!PolyShape (trait)==
    //W
    //W{{{PolyShape}}} is the base type for shapes that are defined by several
    //Wpoints.  The points are stored in a value member of type sequence of
    //W{{{Point}}}s.
    trait PolyShape extends Shape {
      val points: Seq[Point]
      def toPolygon: Polygon = Polygon(points)
      def toPolyline: Polyline = Polyline(points)
    }

    //W|| !SimpleShape         || !BaseShape|| endpoint, _width_, _height_, _toLine_, _toRect_ ||
    //W
    //W==!SimpleShape (trait)==
    //W
    //W{{{SimpleShape}}} is the base type for shapes that are defined by two
    //Wpoints, origin and endpoint (the upper right corner of the shape bounds).
    //WThey have _width_ and _height_.  Every class that extends this trait must
    //Whave value members origin and endpoint, of type {{{Point}}}.
    trait SimpleShape extends BaseShape {
      val endpoint: Point
      def width = endpoint.x - origin.x
      def height = endpoint.y - origin.y
      def toLine: Line = Line(origin, endpoint)
      def toRect: Rectangle = Rectangle(origin, endpoint)
      def toRect(p: Point): RoundRectangle = RoundRectangle(origin, endpoint, p)
    }

    //W|| Elliptical           || Rounded with !SimpleShape || `*`                             ||
    //W`*`: {{{Elliptical}}} implements {{{curvature}}} and overrides {{{width}}} and {{{height}}}.
    //W
    //W==Elliptical (trait)==
    //W
    //W{{{Elliptical}}} is the base type for shapes that are rounded and whose
    //Worigin value member defines their center.
    trait Elliptical extends Rounded with SimpleShape {
      val curvature = endpoint - origin
      override def width = 2 * radiusX
      override def height = 2 * radiusY
    }

    //W|| *Dot*                || !BaseShape                ||                                 ||
    //W
    //W==Dot==
    //W
    //W{{{Dot}}} is drawn to the canvas as a dot of the stroke color.
    class Dot(val origin: Point) extends BaseShape {
      def draw = {
        origin match {
          case Point(x, y) =>
            tCanvas.figure0.point(x, y)
        }
        this
      }

      override def toString = "Staging.Dot(" + origin + ")"
    }
    object Dot {
      def apply(p: Point) = {
        val shape = new Dot(p)
        shape.draw
      }
    }
    def dot(x: Double, y: Double) = Dot(Point(x, y))
    def dot(p: Point) = Dot(p)

    //W|| *Line*               || !SimpleShape              ||                                 ||
    //W
    //W==Line==
    //W
    //W{{{Line}}} is drawn to the canvas as a straight line of the stroke color
    //Wfrom origin to endpoint.
    class Line(val origin: Point, val endpoint: Point) extends SimpleShape {
      def draw = {
        tCanvas.figure0.line(origin, endpoint)
        this
      }
      override def toString = "Staging.Line(" + origin + ", " + endpoint + ")"
    }
    object Line {
      def apply(p1: Point, p2: Point) = {
        val shape = new Line(p1, p2)
        shape.draw
      }
    }
    def line(x: Double, y: Double, w: Double, h: Double): Line =
      Line(Point(x, y), Point(x + w, y + h))
    def line(p1: Point, w: Double, h: Double): Line = {
      Line(p1, Point(p1.x + w, p1.y + h))
    }
    def line(p1: Point, p2: Point): Line = {
      Line(p1, p2)
    }

    //W|| *Rectangle*          || !SimpleShape              ||                                 ||
    //W
    //W==Rectangle==
    //W
    //W{{{Rectangle}}} is drawn to the canvas as a rectangle of the fill and
    //Wstroke color from origin to endpoint.
    //W
    //WThe width and height of the rectangle must both be positive, so if the
    //Wdimensions are given in the form of a {{{Point}}} it must be to the right
    //Wof and above origin.
    class Rectangle(val origin: Point, val endpoint: Point) extends SimpleShape {
      // precondition endpoint > origin
      require(width > 0 && height > 0)
      def draw = {
        tCanvas.figure0.rectangle(origin, endpoint)
        this
      }
      override def toString = "Staging.Rectangle(" + origin + ", " + endpoint + ")"
    }
    object Rectangle {
      def apply(p1: Point, p2: Point) = {
        val shape = new Rectangle(p1, p2)
        shape.draw
      }
    }
    def rectangle(x: Double, y: Double, w: Double, h: Double): Rectangle =
      Rectangle(Point(x, y), Point(x + w, y + h))
    def rectangle(p: Point, w: Double, h: Double): Rectangle =
      Rectangle(p, Point(p.x + w, p.y + h))
    def rectangle(p1: Point, p2: Point): Rectangle =
      Rectangle(p1, p2)
    def square(x: Double, y: Double, s: Double): Rectangle =
      Rectangle(Point(x, y), Point(x + s, y + s))
    def square(p: Point, s: Double): Rectangle =
      Rectangle(p, Point(p.x + s, p.y + s))

    //W|| *!RoundRectangle*    || Rounded with !SimpleShape ||                                 ||
    //W
    //W==!RoundRectangle==
    //W
    //W{{{RoundRectangle}}} is drawn to the canvas as a rectangle with rounded
    //Wcorners of the fill and stroke color from origin to endpoint.  The
    //Wcurvature of the corners can be determined by x-radius and y-radius
    //Wvalues or by a point value.
    //W
    //WThe width and height of the rectangle must both be positive, so if the
    //Wdimensions are given in the form of a {{{Point}}} it must be to the right
    //Wof and above origin.
    class RoundRectangle(
      val origin: Point,
      val endpoint: Point,
      val curvature: Point
    ) extends Rounded with SimpleShape {
      // precondition endpoint > origin
      require(width > 0 && height > 0)
      def draw = {
        tCanvas.figure0.roundRectangle(origin, endpoint, radiusX, radiusY)
        this
      }
      override def toString =
        "Staging.RoundRectangle(" + origin + ", " + endpoint + ", " + curvature + ")"
    }
    object RoundRectangle {
      def apply(p1: Point, p2: Point, p3: Point) = {
        val shape = new RoundRectangle(p1, p2, p3)
        shape.draw
      }
    }
    def roundRectangle(
      x: Double, y: Double,
      w: Double, h: Double,
      rx: Double, ry: Double
    ) =
      RoundRectangle(Point(x, y), Point(x + w, y + h), Point(rx, ry))
    def roundRectangle(
      p: Point,
      w: Double, h: Double,
      rx: Double, ry: Double
    ) =
      RoundRectangle(p, Point(p.x + w, p.y + h), Point(rx, ry))
    def roundRectangle(p1: Point, p2: Point, rx: Double, ry: Double) =
      RoundRectangle(p1, p2, Point(rx, ry))
    def roundRectangle(p1: Point, p2: Point, p3: Point) =
      RoundRectangle(p1, p2, p3)

    //W|| *Polyline*           || !PolyShape                ||                                 ||
    //W
    //W==Polyline==
    //W
    //W{{{Polyline}}} is drawn to the canvas as a segmented line connecting the
    //Wgiven points by straight edges, using the fill and stroke color.
    class Polyline(val points: Seq[Point]) extends PolyShape {
      val shapePath = new kgeom.PolyLine()
      points foreach { case Point(x, y) =>
        shapePath.addPoint(x, y)
      }
      def draw = {
        tCanvas.figure0.polyLine(shapePath)
        this
      }

      override def toString = "Staging.Polyline(" + points + ")"
    }
    object Polyline {
      def apply(pts: Seq[Point]) = {
        val shape = new Polyline(pts)
        shape.draw
      }
    }
    def polyline(pts: Seq[Point]): Polyline = Polyline(pts)

    //W|| *Polygon*            || !PolyShape                ||                                 ||
    //W
    //W==Polygon==
    //W
    //W{{{Polygon}}} is drawn to the canvas as a segmented line connecting the
    //Wgiven points by straight edges, using the fill and stroke color.  The
    //Wshape is closed, meaning that the last point connects to the first point.
    class Polygon(val points: Seq[Point]) extends PolyShape {
      val shapePath = new kgeom.PolyLine()
      points foreach { case Point(x, y) =>
        shapePath.addPoint(x, y)
      }
      shapePath.polyLinePath.closePath
      def draw = {
        tCanvas.figure0.polyLine(shapePath)
        this
      }

      override def toString = "Staging.Polygon(" + points + ")"
    }
    object Polygon {
      def apply(pts: Seq[Point]) = {
        val shape = new Polygon(pts)
        shape.draw
      }
    }
    def polygon(pts: Seq[Point]): Polygon = Polygon(pts)
    def triangle(p0: Point, p1: Point, p2: Point) = polygon(Seq(p0, p1, p2))
    def quad(p0: Point, p1: Point, p2: Point, p3: Point) =
      polygon(Seq(p0, p1, p2, p3))

    //W|| *Ellipse*            || Elliptical                ||                                 ||
    //W
    //W==Ellipse==
    //W
    //W{{{Ellipse}}} is drawn to the canvas as an ellipse of the fill and stroke
    //Wcolor centering on origin, with a curvature defined by the distance from
    //Worigin to endpoint.
    class Ellipse(val origin: Point, val endpoint: Point) extends Elliptical {
      def draw = {
        tCanvas.figure0.ellipse(origin, width, height)
        this
      }
    }
    object Ellipse {
      def apply(p1: Point, p2: Point) = {
        val shape = new Ellipse(p1, p2)
        shape.draw
      }
    }
    def ellipse(x: Double, y: Double, w: Double, h: Double) =
      Ellipse(Point(x, y), Point(x + w, y + h))
    def ellipse(p: Point, w: Double, h: Double) =
      Ellipse(p, Point(p.x + w, p.y + h))
    def ellipse(p1: Point, p2: Point) =
      Ellipse(p1, p2)
    def circle(x: Double, y: Double, r: Double): Ellipse =
      Ellipse(Point(x, y), Point(x + r, y + r))
    def circle(p: Point, r: Double): Ellipse =
      Ellipse(p, Point(p.x + r, p.y + r))

    //W|| *Arc*                || Elliptical                || start, extent                   ||
    //W
    //W==Arc==
    //W
    //W{{{Arc}}} is drawn to the canvas as an elliptical sector of the fill and
    //Wstroke color centering on origin, with a curvature defined by the
    //Wdistance from origin to endpoint.  The class defines two value members of
    //Wtype {{{Double}}}: start is angle where the arc begins, and extent is the
    //Wangle between start and end of the arc.  Both angles are given in degrees,
    //Wwith 0 at "three o'clock", 90 at "twelve o'clock" and so on.
    class Arc(
      val origin: Point, val endpoint: Point,
      val start: Double, val extent: Double
    ) extends Elliptical {
      def draw = {
        origin match {
          case Point(x, y) =>
          tCanvas.figure0.arc(x, y, width, height, start, extent)
        }
        this
      }
    }
    object Arc {
      def apply(p1: Point, p2: Point, s: Double, e: Double) = {
        val shape = new Arc(p1, p2, s, e)
        shape.draw
      }
    }
    def arc(x: Double, y: Double, w: Double, h: Double, s: Double, e: Double): Arc =
      Arc(Point(x, y), Point(x + w / 2, y + h / 2), s, e)
    def arc(p: Point, w: Double, h: Double, s: Double, e: Double): Arc =
      Arc(p, Point(p.x + w / 2, p.y + h / 2), s, e)
    def arc(p1: Point, p2: Point, s: Double, e: Double): Arc =
      Arc(p1, p2, s, e)

    //W|| *!LinesShape*        || !PolyShape                ||                                 ||
    //W
    //W==!LinesShape==
    //W
    //W{{{LinesShape}}} takes a sequence of {{{Point}}}s and connects them
    //Wpairwise by straight lines of the stroke color.
    class LinesShape(val points: Seq[Point]) extends PolyShape {
      def draw = {
        points grouped(2) foreach {
          case List() =>
          case Seq(p0, p1) =>
            line(p0, p1)
          case Point(x, y) :: Nil =>
            dot(x, y)
        }
        this
      }
    }
    object LinesShape {
      def apply(pts: Seq[Point]) = {
        val shape = new LinesShape(pts)
        shape.draw
      }
    }
    def linesShape(pts: Seq[Point]) = LinesShape(pts)

    //W|| *!TrianglesShape*    || !PolyShape                ||                                 ||
    //W
    //W==!TrianglesShape==
    //W
    //W{{{TrianglesShape}}} takes a sequence of {{{Point}}}s and connects them
    //Was triangles of the fill and stroke color.
    class TrianglesShape(val points: Seq[Point]) extends PolyShape {
      def draw = {
        points grouped(3) foreach {
          case List() =>
          case s @ Seq(p0, p1, p2) =>
            polygon(s)
          case p0 :: p1 :: Nil =>
            line(p0, p1)
          case Point(x, y) :: Nil =>
            dot(x, y)
        }
        this
      }
    }
    object TrianglesShape {
      def apply(pts: Seq[Point]) = {
        val shape = new TrianglesShape(pts)
        shape.draw
      }
    }
    def trianglesShape(pts: Seq[Point]) = TrianglesShape(pts)

    //W|| *!TriangleStripShape*|| !PolyShape                ||                                 ||
    //W
    //W==!TriangleStripShape==
    //W
    //W{{{TriangleStripShape}}} takes a sequence of {{{Point}}}s and connects
    //Wthem as adjoining triangles of the fill and stroke color.
    class TriangleStripShape(val points: Seq[Point]) extends PolyShape {
      def draw = {
        points sliding(3) foreach {
          case List() =>
          case s @ Seq(p0, p1, p2) =>
            polygon(s)
          case p0 :: p1 :: Nil =>
            line(p0, p1)
          case Point(x, y) :: Nil =>
            dot(x, y)
        }
        this
      }
    }
    object TriangleStripShape {
      def apply(pts: Seq[Point]) = {
        val shape = new TriangleStripShape(pts)
        shape.draw
      }
    }
    def triangleStripShape(pts: Seq[Point]) = TriangleStripShape(pts)

    //W|| *!QuadsShape*        || !PolyShape                ||                                 ||
    //W
    //W==!QuadsShape==
    //W
    //W{{{QuadsShape}}} takes a sequence of {{{Point}}}s and connects them as
    //Wquads (polygons of four points) of the fill and stroke color.
    class QuadsShape(val points: Seq[Point]) extends PolyShape {
      def draw = {
        points grouped(4) foreach {
          case List() =>
          case s @ Seq(p0, p1, p2, p3) =>
            polygon(s)
          case s @ p0 :: p1 :: p2 :: Nil =>
            Polyline(s)
          case p0 :: p1 :: Nil =>
            line(p0, p1)
          case Point(x, y) :: Nil =>
            dot(x, y)
        }
        this
      }
    }
    object QuadsShape {
      def apply(pts: Seq[Point]) = {
        val shape = new QuadsShape(pts)
        shape.draw
      }
    }
    def quadsShape(pts: Seq[Point]) = QuadsShape(pts)

    //W|| *!QuadStripShape*    || !PolyShape                ||                                 ||
    //W
    //W==!QuadStripShape==
    //W
    //W{{{QuadStripShape}}} takes a sequence of {{{Point}}}s and connects them
    //Was adjoining quads of the fill and stroke color.
    class QuadStripShape(val points: Seq[Point]) extends PolyShape {
      def draw = {
        points sliding(4, 2) foreach {
          case List() =>
          case s @ Seq(p0, p1, p2, p3) =>
            polygon(s)
          case s @ p0 :: p1 :: p2 :: Nil =>
            polyline(s)
          case p0 :: p1 :: Nil =>
            line(p0, p1)
          case Point(x, y) :: Nil =>
            dot(x, y)
        }
        this
      }
    }
    object QuadStripShape {
      def apply(pts: Seq[Point]) = {
        val shape = new QuadStripShape(pts)
        shape.draw
      }
    }
    def quadStripShape(pts: Seq[Point]) = QuadStripShape(pts)

    //W|| *!TriangleFanShape*  || !PolyShape with !BaseShape||                                 ||
    //W
    //W==!TriangleFanShape==
    //W
    //W{{{TriangleFanShape}}} takes a center point (origin) and a sequence of
    //W{{{Point}}}s, and connects the points pairwise with each other and with
    //Wthe center point with straight edges of the fill and stroke color.
    class TriangleFanShape(val origin: Point, val points: Seq[Point]) extends PolyShape
                                                                         with BaseShape {
      def draw = {
        points grouped(2) foreach {
          case List() =>
          case s @ Seq(p0, p1) =>
            polyline(Seq(origin) ++ s)
          case p1 :: Nil =>
            line(origin, p1)
        }
        this
      }
    }
    object TriangleFanShape {
      def apply(p0: Point, pts: Seq[Point]) = {
        val shape = new TriangleFanShape(p0, pts)
        shape.draw
      }
    }
    def triangleFanShape(p0: Point, pts: Seq[Point]) = TriangleFanShape(p0, pts)

    //W|| *!SvgShape*  || Shape || node                            ||
    //W
    //W==!SvgShape==
    //W
    //W{{{SvgShape}}} takes a SVG element (rect, circle, ellipse, line, polyline,
    //Wpolygon, or path) and draws it as a shape of the fill and stroke color.
    //W
    //WTODO: Should handle g and svg elements in the future.
    class SvgShape(val node: scala.xml.Node) extends Shape {
      private def matchXY (ns: scala.xml.Node, xn: String = "x", yn: String = "y"): (Double, Double) = {
        val xStr = (ns \ ("@" + xn)).toString
        val yStr = (ns \ ("@" + yn)).toString
        val x = if (xStr == "") 0 else xStr.toDouble
        val y = if (yStr == "") 0 else yStr.toDouble
        (x, y)
      }

      private def matchWH (ns: scala.xml.Node): (Double, Double) = {
        val widthStr = (ns \ "@width").toString
        val heightStr = (ns \ "@height").toString
        val width = if (widthStr == "") 0 else widthStr.toDouble
        val height = if (heightStr == "") 0 else heightStr.toDouble
        require(width >= 0, "Bad width for XML element " + ns)
        require(height >= 0, "Bad height for XML element " + ns)
        (width, height)
      }

      private def matchRXY (ns: scala.xml.Node): (Double, Double) = {
        val xStr = (ns \ "@rx").toString
        val yStr = (ns \ "@ry").toString
        val x = if (xStr == "") 0 else xStr.toDouble
        require(x >= 0, "Bad rx for XML element " + ns)
        val y = if (yStr == "") 0 else yStr.toDouble
        require(y >= 0, "Bad ry for XML element " + ns)
        val rx = if (x != 0) x else y
        val ry = if (y != 0) y else x
        (rx, ry)
      }

      private def matchFillStroke (ns: scala.xml.Node): (Option[Color], Option[Color]) = {
        val fStr = (ns \ "@fill").toString
        val sStr = (ns \ "@stroke").toString
        //TODO
        (None, None)
      }

      private def matchPoints (ns: scala.xml.Node): Seq[Point] = {
        val pointsStr = (node \ "@points").toString
        val splitter = "(:?,\\s*|\\s+)".r
        val pointsItr = (splitter split pointsStr) map (_.toDouble) grouped(2)
        (pointsItr map { a => Point(a(0), a(1)) }).toList
      }

      def draw = {
        // should handle some of
        //   color, fill-rule, stroke, stroke-dasharray, stroke-dashoffset,
        //   stroke-linecap, stroke-linejoin, stroke-miterlimit, stroke-width,
        //   color-interpolation, color-rendering
        // and
        //   transform-list
        node match {
          case <rect></rect> =>
            val (x, y) = matchXY(node)
            val (width, height) = matchWH(node)
            val (fc, sc) = matchFillStroke(node)
            val (rx, ry) = matchRXY(node)
            if (rx != 0) {
              roundRectangle(x, y, width, height, rx, ry)
            } else {
              rectangle(x, y, width, height)
            }
          case <circle></circle> =>
            val (cx, cy) = matchXY(node, "cx", "cy")
            val rStr = (node \ "@r").toString
            val r = if (rStr == "") 0 else rStr.toDouble
            circle(cx, cy, r)
          case <ellipse></ellipse> =>
            val (cx, cy) = matchXY(node, "cx", "cy")
            val (rx, ry) = matchRXY(node)
            ellipse(cx, cy, rx, ry)
          case <line></line> =>
            val (x1, y1) = matchXY(node, "x1", "y1")
            val (x2, y2) = matchXY(node, "x2", "y2")
            line(x1, y1, x2, y2)
          case <polyline></polyline> =>
            val points = matchPoints(node)
            polyline(points)
          case <polygon></polygon> =>
            val points = matchPoints(node)
            polygon(points)
          case <path></path> =>
            val d = (node \ "@d").toString
            tCanvas.figure0.path(d)
          case <g>{ shapes @ _* }</g> =>
            for (s <- shapes) {
              svgShape(s)
            }
          case <svg>{ shapes @ _* }</svg> =>
            for (s <- shapes) {
              svgShape(s)
            }
          case _ => // unknown element, ignore
        }
        this
      }
    }
    object SvgShape {
      def apply(node: scala.xml.Node) = {
        val shape = new SvgShape(node)
        shape.draw
      }
    }
    def svgShape(node: scala.xml.Node) = SvgShape(node)

  }

  object CanvasAPI  /* extends core.Figure */ {
    import core._

    // impl - prefixed with z to push it down in the code completion list
    val zimpl = tCanvas.figure0
    def clear() = zimpl.clear()
    def fgClear() = zimpl.fgClear()
    def stopRefresh() = zimpl.stopRefresh()
    def setPenColor(color: Color) = zimpl.setPenColor(color)
    def setPenThickness(t: Double) = zimpl.setPenThickness(t)
    def setFillColor(color: Color) = zimpl.setFillColor(color)

    def point(x: Double, y: Double) = zimpl.point(x, y)
    def line(p1: Point, p2: Point) = zimpl.line(p1, p2)
    def line(x0: Double, y0: Double, x1: Double, y1: Double) = zimpl.line(x0, y0, x1, y1)
    def ellipse(center: Point, w: Double, h: Double) = zimpl.ellipse(center, w, h)
    def ellipse(cx: Double, cy: Double, w: Double, h: Double) = zimpl.ellipse(cx, cy, w, h)
    def arc(onEll: Ellipse, start: Double, extent: Double) = zimpl.arc(onEll, start, extent)
    def arc(cx: Double, cy: Double, w: Double, h: Double, 
            start: Double, extent: Double) = zimpl.arc(cx, cy, w, h, start, extent)
    def arc(cx: Double, cy: Double, r: Double, start: Double, extent: Double) = zimpl.arc(cx, cy, r, start, extent)
    def arc(cp: Point, r: Double, start: Double, extent: Double) = zimpl.arc(cp, r, start, extent)
    def circle(cp: Point, radius: Double) = zimpl.circle(cp, radius)
    def circle(cx: Double, cy: Double, radius: Double) = zimpl.circle(cx, cy, radius)
    def rectangle(bLeft: Point, tRight: Point) = zimpl.rectangle(bLeft, tRight)
    def rectangle(x0: Double, y0: Double,  w: Double, h: Double) = zimpl.rectangle(x0, y0, w, h)
    def text(content: String, x: Double, y: Double) = zimpl.text(content, x, y)
    def text(content: String, p: Point) = zimpl.text(content, p)
    def refresh(fn: => Unit) = zimpl.refresh(fn)
  }

}

class InterpOutputHandler(ctx: RunContext) {
  val Log = Logger.getLogger(getClass.getName);

  @volatile var errorSeen = false

  val OutputMode = 1
//  val ErrorMsgMode = 2
  val ErrorTextMode = 3
  val HatMode = 4
  val ErrorTextWithoutLinkMode = 5
  @volatile var currMode = OutputMode

  val errorPattern = java.util.regex.Pattern.compile("""(^<console>:\d+: )error:""")
  val exceptionPattern = java.util.regex.Pattern.compile("""^\w+(\.\w+)+(Exception|Error)""")
  @volatile var interpOutputSuppressed = false

  def showInterpOutput(lineFragment: String) {
    if (!interpOutputSuppressed) reportInterpOutput(lineFragment)
  }

  private def reportExceptionOutput(output0: String) {
    Log.info("Exception in interpreter output: " + output0)
    val lines = output0.split("\n")

    val output = if (lines.size > 5) {
      lines.take(5).mkString("\n") + "......\n"
    }
    else {
      output0
    }
    ctx.println(output)
  }

  private def reportNonExceptionOutput(output: String) {
    // Note - the call sequence has changed, with \r\ns coming
    // in separate calls. Hence the if block right at the beginning
    // of the method below
    // Interp sends in one line at a time for error output
    // we get three calls for an error:
    // (1) err msg (2) err text (3) hat
    // Scala compiler code reference:
    // ConsoleReporter.printMessage() calls:
    // - ConsoleReporter.printMessage() [overloaded] to print error message
    // - printSourceLines(pos), which calls:
    // -- ConsoleReporter.printMessage() [overloaded] to print error text
    // -- printColumnMarker(pos) - to print hat

    def isBadMsg(msg: String) = {
      msg.contains("Unmatched closing brace")
    }

    if ((currMode != OutputMode) && (output == "\r\n" || output == "\n")) {
      ctx.println(output)
      return
    }

    currMode match {
      case OutputMode =>
        val m = errorPattern.matcher(output)
        if (m.find) {
          ctx.reportErrorMsg(output.substring(m.group(1).length, output.length))
          if (isBadMsg(output)) {
            currMode = ErrorTextWithoutLinkMode
          }
          else {
            currMode = ErrorTextMode
          }
        }
        else {
          ctx.reportOutput(output)
        }
      case ErrorTextMode =>
        ctx.reportErrorText(output)
        currMode = HatMode
      case ErrorTextWithoutLinkMode =>
        ctx.reportErrorMsg(output)
        currMode = HatMode
      case HatMode =>
        ctx.println(output)
        currMode = OutputMode
    }
  }

  def reportInterpOutput(output: String) {
    if (output == "") return

    if (exceptionPattern.matcher(output).find) {
      reportExceptionOutput(output)
    }
    else {
      reportNonExceptionOutput(output)
    }
  }

  def withOutputSuppressed[T](fn: => T): T = {
    interpOutputSuppressed = true
    var ret: T = null.asInstanceOf[T]
    try {
      ret = fn
    }
    finally {
      interpOutputSuppressed = false
    }
    ret
  }
}
