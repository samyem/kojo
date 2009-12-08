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
package net.kogics.kojo.xscala

import java.io._
import java.awt.Color
import scala.actors.Actor._
import scala.tools.nsc.{Interpreter, InterpreterResults => IR, Settings}

import java.util.logging._

import net.kogics.kojo.util._
import net.kogics.kojo.core._

class ScalaCodeRunner(ctx: net.kogics.kojo.RunContext, tCanvas: SCanvas) {
  val Log = Logger.getLogger(getClass.getName);

  val OutputDelimiter = "---\n"
  val Marker = "\u0000"

  @volatile var initDone = false

  System.setProperty("java.class.path", createCp(
      List("modules/ext/scala-library.jar",
           "modules/ext/scala-compiler.jar",
           "modules/net-kogics-kojo.jar",
           "modules/ext/piccolo2d-core-1.3-SNAPSHOT.jar",
           "modules/ext/piccolo2d-extras-1.3-SNAPSHOT.jar"
      )
    ))

  // Scala Interpreter cannot be loaded by Kojo as of Revision 19285 because of changes in Settings
  // and MainGenericRunner
  // I need to follow up with the Scala folks about this when I have some time
  // Fix for now reverts the behavior of Settings to Revision 19284 by subclassing it and overriding the
  // classpathDefault method
  val iSettings = new Settings {
    private def syspropopt(name: String): Option[String] = onull(System.getProperty(name))

    override protected def classpathDefault =
      syspropopt("env.classpath") orElse syspropopt("java.class.path") getOrElse ""
  }

  val interpOutput = new PipedInputStream
  val pipedOutput = new PipedOutputStream(interpOutput)

  val interp = new Interpreter(iSettings, new NewLinePrintWriter(pipedOutput)) {
    override protected def parentClassLoader = Thread.currentThread.getContextClassLoader
  }
  interp.setContextClassLoader
  
  val interpOutputReader = new Runnable {
    def readOutput() {
      // Runs on Interpreter pipe reader thread
      val reader = new BufferedReader(new InputStreamReader(interpOutput))
      val buf = new Array[Char](1024)
      var nbytes = reader.read(buf)
      while (nbytes != -1) {
        val iOutput = new String(buf, 0, nbytes)
        Log.info("Output received from Interpreter: " + iOutput)
        showOutput(iOutput)
        nbytes = reader.read(buf)
      }
    }

    def run() {
      try {
        readOutput()
      }
      catch {
        case e: Exception =>
          Log.warning("Output Reader Exception: " + Utils.stackTraceAsString(e))
      }
      
      val dos = new DataOutputStream(pipedOutput)
      dos.writeBytes("Resetting Output Reader.\n"); dos.flush()
      run()
    }
  }

  new Thread(interpOutputReader).start

// Test Pipe Exceptions
// TODO: This needs to go into a unit test
//  val rxx = new Runnable {
//    def run {
//      Thread.sleep(10000)
//      val dos = new DataOutputStream(pipedOutput)
//      dos.writeBytes("Hi There.\n")
//      dos.flush()
////      Thread.sleep(5000)
//      new Thread(this).start()
//    }
//  }
//
//  new Thread(rxx).start()

  object Builtins extends SCanvas {
    type Sprite = net.kogics.kojo.core.Sprite
    type Color = java.awt.Color

    PuzzleLoader.init()

    private val throttler = new Throttler {}

    def println(obj: Any): Unit = println(obj.toString)

    def println(s: String): Unit = {
      // Runs on Actor pool (interpreter) thread
      showOutput(s + "\n")
      throttler.throttle
    }

    def version = println("Scala " + scala.tools.nsc.Properties.versionString)

    def welcome = {
      println("""Welcome to Kojo.

Run the help command, i.e. type help and press Ctrl+Enter in the script window, if you need assistance.
""")
    }

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
  towards(x, y) - Make the turtle turn towards the point (x, y)
  moveTo(x, y) - Make the turtle move to the point (x, y)
  write(text) - Make the turtle write the specified text at its current location
  undo() - Undo the last turtle command

