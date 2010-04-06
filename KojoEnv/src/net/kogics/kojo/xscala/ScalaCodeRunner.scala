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
            safeProcess {
              reply(CompletionResponse(methodCompletions(str)))
            }

          case VarCompletionRequest(str) =>
            safeProcess {
              reply(CompletionResponse(varCompletions(str)))
            }

          case KeywordCompletionRequest(str) =>
            safeProcess {
              reply(CompletionResponse(keywordCompletions(str)))
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
      // warning! calling into interp while a computation is running within the actor
      // might lead to problems
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

  object Builtins extends SCanvas {
    type Turtle = core.Turtle
    type Color = java.awt.Color
    
    PuzzleLoader.init()
    val Random = new java.util.Random

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

    def forward(n: Double) = tCanvas.forward(n)
    def turn(angle: Double) = tCanvas.turn(angle)
    def clear() = tCanvas.clear()
    def clearPuzzlers() = tCanvas.clearPuzzlers()
    def penUp() = tCanvas.penUp()
    def penDown() = tCanvas.penDown()
    def setPenColor(color: Color) = tCanvas.setPenColor(color)
    def setPenThickness(t: Double) = tCanvas.setPenThickness(t)
    def setFillColor(color: Color) = tCanvas.setFillColor(color)
    def beamsOn() = tCanvas.beamsOn()
    def beamsOff() = tCanvas.beamsOff()
    def write(text: String) = tCanvas.write(text)
    def visible() = tCanvas.visible()
    def invisible() = tCanvas.invisible()

    def towards(x: Double, y: Double) = tCanvas.towards(x, y)
    def position: core.Point = tCanvas.position
    def heading: Double = tCanvas.heading

    def jumpTo(x: Double, y: Double) = tCanvas.jumpTo(x, y)
    def moveTo(x: Double, y: Double) = tCanvas.moveTo(x, y)

    def animationDelay = tCanvas.animationDelay
    def setAnimationDelay(d: Long) = tCanvas.setAnimationDelay(d)

    def newTurtle() = tCanvas.newTurtle(0, 0)
    def newTurtle(x: Int, y: Int) = tCanvas.newTurtle(x, y)
    val turtle0 = tCanvas.turtle0
    val figure0 = tCanvas.figure0

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
    import core._

    implicit def tupleDToPVector(tuple: (Double, Double)) = PVector(tuple._1, tuple._2)
    implicit def tupleIToPVector(tuple: (Int, Int)) = PVector(tuple._1, tuple._2)

    class PVector (val x: Double, val y: Double) {
      def size = Math.sqrt(x * x + y * y)
      def mag = size
      def dist (that: PVector): Double = (this - that) size
      def dot (that: PVector) = this.x * that.x + this.y * that.y
      def normalize = this / (this size)
      def +(that: PVector) = PVector(this.x + that.x, this.y + that.y)
      def -(that: PVector) = PVector(this.x - that.x, this.y - that.y)
      def unary_- = PVector(-x, -y)
      def *(value: Double) = PVector(x * value, y * value)
      def mult(a: Double, b: Double) = PVector(x * a, y * b)
      def /(v: Double) = {
        if (v == 0) error("Staging.PVector: can't divide by zero")
        else {
          PVector(x / v, y / v)
        }
      }

      def point = {
        tCanvas.figure0.point(this.x, this.y)
        this
      }

      def line (that: PVector) = {
        tCanvas.figure0.line(this.x, this.y, that.x, that.y)
        that
      }

      def rect (that: PVector) = {
        val bLeftX = if (this.x <= that.x) this.x else that.x
        val bLeftY = if (this.y <= that.y) this.y else that.y
        val uRightX = if (this.x <= that.x) that.x else this.x
        val uRightY = if (this.y <= that.y) that.y else this.y
        tCanvas.figure0.rectangle(bLeftX, bLeftY, uRightX, uRightY)
        that
      }

      def triangle (v1: PVector, v2: PVector) = {
        this line v1 line v2 line this
      }

      def quad (v1: PVector, v2: PVector, v3: PVector) = {
        this line v1 line v2 line v3 line this
      }

      def arc (w: Double, h: Double, start: Double, extent: Double) = {
        tCanvas.figure0.arc(this.x, this.y, w, h, start, extent)
        this
      }

      def ellipse (w: Double, h: Double) = {
        tCanvas.figure0.ellipse(this.x, this.y, w, h)
        this
      }

      def circle (radius: Double) = {
        tCanvas.figure0.circle(this.x, this.y, radius)
        this
      }

      override def toString = "Staging.PVector(" + x + ", " + y + ")"
    }
    object PVector {
      def apply (x: Double, y: Double) = new PVector(x, y)
    }

    val O = PVector(0, 0)

    def fill (color: java.awt.Color) { tCanvas.figure0.setFillColor(color) }
    def noFill { tCanvas.figure0.setFillColor(null) }
    def stroke (color: java.awt.Color) { tCanvas.figure0.setPenColor(color) }
    def noStroke { tCanvas.figure0.setPenColor(null) }
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
  val exceptionPattern = java.util.regex.Pattern.compile("""^\w+(\.\w+)+Exception""")
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
