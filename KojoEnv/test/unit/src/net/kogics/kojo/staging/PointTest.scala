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

//import org.junit.After
//import org.junit.Before
import org.junit.Test
import org.junit.Assert._

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{CountDownLatch, TimeUnit}

import net.kogics.kojo.core.RunContext

import net.kogics.kojo.util._

// cargo coding off CodePaneTest
class PointTest extends KojoTestBase {

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

  object Tester {
    var resCounter = 0
    var res = ""

    def apply (cmd: String, s: String, f: Boolean = true) = {
      res += stripCrLfs(Delimiter)
      if (f) {
        res += "res" + resCounter + ": "
        resCounter += 1
      }
      res += s
      pane.setText(cmd)
      runCtx.success.set(false)
      runCode()
      Utils.runInSwingThreadAndWait {  /* noop */  }
      assertTrue(runCtx.success.get)
      assertTrue(stripCrLfs(runCtx.getCurrentOutput) matches res)
    }
  }

  @Test
  // lalit sez: if we have more than five tests, we run out of heap space - maybe
  // a leak in the Scala interpreter/compiler subsystem. So we run (mostly)
  // everything in one test
  def test1 = {
  //W
  //W==Points==
  //W
  //WA point value can be created by calling the method
  //W
  //W{{{
  //Wpoint(xval, yval)
  //W}}}
  //W
    Tester("Staging.point(-22, -13)", "net.kogics.kojo.core.Point = Point\\(-22[,.]00, -13[,.]00\\)")
  //W
  //WThe constant `O` (capital o) has the same value of `point(0, 0)`.
    Tester("Staging.O", "net.kogics.kojo.core.Point = Point\\(0[,.]00, 0[,.]00\\)")
  //W
  //WThe methods `Mid` and `Ext` have the coordinates of the middle point of the
  //Wuser screen and the coordinates of the upper right corner of the user
  //Wscreen (the extreme point), respectively.  Both return value `point(0, 0)`
  //Wif `screenSize` hasn't been called yet.
    Tester("Staging.Mid", "net.kogics.kojo.core.Point = Point\\(0[,.]00, 0[,.]00\\)")
    Tester("Staging.Ext", "net.kogics.kojo.core.Point = Point\\(0[,.]00, 0[,.]00\\)")
    Tester("Staging.screenSize(10,10)", "\\(Int, Int\\) = \\(10,10\\)")
    Tester("Staging.Mid", "net.kogics.kojo.core.Point = Point\\(5[,.]00, 5[,.]00\\)")
    Tester("Staging.Ext", "net.kogics.kojo.core.Point = Point\\(10[,.]00, 10[,.]00\\)")
  //W
  //WPoint values can be added, subtracted, or negated
  //W
  //W{{{
  //Wpoint(10, 20) + point(25, 0)
    Tester(
      "Staging.point(10, 20) + Staging.point(25, 0)",
      "net.kogics.kojo.core.Point = Point\\(35[,.]00, 20[,.]00\\)"
    )
  //W}}}
  //W
  //Wis the same as `point(35, 20)`
  //W
  //W{{{
  //Wpoint(35, 20) - point(25, 0)
    Tester(
      "Staging.point(35, 20) - Staging.point(25, 0)",
      "net.kogics.kojo.core.Point = Point\\(10[,.]00, 20[,.]00\\)"
    )
  //W}}}
  //W
  //Wis the same as `point(10, 20)`
  //W
  //W{{{
  //W-point(10, -20)
    Tester(
      "-Staging.point(10, -20)",
      "net.kogics.kojo.core.Point = Point\\(-10[,.]00, 20[,.]00\\)"
    )
  //W}}}
  //W
  //Wis the same as `point(-10, 20)`
  //W
  //WTuples of {{{Double}}}s or {{{Int}}}s are implicitly converted to
  //W{{{Point}}}s where applicable, if `Staging` has been imported.
    Tester("""import Staging._
             |var a = Staging.point(0,0)
             |a = (-22, -13)""".stripMargin,
      "import Staging._" +
      "a: net.kogics.kojo.core.Point = Point\\(0[,.]00, 0[,.]00\\)" +
      "a: net.kogics.kojo.core.Point = Point\\(-22[,.]00, -13[,.]00\\)",
      false
    )
    Tester("""import Staging._
             |var b = Staging.point(0,0)
             |b = (5., .45)""".stripMargin,
      "import Staging._" +
      "b: net.kogics.kojo.core.Point = Point\\(0[,.]00, 0[,.]00\\)" +
      "b: net.kogics.kojo.core.Point = Point\\(5[,.]00, 0[,.]45\\)",
      false
    )
  //W
  }

  def stripCrLfs(str: String) = str.replaceAll("\r?\n", "")
}

