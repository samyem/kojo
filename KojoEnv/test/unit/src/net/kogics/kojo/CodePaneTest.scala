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

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{CountDownLatch, TimeUnit}

import net.kogics.kojo.core.RunContext

class CodePaneTest {

  val fileStr = System.getProperty("nbjunit.workdir") + "../../../../../../../Kojo/build/cluster"
  val file = new java.io.File(fileStr)
  assertTrue(file.exists)
  System.setProperty("netbeans.dirs", fileStr)

  val runCtx = new RunContext {
    val currOutput = new StringBuilder()
    val success = new AtomicBoolean()
    val error = new AtomicBoolean()

    def onInterpreterInit() {}
    def showScriptInOutput() {}
    def hideScriptInOutput() {}
    def reportRunError() {}
    def readInput(prompt: String) = ""

    def reportOutput(lineFragment: String) {
      currOutput.append(lineFragment)
    }

    def onInterpreterStart() {}
    def clearOutput {currOutput.clear}
    def getCurrentOutput: String  = currOutput.toString

    def onRunError() {
      error.set(true)
      latch.countDown()
    }
    def onRunSuccess() {
      success.set(true)
      latch.countDown()
    }
    def onRunInterpError() {latch.countDown()}

    def reportErrorMsg(errMsg: String) {
      currOutput.append(errMsg)
    }
    def reportErrorText(errText: String) {
      currOutput.append(errText)
    }
  }

  val codeRunner = new xscala.ScalaCodeRunner(runCtx, sprite.SpriteCanvas.instance)
  val pane = new javax.swing.JEditorPane()
  val Delimiter = ""
  var latch: CountDownLatch = _

  def runCode() {
    latch = new CountDownLatch(1)
    codeRunner.runCode(pane.getText())
    latch.await()
  }

  def scheduleInterruption() {
    new Thread(new Runnable {
        def run() {
          Thread.sleep(1000)
          codeRunner.interruptInterpreter()
        }
      }).start()
  }

  // if we have more than five tests, we run out of heap space - maybe a leak in the Scala interpreter/compiler
  // subsystem. So we run (mostly) everything in one test
  @Test
  def testEvalSession = {

    pane.setText("12")
    runCtx.success.set(false)
    runCode()
    assertTrue(runCtx.success.get)
    assertEquals(stripCrLfs(Delimiter) +
                 "res0: Int = 12",
                 stripCrLfs(runCtx.getCurrentOutput))

    pane.setText("13")
    runCode()
    assertEquals(stripCrLfs(Delimiter) +
                 "res0: Int = 12" +
                 stripCrLfs(Delimiter) +
                 "res1: Int = 13",
                 stripCrLfs(runCtx.getCurrentOutput))


    pane.setText("forward(100)")
    runCode()
    assertEquals(stripCrLfs(Delimiter) +
                 "res0: Int = 12" +
                 stripCrLfs(Delimiter) +
                 "res1: Int = 13" +
                 stripCrLfs(Delimiter),
                 stripCrLfs(runCtx.getCurrentOutput))

    pane.setText("14")
    runCode()
    assertEquals(stripCrLfs(Delimiter) +
                 "res0: Int = 12" +
                 stripCrLfs(Delimiter) +
                 "res1: Int = 13" +
                 stripCrLfs(Delimiter) +
                 "res3: Int = 14",
                 stripCrLfs(runCtx.getCurrentOutput))

    runCtx.clearOutput

    pane.setText("while (true) {println(\"42\")}")
    scheduleInterruption()
    runCode()
    Thread.sleep(500)
    println("***********Post Interruption Output: " + runCtx.getCurrentOutput)
    assertTrue(runCtx.getCurrentOutput.contains("Script Stopped."))

    runCtx.clearOutput
    assertEquals("", runCtx.getCurrentOutput)

    pane.setText("while (true) {forward(100)}")
    scheduleInterruption()
    runCode()
    Thread.sleep(500)
    assertTrue(runCtx.getCurrentOutput.contains("Script Stopped."))
  }

  @Test
  def testTwoEvalsAndAnError = {
    pane.setText("12")
    runCode()
    assertTrue(runCtx.getCurrentOutput.contains("12"))

    pane.setText("13")
    runCode()
    assertTrue(runCtx.getCurrentOutput.contains("13"))

    runCtx.clearOutput

    pane.setText("some junk")
    runCode()

    assertTrue(runCtx.getCurrentOutput.contains("error: not found: value some"))
  }

  def stripCrLfs(str: String) = str.replaceAll("\r?\n", "")
}
