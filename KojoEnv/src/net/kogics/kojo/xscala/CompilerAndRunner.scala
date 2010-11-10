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

package net.kogics.kojo.xscala

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
import scala.tools.nsc.{InterpreterResults => IR}

trait CompilerListener {
  def error(msg: String, line: Int, column: Int, offset: Int, lineContent: String)
  def warning(msg: String, line: Int, column: Int)
  def info(msg: String, line: Int, column: Int)
  def message(msg: String)
}

// This class borrows code and ideas from scala.tools.nsc.Interpreter
class CompilerAndRunner(settings: Settings, listener: CompilerListener) {
  var counter = 0

  val prefix = """object Wrapper%d {
  val builtins = net.kogics.kojo.xscala.Builtins.instance
  import builtins._
  val Staging = net.kogics.kojo.staging.API
  val Mw = net.kogics.kojo.mathworld.MathWorld.instance

"""

  val prefixLines = prefix.lines.size

  val codeTemplate = """%s%s

  def entry() {
    // noop
  }
}
"""
  
  var offsetDelta: Int = _

  val virtualDirectory = new VirtualDirectory("(memory)", None)

//  val settings = new Settings()
//  settings.usejavacp.value = true
  settings.outputDirs.setSingleOutput(virtualDirectory)

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
    override def info0(position: Position, msg: String, severity: Severity, force: Boolean) = {
      severity.count += 1
      lazy val line = position.line - prefixLines
      lazy val offset = position.startOrPoint - offsetDelta - (line-1)
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

  def compileAndRun(code0: String) = {
    counter += 1
    val pfx = prefix format(counter)
    offsetDelta = pfx.length
    val code = codeTemplate format(pfx, code0)
    val codeBytes = code.getBytes

//    val file = new VirtualFile("ab", "cd") {
//      override def input : InputStream = new ByteArrayInputStream(codeBytes)
//      override def sizeOption: Option[Int] = Some(codeBytes.size)
//    }

    val run = new compiler.Run
    reporter.reset
//    run.compileFiles(List(file))
    run.compileSources(List(new BatchSourceFile("scripteditor", code)))

    if (!reporter.hasErrors) {
      try {
        val loadedResultObject = loadByName("Wrapper%d" format(counter))
        loadedResultObject.getMethod("entry").invoke(loadedResultObject)
        IR.Success
      }
      catch {
        case t: Throwable => IR.Error
      }
    }
    else {
      IR.Error
    }

  }
}
