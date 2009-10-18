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

import java.util.concurrent.{CountDownLatch, TimeUnit}


class CodePaneTest {

  val fileStr = System.getProperty("nbjunit.workdir") + "../../../../../../../Kojo/build/cluster"
  val file = new java.io.File(fileStr)
  assertTrue(file.exists)
  System.setProperty("netbeans.dirs", fileStr)

  var latch: CountDownLatch = _

  @volatile var outputMarker: String = _
  val DefaultMarker = "\u0000"

  val runCtx = new RunContext {
    val currOutput = new StringBuilder

    def reportRunError() {}

    def reportOutput(lineFragment: String) {
      if (lineFragment.contains(outputMarker)) {
        currOutput.append(lineFragment)
        // signal waiters
        latch.countDown
      } else {
        currOutput.append(lineFragment)
      }
    }

    def getCurrentOutput: String  = currOutput.toString
    def isRunningEnabled: Boolean = true
    def interpreterStarted {}
    def interpreterDone {}
    def clearOutput {currOutput.clear}
  }

  val codeRunner = new xscala.ScalaCodeRunner(runCtx, sprite.SpriteCanvas.instance)
  val pane = new CodePane(codeRunner)
  val Delimiter = pane.OutputDelimiter


  def runCode() {
    codeRunner.runCode(pane.getText())
  }

  def interruptInterpreter() {
    codeRunner.interruptInterpreter()
  }

  // if we have more than five tests, we run out of heap space - maybe a leak in the Scala interpreter/compiler
  // subsystem. So we run (mostly) everything in one test
  @Test
  def testEvalSession = {
    
    pane.setText("12")
    runCode()
    awaitResult("12")
    assertEquals("res1: Int = 12", stripCrLfs(runCtx.getCurrentOutput))
//    assertEquals(1, pane.commandHistory.hIndex)

    pane.setText("13")
    runCode()
    awaitResult("13")

    assertEquals("res1: Int = 12" +
                 stripCrLfs(Delimiter) +
                 "res2: Int = 13",
                 stripCrLfs(runCtx.getCurrentOutput))
//    assertEquals(2, pane.commandHistory.hIndex)

    pane.setText("forward(100)")
    runCode()
    awaitResult(Delimiter)

    assertEquals("res1: Int = 12" +
                 stripCrLfs(Delimiter) +
                 "res2: Int = 13" +
                 stripCrLfs(Delimiter),
                 stripCrLfs(runCtx.getCurrentOutput))
//    assertEquals(3, pane.commandHistory.hIndex)

    pane.setText("14")
    runCode()
    awaitResult("14")

    assertEquals("res1: Int = 12" +
                 stripCrLfs(Delimiter) +
                 "res2: Int = 13" +
                 stripCrLfs(Delimiter) +
                 "res4: Int = 14",
                 stripCrLfs(runCtx.getCurrentOutput))
//    assertEquals(4, pane.commandHistory.hIndex)

    runCtx.clearOutput

    pane.setText("while (true) {println(\"42\")}")
    runCode()
    Thread.sleep(500)
    interruptInterpreter()
    awaitResult("Script Stopped")
    assertTrue(runCtx.getCurrentOutput.endsWith("Script Stopped.\n"))

    runCtx.clearOutput
    assertEquals("", runCtx.getCurrentOutput)

    pane.setText("while (true) {forward(100)}")
    runCode()
    Thread.sleep(500)
    interruptInterpreter()
    awaitResult("Script Stopped")
    assertTrue(runCtx.getCurrentOutput.endsWith("Script Stopped.\n"))
  }

  @Test
  def testTwoEvalsAndAnError = {
    pane.setText("12")
    runCode()
    awaitResult("12")

    pane.setText("13")
    runCode()
    awaitResult("13")

    runCtx.clearOutput

    pane.setText("some junk")
    runCode()
    awaitResult("^")

    assertTrue(runCtx.getCurrentOutput.contains("error: not found: value some"))
  }

  def stripCrLfs(str: String) = str.replaceAll("\r?\n", "")

  def awaitResult(result: String) {
    outputMarker = result
    latch = new CountDownLatch(1)
    val found = latch.await(5, TimeUnit.SECONDS)
    if (!found) throw new RuntimeException("Expected output %s not seen. Current output: %s" format(result, runCtx.getCurrentOutput))
  }
}