  clear() - Clear the screen. To bring the turtle to the center after this step, just resize the turtle window.

  penDown() - Make the turtle put its pen down to draw a line as it moves. The pen is down by default
  penUp() - Make the turtle not draw a line as it moves
  setPenColor(color) - Specify the color of the pen that the turtle draws with
  setPenThickness(thickness) - Specify the thickness (as an number) of the pen that the turtle draws with
  setFillColor(color) - Specify the fill color of the areas drawn by the turtle

  listPuzzles - show the names of the puzzles available in the system
  loadPuzzle(name) - load the named puzzle
  clearPuzzlers() - clear out the puzzler turtles and the puzzles from the screen

  gridOn() - Show a grid on the canvas
  gridOff() - Hide the grid
  beamsOn() - Show crossbeams centered on the turtle - to help with solving puzzles
  beamsOff() - Hide the turtle crossbeams
  invisible() - Hide the turtle
  visible() - Make the hidden turtle visible again

  repeat(n) {} - Repeat commands within braces n number of times

  newTurtle(x, y) - Make a new turtle located at the point (x, y)
  turtle0 - gives you a handle to the origianl turtle.
""")
    }

    def repeat(n: Int) (fn: => Unit) {
      for (i <- 1 to n) {
        fn
        throttler.throttle
      }
    }

    def gridOn() = tCanvas.gridOn()
    def gridOff() = tCanvas.gridOff()

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
    def position: (Double, Double) = tCanvas.position
    def heading: Double = tCanvas.heading

    def jumpTo(x: Double, y: Double) = tCanvas.jumpTo(x, y)
    def moveTo(x: Double, y: Double) = tCanvas.moveTo(x, y)
    def point(x: Double, y: Double) = tCanvas.point(x, y)


    def animationDelay = tCanvas.animationDelay
    def setAnimationDelay(d: Long) = tCanvas.setAnimationDelay(d)

    def newTurtle() = tCanvas.newTurtle(0, 0)
    def newTurtle(x: Int, y: Int) = tCanvas.newTurtle(x, y)
    val turtle0 = tCanvas.turtle0

    def newPuzzler(x: Int, y: Int) = tCanvas.newPuzzler(x, y)
    def pathToPolygon() = tCanvas.pathToPolygon()
    def pathToParallelogram() = tCanvas.pathToParallelogram()
    def undo() = tCanvas.undo()


    def listPuzzles = PuzzleLoader.listPuzzles
    def clearOutput() = ctx.clearOutput()

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

  interp.bind("predef", "net.kogics.kojo.xscala.ScalaCodeRunner", this)
  interp.interpret("val builtins = predef.Builtins")
  interp.interpret("import predef.Builtins._")
  interp.interpret("\"" + Marker + "\"")

  val codeRunner = startCodeRunner()

  def createCp(xs: List[String]): String = {
    val ourCp = new StringBuilder

//    val oldCp = System.getProperty("java.class.path")
//    ourCp.append(prefix)

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

  def showOutput(lineFragment: String) {
    if (!initDone) {
      if (lineFragment.contains(Marker)) initDone = true
    }
    else {
      if (!InterpreterManager.interruptionInProgress) ctx.reportOutput(lineFragment)
    }
  }

  def unconditionallyShowOutput(s: String) = ctx.reportOutput(s)

  def maybeOutputDelimiter {
    val output = ctx.getCurrentOutput

    if (output.length > 0 && !output.endsWith(OutputDelimiter))
      showOutput(OutputDelimiter)
  }

  def runCode(code: String) = synchronized {
    // Runs on swing thread
//    Log.info("Running Code:\n---\n%s\n---\n" format(code))
    codeRunner ! RunCode(code)
  }
  

  object InterpreterManager {
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
      
      unconditionallyShowOutput("Attempting to stop Script...\n")

      if (interpreterThread.isDefined) {
        Log.info("Interrupting Interpreter thread...")
        interruptTimer = Some(Utils.schedule(4) {
            // don't need to clean out interrupt state because Kojo needs to be shut down anyway
            // and in fact, cleaning out the interrupt state will mess with a delayed interruption
            Log.info("Interrupt timer fired")
            unconditionallyShowOutput("Unable to stop script.\nPlease restart the Kojo Environment unless you see a 'Script Stopped' message soon.\n")
          })
        interpreterThread.get.interrupt
      }
      else {
        unconditionallyShowOutput("Animation Stopped.\n")
      }
    }

    def interpreterStarted {
      // Runs on Actor pool thread
      interpreterThread = Some(Thread.currentThread)
      // do this here instead of the done method to allow filtering of
      // interruption stack trace from interpreter
      interruptTimer = None
      maybeOutputDelimiter
      ctx.interpreterStarted
    }

    def interpreterDone {
      Log.info("Interpreter Done notification received")
      // Runs on Actor pool thread
      // might not be called for runaway computations
      // in which case Kojo has to be restarted
      if (interruptTimer.isDefined) {
        Log.info("Cancelling interrupt timer")
        interruptTimer.get.stop
        Log.info("Requesting Script Stopped Output")
        unconditionallyShowOutput("Script Stopped.\n")
      }
      interpreterThread = None
      ctx.interpreterDone
    }
  }

  def interruptInterpreter() = InterpreterManager.interruptInterpreter()

  case class RunCode(code: String)

  def startCodeRunner() = actor {

    val varPattern = java.util.regex.Pattern.compile("\\bvar\\b")

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

    while(true) {
      receive {
        // Runs on Actor pool thread
        case RunCode(code) =>
          try {
            Log.info("CodeRunner actor running code:\n---\n%s\n---\n" format(code))
//            val ct = Thread.currentThread
//            Log.info("CodeRunner actor running on thread: %s, Id: %d" format(ct.getName, System.identityHashCode(ct)))
            InterpreterManager.interpreterStarted
            val ret = interpret(code)
            Log.info("CodeRunner actor done running code. Return value %s" format (ret.toString))
            if (ret == IR.Incomplete) showOutput("Incomplete code fragment.\n")
            if (ret != IR.Success) ctx.reportRunError
          }
          catch {
            case t: Throwable => Log.log(Level.SEVERE, "Interpreter Problem", t)
          }
          finally {
            Log.info("CodeRunner actor doing final handling for code.")
            InterpreterManager.interpreterDone
          }
      }
    }
  }

  import CodeCompletionUtils._

  def completions(identifier: String) = {
    Log.fine("Finding Identifier completions for: " + identifier)
    val completions = interp.membersOfIdentifier(identifier).filter {s => !MethodDropFilter.contains(s)}
    Log.fine("Completions: " + completions)
    completions
  }

  def methodCompletions(str: String): (List[String], Int) = synchronized {
    val (oIdentifier, oPrefix) = findIdentifier(str)
    val prefix = if(oPrefix.isDefined) oPrefix.get else ""
    if (oIdentifier.isDefined) {
      (completions(oIdentifier.get).filter {s => s.startsWith(prefix)}, prefix.length)
    }
    else {
      val c1s = completions("builtins").filter {s => s.startsWith(prefix)}
      Log.fine("Filtered builtins completions for prefix '%s' - %s " format(prefix, c1s))
      (c1s, prefix.length)
    }
  }

  def varCompletions(str: String): (List[String], Int) = synchronized {

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
      val c2s = Keywords.filter {s => s.startsWith(prefix)}
      (c2s, prefix.length)
    }
  }
}

class NewLinePrintWriter(out: OutputStream) extends PrintWriter(out, true) {
  override def print(string: String) {
    // scala.Console.println("Received from Interpreter: " + string)
    super.print(string)
    flush()
  }
}