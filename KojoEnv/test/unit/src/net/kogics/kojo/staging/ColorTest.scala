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
class ColorTest extends KojoTestBase {

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
      assertEquals(res, stripCrLfs(runCtx.getCurrentOutput))
    }
  }

  @Test
  // lalit sez: if we have more than five tests, we run out of heap space - maybe
  // a leak in the Scala interpreter/compiler subsystem. So we run (mostly)
  // everything in one test
  def test1 = {
  //W
  //W==Color==
  //W
  //WA color value can be created by calling the method
  //W
  //W{{{
  //Wcolor( ... )
    Tester(
      "import Staging._ ; println(color(22, 13, 75))",
      "java.awt.Color[r=22,g=13,b=75]import Staging._",
      false
    )

  //W}}}
  //W
  //Wwhich yields an instance of `java.awt.Color`.  The method accepts different
  //Wsets of arguments depending on color mode.
  //W
  //W{{{
  //WcolorMode(GRAY(lim))
  //Wcolor(arg)
    Tester(
      "import Staging._ ; colorMode(GRAY(255)) ; println(color(22))",
      "java.awt.Color[r=22,g=22,b=22]import Staging._",
      false
    )
    Tester(
      "import Staging._ ; colorMode(GRAY(255)) ; println(color(.1))",
      "java.awt.Color[r=26,g=26,b=26]import Staging._",
      false
    )

  //W}}}
  //W
  //Wcreates a grayscale color with a whiteness value of `arg`.  If `arg` is an
  //W`Double` value, it is expected to be within the range 0.0 (black) -- 1.0
  //W(white).  If `arg` is an integer value, it is as if the method were called
  //Wwith
  //W
  //W{{{
  //Wcolor(norm(arg, 0, lim))
    Tester(
      "import Staging._ ; colorMode(GRAY(255)) ; println(color(norm(22, 0, 255)))",
      "java.awt.Color[r=22,g=22,b=22]import Staging._",
      false
    )

  //W}}}
  //W
  //W{{{
  //WcolorMode(RGB(lim1, lim2, lim3))
  //Wcolor(intVal)
    Tester(
      "import Staging._ ; colorMode(RGB(255, 255, 255)) ; println(color(22))",
      "java.awt.Color[r=0,g=0,b=22]import Staging._",
      false
    )

  //W}}}
  //W
  //Won the other hand creates a color with this absolute code.
  //W
  //W{{{
  //WcolorMode(GRAYA(lim1, lim2)) // GRAY + ALPHA
  //Wcolor(arg1, arg2)
    Tester(
      "import Staging._ ; colorMode(GRAYA(255, 255)) ; println(color(22, 22))",
      "java.awt.Color[r=22,g=22,b=22]import Staging._",
      false
    )
    Tester(
      "import Staging._ ; colorMode(GRAYA(255, 255)) ; println(color(.1, .1))",
      "java.awt.Color[r=26,g=26,b=26]import Staging._",
      false
    )

  //W}}}
  //W
  //Wcreates a grayscale color with a whiteness value of `arg1` and an opaqueness
  //Wof `arg2`.
  //W
  //W{{{
  //WcolorMode(RGB(lim1, lim2, lim3)) // RED/GREEN/BLUE
  //Wcolor(arg1, arg2, arg3)
    Tester(
      "import Staging._ ; colorMode(RGB(255, 255, 255)) ; println(color(22, 22, 22))",
      "java.awt.Color[r=22,g=22,b=22]import Staging._",
      false
    )
    Tester(
      "import Staging._ ; colorMode(RGB(255, 255, 255)) ; println(color(.1, .1, .1))",
      "java.awt.Color[r=26,g=26,b=26]import Staging._",
      false
    )

  //W}}}
  //W
  //Wcreates a color with red, green, blue components `arg1`, `arg2`, `arg3`.
  //W
  //W{{{
  //WcolorMode(RGBA(lim1, lim2, lim3, lim4)) // RED/GREEN/BLUE + ALPHA
  //Wcolor(arg1, arg2, arg3, arg4)
    Tester(
      "import Staging._ ; colorMode(RGBA(255, 255, 255, 255)) ; println(color(22, 22, 22, 22))",
      "java.awt.Color[r=22,g=22,b=22]import Staging._",
      false
    )
    Tester(
      "import Staging._ ; colorMode(RGBA(255, 255, 255, 255)) ; println(color(.1, .1, .1, .1))",
      "java.awt.Color[r=26,g=26,b=26]import Staging._",
      false
    )

  //W}}}
  //W
  //Wcreates a color with red, green, blue components `arg1`, `arg2`, `arg3` and
  //Walpha value `arg4`.
  //W
  //W{{{
  //WcolorMode(HSB(lim1, lim2, lim3)) // HUE/SATURATION/BRIGHTNESS
  //Wcolor(arg1, arg2, arg3)
    Tester(
      "import Staging._ ; colorMode(HSB(255, 255, 255)) ; println(color(22, 22, 22))",
      "java.awt.Color[r=22,g=21,b=20]import Staging._",
      false
    )
    Tester(
      "import Staging._ ; colorMode(HSB(255, 255, 255)) ; println(color(.1, .1, .1))",
      "java.awt.Color[r=26,g=24,b=23]import Staging._",
      false
    )

  //W}}}
  //W
  //Wcreates a color with hue, saturation, brightness components `arg1`, `arg2`,
  //W`arg3`.
  //W
  //W{{{
  //WcolorMode(HSBA(lim1, lim2, lim3, lim4)) // HUE/SATURATION/BRIGHTNESS + ALPHA
  //Wcolor(arg1, arg2, arg3, arg4)
    Tester(
      "import Staging._ ; colorMode(HSBA(255, 255, 255, 255)) ; println(color(22, 22, 22, 22))",
      "java.awt.Color[r=23,g=117,b=20]import Staging._",
      false
    )
    Tester(
      "import Staging._ ; colorMode(HSBA(255, 255, 255, 255)) ; println(color(.1, .1, .1, .1))",
      "java.awt.Color[r=27,g=152,b=23]import Staging._",
      false
    )

  //W}}}
  //W
  //Wcreates a color with hue, saturation, brightness components `arg1`, `arg2`,
  //W`arg3` and alpha value `arg4`.
  //W
  //WFinally,
  //W
  //W{{{
  //Wcolor(s)
    Tester(
      """import Staging._ ; println(color("#99ccDD"))""",
      "java.awt.Color[r=153,g=204,b=221]import Staging._",
      false
    )

  //W}}}
  //W
  //Wwhere `s` is a string with the format "#rrggbb" creates a color described
  //Wby that (hexadecimal) value regardless of color mode.
  //W
  //WWhen drawing figures, the _fill_ color, which is used for the insides, and
  //Wthe _stroke_ color, which is used for the edges, can be set and unset.
  //W
  //WTo set the fill color, call `fill`.
  //W
  //W{{{
  //Wfill(color)
    Tester(
      """import Staging._ ; fill(color("#99ccDD"))""",
      "import Staging._",
      false
    )
    assertEquals("java.awt.Color[r=153,g=204,b=221]", SpriteCanvas.instance.figure0.fillColor.toString)

  //W}}}
  //W
  //WTo unset the fill color, call `noFill`, or `fill` with a `null` argument.
  //W
  //W{{{
  //WnoFill
  //Wfill(null)
    Tester("import Staging._ ; fill(null)", "import Staging._", false)
    assertEquals(null, SpriteCanvas.instance.figure0.fillColor)

    Tester(
      """import Staging._ ; fill(color("#99ccDD")) ; noFill""",
      "import Staging._",
      false
    )
    assertEquals(null, SpriteCanvas.instance.figure0.fillColor)

  //W}}}
  //W
  //WTo set the stroke color, call `stroke`.
  //W
  //W{{{
  //Wstroke(color)
    Tester(
      """import Staging._ ; stroke(color("#99ccDD"))""",
      "import Staging._",
      false
    )
    assertEquals("java.awt.Color[r=153,g=204,b=221]", SpriteCanvas.instance.figure0.lineColor.toString)

  //W}}}
  //W
  //WTo unset the stroke color, call `noStroke`, or `stroke` with a `null` argument.
  //W
  //W{{{
  //WnoStroke
  //Wstroke(null)
    Tester("import Staging._ ; stroke(null)", "import Staging._", false)
    assertEquals(null, SpriteCanvas.instance.figure0.lineColor)

    Tester(
      """import Staging._ ; stroke(color("#99ccDD")) ; noStroke""",
      "import Staging._",
      false
    )
    assertEquals(null, SpriteCanvas.instance.figure0.lineColor)

  //W}}}
  //W
  //TODO test strokeWidth, withStyle
  }

  def stripCrLfs(str: String) = str.replaceAll("\r?\n", "")
}

