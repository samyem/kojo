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
package staging

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{CountDownLatch, TimeUnit}

import net.kogics.kojo.core.RunContext

import net.kogics.kojo.util._

// cargo coding off CodePaneTest
class ScreenMethodsTest extends KojoTestBase {

  val fileStr = System.getProperty("nbjunit.workdir") + "../../../../../../../Kojo/build/cluster"
  val file = new java.io.File(fileStr)
  assertTrue(file.exists)
  System.setProperty("netbeans.dirs", fileStr)

  val runCtx = new RunContext {
    val currOutput = new StringBuilder()
    val success = new AtomicBoolean()
    val error = new AtomicBoolean()

    def inspect(obj: AnyRef) {}
    def onInterpreterInit() {}
    def showScriptInOutput() {}
    def hideScriptInOutput() {}
    def showVerboseOutput() {}
    def hideVerboseOutput() {}
    def reportRunError() {}
    def readInput(prompt: String) = ""

    def println(outText: String) = reportOutput(outText)
    def reportOutput(lineFragment: String) {
      currOutput.append(lineFragment)
    }

    def onInterpreterStart(code: String) {}
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

  val codeRunner = new xscala.ScalaCodeRunner(runCtx, SpriteCanvas.instance, geogebra.GeoGebraCanvas.instance.geomCanvas)
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

  def peekZoom = {
    val a = SpriteCanvas.instance.getCamera.getViewTransformReference
    (
      "%.4g" format a.getScaleX,
      "%.4g" format a.getScaleY,
      "%.4g" format a.getTranslateX,
      "%.4g" format a.getTranslateY
    )
  }

  object Tester {
    var resCounter = 0
    var res = ""

    def apply (cmd: String, s: String) = {
      res += stripCrLfs(Delimiter) + "res" + resCounter + ": " + s
      resCounter += 1
      pane.setText(cmd)
      runCtx.success.set(false)
      runCode()
      Utils.runInSwingThreadAndWait {  /* noop */  }
      assertTrue(runCtx.success.get)
      assertEquals(res, stripCrLfs(runCtx.getCurrentOutput))
    }
  }

  @Test
  // lalit sez: if we have more than five tests, we run out of heap space - maybe a leak in the Scala interpreter/compiler
  // subsystem. So we run (mostly) everything in one test
  def testPreEval = {
    assertEquals(("1.000","-1.000","0.000","0.000"), peekZoom)
    Utils.runInSwingThreadAndWait {  /* noop */  }
  }

  @Test
  def testEvalSession = {
  //W
  //W==User Screen==
  //W
  //WThe current width and height of the user screen is stored in the variables
  //W`screenWidth` and `screenHeight` (both are 0 by default).
  //W
    Tester("Staging.screenWidth",
           "Int = 0")
    Tester("Staging.screenHeight",
           "Int = 0")
  //WThe dimensions of the user screen can be set by calling
  //W
  //W{{{
  //WscreenSize(width, height)
  //W}}}
    Tester("Staging.screenSize(250, 150)",
           "(Int, Int) = (250,150)")
    assertEquals(("3.000","-3.000","-375.0","225.0"), peekZoom)
    Tester("Staging.screenWidth",
           "Int = 250")
    Tester("Staging.screenHeight",
           "Int = 150")
  //WThe orientation of either axis can be reversed by negation, e.g.:
  //W
  //W{{{
  //WscreenSize(width, -height)
  //W}}}
  //W
  //Wmakes (0,0) the upper left corner and (width, height) the lower right
  //Wcorner.
    Tester("Staging.screenSize(250, -150)",
           "(Int, Int) = (250,150)")
    assertEquals(("3.000","3.000","-375.0","-225.0"), peekZoom)
    Tester("Staging.screenSize(-250, 150)",
           "(Int, Int) = (250,150)")
    assertEquals(("-3.000","-3.000","375.0","225.0"), peekZoom)
    Tester("Staging.screenSize(-250, -150)",
           "(Int, Int) = (250,150)")
    assertEquals(("-3.000","3.000","375.0","-225.0"), peekZoom)
  }

  def stripCrLfs(str: String) = str.replaceAll("\r?\n", "")
}

