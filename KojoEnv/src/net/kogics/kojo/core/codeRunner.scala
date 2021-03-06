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

package net.kogics.kojo.core

import java.util.concurrent.CountDownLatch

trait CodeRunner {
  def interruptInterpreter(): Unit
  def runCode(code: String): Unit
  def parseCode(code: String, browseAst: Boolean): Unit
  def compileCode(code: String): Unit
  def compileRunCode(code: String): Unit
  def varCompletions(prefix: Option[String]): (List[String], Int)
  def keywordCompletions(prefix: Option[String]): (List[String], Int)
  def memberCompletions(code: String, caretOffset: Int, objid: String, prefix: Option[String]): (List[CompletionInfo], Int)
  def activateTw(): Unit
  def activateStaging(): Unit
  def activateMw(): Unit
}

trait RunContext {
  def onInterpreterInit(): Unit
  def onInterpreterStart(code: String): Unit
  def onRunError(): Unit
  def onRunSuccess(): Unit
  def onRunInterpError(): Unit
  def onCompileStart(): Unit
  def onCompileError(): Unit
  def onCompileSuccess(): Unit
  def onInternalCompilerError(): Unit

  def kprintln(outText: String)
  def reportOutput(outText: String)
  def reportErrorMsg(errMsg: String)
  def reportErrorText(errText: String)
  def reportSmartErrorText(errText: String, line: Int, column: Int, offset: Int)

  def readInput(prompt: String): String

  def showScriptInOutput(): Unit
  def hideScriptInOutput(): Unit
  def showVerboseOutput(): Unit
  def hideVerboseOutput(): Unit
  def retainSingleLineCode(): Unit
  def clearSingleLineCode(): Unit
  def clearOutput(): Unit
  def setScript(code: String): Unit

  def inspect(obj: AnyRef): Unit
  def stopAnimation(): Unit
}

class ProxyCodeRunner(codeRunnerMaker: () => CodeRunner) extends CodeRunner {
  val latch = new CountDownLatch(1)
  @volatile var codeRunner: CodeRunner = _

  new Thread(new Runnable {
      def run {
        codeRunner = codeRunnerMaker()
        latch.countDown()
      }
    }).start()

  def interruptInterpreter() {
    latch.await()
    codeRunner.interruptInterpreter()
  }

  def runCode(code: String) {
    latch.await()
    codeRunner.runCode(code)
  }

  def compileRunCode(code: String) {
    latch.await()
    codeRunner.compileRunCode(code)
  }

  def compileCode(code: String) {
    latch.await()
    codeRunner.compileCode(code)
  }

  def parseCode(code: String, browseAst: Boolean) {
    latch.await()
    codeRunner.parseCode(code, browseAst)
  }

  def varCompletions(prefix: Option[String]): (List[String], Int) = {
    latch.await()
    codeRunner.varCompletions(prefix)
  }

  def keywordCompletions(prefix: Option[String]): (List[String], Int) = {
    latch.await()
    codeRunner.keywordCompletions(prefix)
  }
  
  def memberCompletions(code: String, caretOffset: Int, objid: String, prefix: Option[String]): (List[CompletionInfo], Int) = {
    latch.await()
    codeRunner.memberCompletions(code, caretOffset, objid, prefix)
  }
  
  def activateTw() {
    latch.await()
    codeRunner.activateTw()
  }
  
  def activateStaging() {
    latch.await()
    codeRunner.activateStaging()
  }
  
  def activateMw() {
    latch.await()
    codeRunner.activateMw()
  }
}
