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
               "modules/ext/piccolo2d-core-1.3.jar",
               "modules/ext/piccolo2d-extras-1.3.jar",
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
        // Interesting fact:
        // If you make an object available via bind, the interface is not type-safe
        // If you make it available via interpret("val ="), its type safe
        // Observed via Staging.linesShape
        // TODO: reevaluate other binds
        interp.interpret("val Staging = net.kogics.kojo.staging.API")
//        interp.bind("Staging", "net.kogics.kojo.staging.Facade$", staging.Facade)
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
    
    def print(obj: Any): Unit = println(obj)
    def println(obj: Any): Unit = println(if (obj == null) "null" else obj.toString)

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

Here's a partial list of the available commands:
  forward(numSteps) - Moves the turtle forward a given number of steps
  back(numSteps) - Moves the turtle back a given number of steps
  turn(angle) - Turns the turtle through a specified angle. Angles are positive for counter-clockwise turns
  right() - Turns the turtle right
  right(angle) - Turns the turtle right through a specified angle
  left() - Turns the turtle left
  left(angle) - Turns the turtle left through a specified angle
  towards(x, y) - Turns the turtle towards the point (x, y)
  moveTo(x, y) - Moves the turtle to the point (x, y)
  setHeading(angle) - Sets the turtle's heading (the direction in which it is pointing)
  setPosition(x, y) - Sets the turtle's position (without making it draw any lines). The turtle's heading is not changed
  home() - Moves the turtle to its original location, and makes it point north
  write(text) - Makes the turtle write the specified text at its current location
  undo() - Undoes the last turtle command

  clear() - Clears the screen. To bring the turtle to the center of the window after this command, just resize the turtle canvas
  zoom(factor, cx, cy) - Zooms in by the given factor, and position (cx, cy) at the center of the turtle canvas
  setAnimationDelay(delay) - Sets the turtle's speed. The specified delay is the amount of time taken by the turtle to move through a distance of one hundred steps

  penDown() - Makes the turtle put its pen down - to draw lines as it moves. The pen is down by default
  penUp() - Makes the turtle not draw lines as it moves
  setPenColor(color) - Specifies the color of the pen that the turtle draws with
  setPenThickness(thickness) - Specifies the thickness (as an number) of the pen that the turtle draws with
  setFillColor(color) - Specifies the background color of the figures drawn by the turtle

  listPuzzles() - shows the names of the puzzles available in the system
  loadPuzzle(name) - loads the named puzzle
  clearPuzzlers() - clears out the puzzler turtles and the puzzles from the screen

  gridOn() - Shows a grid on the canvas
  gridOff() - Hides the grid
  beamsOn() - Shows crossbeams centered on the turtle - to help with solving puzzles
  beamsOff() - Hides the turtle crossbeams
  invisible() - Hides the turtle
  visible() - Makes the hidden turtle visible again

  newTurtle(x, y) - Makes a new turtle located at the point (x, y)
  turtle0 - gives you a handle to the original turtle.

  showScriptInOutput() - Enables the display of scripts in the output window when they run
  hideScriptInOutput() - Stops the display of scripts in the output window
  showVerboseOutput() - Enables the display of the output from the Scala interpreter. By default, output from the interpreter is shown only for single line scripts.
  hideVerboseOutput() - Stops the display of the output from the Scala interpreter.

  version - Displays the version of Scala being used
  repeat(n) {} - Repeats commands within braces n number of times
  println(string) or print(string) - Displays the given string in the output window
  readln(promptString) - Displays the given prompt in the output window and reads a line that the user enters
  readInt(promptString) - Displays the given prompt in the output window and reads an Integer value that the user enters
  readDouble(promptString) - Displays the given prompt in the output window and reads a Double-precision Real value that the user enters
  random(upperBound) - Returns a random Integer between 0 (inclusive) and upperBound (exclusive)
  randomDouble(upperBound) - Returns a random Double-precision Real between 0 (inclusive) and upperBound (exclusive)
  inspect(obj) - Explores the internal fields of the given object
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
    def zoomXY(xfactor: Double, yfactor: Double, cx: Double, cy: Double) =
      tCanvas.zoomXY(xfactor, yfactor, cx, cy)

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
