/*
 * Copyright (C) 2011 Lalit Pant <pant.lalit@gmail.com>
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

import scala.tools.nsc.interpreter._
import java.io.PrintWriter

object KojoInterpreter {
  type Settings = scala.tools.nsc.Settings
  val IR = scala.tools.nsc.InterpreterResults
}

class KojoInterpreter(settings: KojoInterpreter.Settings, out: PrintWriter) extends StoppableCodeRunner {
  val interp = new scala.tools.nsc.Interpreter(settings, out) {
    override protected def parentClassLoader = classOf[KojoInterpreter].getClassLoader
  }
  interp.setContextClassLoader()

  def bind(name: String, boundType: String, value: Any) = interp.bind(name, boundType, value)
  def interpret(code: String) = interp.interpret(code)
  def completions(id: String) = interp.methodsOf(id)
  def unqualifiedIds = interp.unqualifiedIds
  def stop(interpThread: Thread) {
    interpThread.interrupt()
  }
  
  def evalExpr[T: Manifest](line: String): T = interp.evalExpr(line)
}
