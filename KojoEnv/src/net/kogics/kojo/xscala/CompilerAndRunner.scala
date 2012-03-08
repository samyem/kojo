/*
 * Copyright (C) 2010 Lalit Pant <pant.lalit@gmail.com>
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

import util.Utils

import scala.tools.nsc._
import reporters._
import util._
import io._
import java.io._
import interpreter.AbstractFileClassLoader

import java.lang.{ Class, ClassLoader }
import scala.tools.nsc.util.ScalaClassLoader
import ScalaClassLoader.URLClassLoader
import java.net.{ MalformedURLException, URL }
import scala.tools.util.PathResolver
import java.lang.reflect
import reflect.InvocationTargetException
import KojoInterpreter.IR

trait CompilerListener {
  def error(msg: String, line: Int, column: Int, offset: Int, lineContent: String)
  def warning(msg: String, line: Int, column: Int)
  def info(msg: String, line: Int, column: Int)
  def message(msg: String)
}

// This class borrows code and ideas from scala.tools.nsc.Interpreter
class CompilerAndRunner(makeSettings: () => Settings, initCode: => Option[String], listener: CompilerListener) extends StoppableCodeRunner {
  
  var counter = 0 
  // The Counter above is used to define/create a new wrapper object for every run. The calling of the entry() 
  //.method within this object results in the initialization of the object, which causes the user submitted 
  // code to run.
  // If we don't increment the counter, the user code will not run (an object is initialized only once)
  // If this approach turns out to be too memory intensive, I'm sure there are other ways of running user 
  // submitted code.'
  val prefixHeader = "object Wrapper"
  val prefix0 = """ {
  val builtins = net.kogics.kojo.xscala.Builtins.instance
  import builtins._
  val Staging = net.kogics.kojo.staging.API
  val Mw = net.kogics.kojo.mathworld.MathWorld.instance
  def entry() {
    // noop
  }
""" 

  def prefix = "%s%s\n" format(prefix0, initCode.getOrElse(""))

  def prefixLines = prefix.lines.size

  val codeTemplate = """%s
%s
}
"""

  var offsetDelta: Int = _

  val virtualDirectory = new VirtualDirectory("(memory)", None)

  def makeSettings2() = {
    val stng = makeSettings()
    stng.outputDirs.setSingleOutput(virtualDirectory)
    stng.deprecation.value = true
    stng
  }
  
  val settings = makeSettings2()
  
  lazy val compilerClasspath: List[URL] = new PathResolver(settings) asURLs

  private var _classLoader: AbstractFileClassLoader = null
  def resetClassLoader() = _classLoader = makeClassLoader()
  def classLoader: AbstractFileClassLoader = {
    if (_classLoader == null)
      resetClassLoader()

    _classLoader
  }
  private def makeClassLoader(): AbstractFileClassLoader = {
    val parent =
      if (parentClassLoader == null)  ScalaClassLoader fromURLs compilerClasspath
    else                            new URLClassLoader(compilerClasspath, parentClassLoader)

    new AbstractFileClassLoader(virtualDirectory, parent)
  }
  private def loadByName(s: String): Class[_] = (classLoader loadClass s)
  private def methodByName(c: Class[_], name: String): reflect.Method =
    c.getMethod(name, classOf[Object])

  protected def parentClassLoader: ClassLoader =
    this.getClass.getClassLoader()

  def getInterpreterClassLoader() = classLoader

  // Set the current Java "context" class loader to this interpreter's class loader
  def setContextClassLoader() = classLoader.setAsContext()

  val reporter = new Reporter {
    override def info0(position: Position, msg: String, severity: Severity, force: Boolean) {
      severity.count += 1
      lazy val line = position.line - prefixLines - 1 // we added an extra line after the prefix in the code template. Take it off
      lazy val offset = position.startOrPoint - offsetDelta - 1 // we added an extra newline char after the prefix
      severity match {
        case ERROR if position.isDefined =>
          listener.error(msg, line, position.column, offset, position.lineContent)
        case WARNING if position.isDefined =>
          listener.warning(msg, line, position.column)
        case INFO if position.isDefined =>
          listener.info(msg, line, position.column)
        case _ =>
          listener.message(msg)
      }
    }
  }

  val compiler = new Global(settings, reporter)
  
  def pfxWithCounter = Utils.stripCR("%s%d%s" format(prefixHeader, counter, prefix))

  def compile(code0: String, stopPhase: List[String] = List("selectiveanf")) = {
    val pfx = pfxWithCounter
    offsetDelta = pfx.length
    val code = Utils.stripCR(codeTemplate format(pfx, code0))
    
    compiler.settings.stopAfter.value = stopPhase
    val run = new compiler.Run
    reporter.reset
    run.compileSources(List(new BatchSourceFile("scripteditor", code)))
    if (reporter.hasErrors) IR.Error else IR.Success
  }

  def compileAndRun(code0: String) = {
    counter += 1
    val result = compile(code0, Nil) 
    
    if (result == IR.Success) {
      if (Thread.interrupted) {
        listener.message("Thread interrupted")
        IR.Error
      }
      else {
        try {
          val loadedResultObject = loadByName("Wrapper%d" format(counter))
          loadedResultObject.getMethod("entry").invoke(loadedResultObject)
          IR.Success
        }
        catch {
          case t: Throwable =>
            var realT = t
            while (realT.getCause != null) {
              realT = realT.getCause
            }
            if (realT.isInstanceOf[InterruptedException]) {
              listener.message("Execution thread interrupted.")
            }
            else {
              listener.message(Utils.stackTraceAsString(realT))
            }
            IR.Error
        }
      }
    }
    else {
      IR.Error
    }
  }

  def stop(interpThread: Thread) {
    interpThread.interrupt()
  }

  def parse(code0: String, browseAst: Boolean) = {
    compiler.settings = makeSettings2()
    val pfx = pfxWithCounter
    offsetDelta = pfx.length
    val code = Utils.stripCR(codeTemplate format(pfx, code0))

    compiler.settings.stopAfter.value = stopPhase()
    if (browseAst) {
      compiler.settings.browse.value = stopPhase()
    }
    val run = new compiler.Run
    reporter.reset
    try {
      run.compileSources(List(new BatchSourceFile("scripteditor", code)))
    }
    finally {
      compiler.settings = makeSettings2()
    }

//    compiler.settings.browse.value = List()

    if (reporter.hasErrors) {
      IR.Error
    }
    else {
//      val tree = run.units.next.body
//      listener.message(tree.toString)
      compiler.printAllUnits()
      IR.Success
    }
  }

  // phase after which you want to stop
  private def stopPhase() = {
    val ret = Builtins.instance.astStopPhase
    if (ret != null && ret != "") List(ret) else Nil
  }

  
  val preporter = new Reporter {
    override def info0(position: Position, msg: String, severity: Severity, force: Boolean) {
    }
  }
  val pcompiler = new interactive.Global(settings, preporter) 
  
  import core.CompletionInfo
  def completions(code0: String, offset: Int): List[CompletionInfo] = {
    def addParensAfterOffset(c: String) = {
      "%s  () // %s" format(c.substring(0, offset), c.substring(offset, c.length))
    }
    def addResultColon(str: String) = {
      val li = str.lastIndexOf(')')
      "%s: %s" format(str.substring(0,li+1), str.substring(li+1, str.length))
    }
   
    import interactive._

    val pfx = pfxWithCounter
    val offsetDelta = pfx.length
    val code = Utils.stripCR(codeTemplate format(pfx, addParensAfterOffset(code0)))
    
    val source = new BatchSourceFile("scripteditor", code)
    val pos = new OffsetPosition(source, offset + offsetDelta + 1)

    var r1 = new Response[Unit]
    pcompiler.askReload(List(source), r1)

    var resp = new Response[List[pcompiler.Member]]
    pcompiler.askTypeCompletion(pos, resp)
    resp.get match {
      case Left(x) => 
        x filter { e =>  
          (e.sym.isMethod && !e.sym.isConstructor && e.sym.isPublic) || 
          (e.sym.isValue && !e.sym.isMethod && e.sym.nameString != "this")
        } map { e => 
          var prio = 100

          val tm = e.asInstanceOf[pcompiler.TypeMember]
          if (tm.viaView != pcompiler.NoSymbol) prio += 20
          if (tm.inherited == true) prio += 10
          // give vals and vars lower priority because we can't seem to distinguish 
          // between private and public vals/vars.
          // This way they go below the methods
          if (e.sym.isValue && !e.sym.isMethod) prio += 5 

          e.tpe match {
            case mt: pcompiler.MethodType => CompletionInfo(e.sym.nameString, 
                                                            mt.params.map(_.nameString.replace("$", "")), 
                                                            mt.paramTypes.map(_.toString), 
                                                            mt.resultType.toString,
                                                            prio)
            case pt: pcompiler.PolyType => CompletionInfo(e.sym.nameString, 
                                                          pt.resultType.params.map(_.nameString.replace("$", "")), 
                                                          pt.resultType.paramTypes.map(_.toString), 
                                                          pt.resultType.resultType.toString,
                                                          prio)
            case nt: pcompiler.NullaryMethodType => CompletionInfo(e.sym.nameString, 
                                                                   Nil, 
                                                                   Nil, 
                                                                   nt.resultType.toString,
                                                                   prio)
            case vt: pcompiler.UniqueTypeRef => CompletionInfo(e.sym.nameString, 
                                                               Nil, 
                                                               Nil, 
                                                               vt.resultType.toString,
                                                               prio,
                                                               true)
            case t @ _ => CompletionInfo(e.sym.nameString, List(t.getClass.getName), List("todo2"), "todo2", prio)
          }
        }
      case Right(y) => println("Completion warning: %s" format(y)); Nil  
    }
  }
}
