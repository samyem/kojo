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

import util._
import core._

import java.io._
import java.awt.Color
import scala.actors.Actor
import scala.tools.nsc.{Interpreter, InterpreterResults => IR, Settings}

import java.util.logging._
import org.openide.ErrorManager;

class ScalaCodeRunner(val ctx: RunContext, val tCanvas: SCanvas, val storyTeller: story.StoryTeller) extends CodeRunner {
  val Log = Logger.getLogger(getClass.getName)
  val builtins = Builtins.initedInstance(this)
  val outputHandler = new InterpOutputHandler(ctx)
  val codeRunner = startCodeRunner()

  def println(s: String) = ctx.println(s)

  def runCode(code: String) {
    // Runs on swing thread
    codeRunner ! RunCode(code)
  }

  def compileRunCode(code: String) {
    codeRunner ! CompileRunCode(code)
  }

  def compileCode(code: String) {
    codeRunner ! CompileCode(code)
  }

  def parseCode(code: String, browseAst: Boolean) {
    codeRunner ! ParseCode(code, browseAst)
  }

  def interruptInterpreter() = InterruptionManager.interruptInterpreter()

  case object Init
  case class RunCode(code: String)
  case class CompileRunCode(code: String)
  case class CompileCode(code: String)
  case class ParseCode(code: String, browseAst: Boolean)
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
    var compiler: CompilerAndRunner = _
    
//    val varPattern = java.util.regex.Pattern.compile("\\bvar\\b")
    val storyPattern = java.util.regex.Pattern.compile("\\bstClear()\\b")

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
              initCompiler()
            }

          case CompileCode(code) =>
            try {
              Log.info("CodeRunner actor compiling code:\n---\n%s\n---\n" format(code))
              InterruptionManager.onInterpreterStart()
              ctx.onInterpreterStart(code)

              val ret = compile(code)
              Log.info("CodeRunner actor done compiling code. Return value %s" format (ret.toString))

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

          case CompileRunCode(code) =>
            try {
              Log.info("CodeRunner actor compiling/running code:\n---\n%s\n---\n" format(code))
              InterruptionManager.onInterpreterStart()
              ctx.onInterpreterStart(code)

              val ret = compileAndRun(code)
              Log.info("CodeRunner actor done compiling/running code. Return value %s" format (ret.toString))

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

          case ParseCode(code, browseAst) =>
            try {
              Log.info("CodeRunner actor parsing code:\n---\n%s\n---\n" format(code))
              InterruptionManager.onInterpreterStart()
              ctx.onInterpreterStart(code)

              val ret = compiler.parse(code, browseAst)
              Log.info("CodeRunner actor done parsing code. Return value %s" format (ret.toString))

              if (ret != null) {
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

    val jars = List("modules/ext/scala-library.jar",
                    "modules/ext/scala-compiler.jar",
                    "modules/net-kogics-kojo.jar",
                    "modules/ext/piccolo2d-core-1.3.jar",
                    "modules/ext/piccolo2d-extras-1.3.jar",
                    "modules/ext/geogebra_main.jar",
                    "modules/ext/geogebra_gui.jar",
                    "modules/ext/geogebra_cas.jar",
                    "modules/ext/geogebra.jar",
                    "modules/ext/jfugue-4.1.jar"
    )

    def initCompiler() {
      val iSettings = new Settings()
      iSettings.classpath.append(createCp(jars))
      compiler = new CompilerAndRunner(iSettings, new CompilerOutputHandler(ctx)) {
        override protected def parentClassLoader = classOf[ScalaCodeRunner].getClassLoader
      }
      compiler.setContextClassLoader()
    }

    def initInterp() {
      val iSettings = new Settings()
      iSettings.classpath.append(createCp(jars))

      interp = new Interpreter(iSettings, new GuiPrintWriter()) {
        override protected def parentClassLoader = classOf[ScalaCodeRunner].getClassLoader
      }
      interp.setContextClassLoader()

      outputHandler.withOutputSuppressed {
        interp.bind("predef", "net.kogics.kojo.xscala.ScalaCodeRunner", ScalaCodeRunner.this)
        interp.interpret("val builtins = predef.builtins")
        interp.interpret("import predef.builtins._")
        interp.bind("turtle0", "net.kogics.kojo.core.Turtle", tCanvas.turtle0)
        // Interesting fact:
        // If you make an object available via bind, the interface is not type-safe
        // If you make it available via interpret("val ="), its type safe
        // Observed via Staging.linesShape
        // TODO: reevaluate other binds
        interp.interpret("val Staging = net.kogics.kojo.staging.API")
//        interp.bind("Staging", "net.kogics.kojo.staging.Facade$", staging.Facade)
//        interp.bind("Mw", "net.kogics.kojo.core.GeomCanvas", geomCanvas)
        interp.interpret("val Mw = net.kogics.kojo.mathworld.MathWorld.instance")
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

      // add all jars in user's kojo lib dir to classpath
      val userDir = System.getProperty("netbeans.user")
      val libDir = userDir + File.separatorChar + "lib"
      val libDirFs = new File(libDir)
      if (libDirFs.exists) {
        val jarFiles = libDirFs.list(new FilenameFilter {
            override def accept(dir: File, name: String) = {
              name.endsWith(".jar")
            }
          })

        jarFiles.foreach {x =>
          ourCp.append(libDir)
          ourCp.append(File.separatorChar)
          ourCp.append(x)
          ourCp.append(File.pathSeparatorChar)
        }
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

    def compileAndRun(code: String): IR.Result = {
      compiler.compileAndRun(code)
    }

    def compile(code: String): IR.Result = {
      compiler.compile(code)
    }

    def needsLineByLineInterpretation(code: String): Boolean = {
      false
//      storyPattern.matcher(code).find()
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
      def methodFilter(s: String) = !MethodDropFilter.contains(s) && !InternalMethodsRe.matcher(s).matches

      Log.fine("Finding Identifier completions for: " + identifier)
      val completions = outputHandler.withOutputSuppressed {
        interp.methodsOf(identifier).distinct.filter {s => methodFilter(s)}
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
