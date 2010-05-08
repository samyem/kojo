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

import net.kogics.kojo.util._

// cargo coding off CodePaneTest
class ShapesTest extends KojoTestBase {

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
    val canvas = SpriteCanvas.instance
    (
        canvas.getCamera.getViewTransformReference.getScaleX,
        canvas.getCamera.getViewTransformReference.getScaleY,
        canvas.getCamera.getViewTransformReference.getTranslateX,
        canvas.getCamera.getViewTransformReference.getTranslateY
      )
  }

  object Tester {
    var resCounter = 0
    var res = ""

    def apply (cmd: String, res: String = "") = {
      //res += stripCrLfs(Delimiter) + "res" + resCounter + ": " + s
      resCounter += 1
      pane.setText(cmd)
      runCtx.success.set(false)
      runCode()
      Utils.runInSwingThreadAndWait {  /* noop */  }
      assertTrue(runCtx.success.get)
      val s = SpriteCanvas.instance.figure0.dumpLastOfCurrLayer
      if (res != "") {
        assertEquals(res, s)
      }
      else {
        println(s)
      }

      // TODO means of assertion
    }
  }

  def testPolyLine(r: AnyRef, size: Int) = {
    assertTrue(r.isInstanceOf[net.kogics.kojo.kgeom.PolyLine])
    val pl = r.asInstanceOf[net.kogics.kojo.kgeom.PolyLine]
    assertEquals(size, pl.size)
  }

  @Test
  // lalit sez: if we have more than five tests, we run out of heap space - maybe a leak in the Scala interpreter/compiler
  // subsystem. So we run (mostly) everything in one test
  def testEvalSession = {
    val f = SpriteCanvas.instance.figure0
    var n = f.dumpNumOfChildren
    assertEquals(n, 0)

    Tester("Staging.dot(Staging.Point(15, 10))", "PPoint(15,10)")
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    Tester("import Staging._ ; line((15, 15), (40, 20))")
    assertEquals("PolyLine(15,15)", f.dumpChildString(n))
    testPolyLine(f.dumpChild(n), 2)
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    Tester("import Staging._ ; line((15, 15), (40, 20)).toRectangle")
    n += 1 // this command creates two objects, a line and a rectangle

    assertEquals("PPath(15,15)", f.dumpChildString(n))
    val r1 = f.dumpChild(n)
    //assertTrue(r1.isInstanceOf[edu.umd.cs.piccolo.nodes.PPath])
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    Tester("import Staging._ ; rectangle((15, 15), (40, 20))", "PPath(15,15)")
    val r2 = f.dumpChild(n)
    //assertTrue(r2.isInstanceOf[edu.umd.cs.piccolo.nodes.PPath])
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    Tester("import Staging._ ; square((15, 15), 20)", "PPath(15,15)")
    val r3 = f.dumpChild(n)
    //assertTrue(r3.isInstanceOf[edu.umd.cs.piccolo.nodes.PPath])
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    Tester("import Staging._ ; roundRectangle((15, 15), (40, 20), (3, 5))", "PPath(15,15)")
    val r4 = f.dumpChild(n)
    //assertTrue(r4.isInstanceOf[edu.umd.cs.piccolo.nodes.PPath])
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    Tester("import Staging._ ; triangle((15, 15), (25, 35), (35, 15))", "PolyLine(15,15)")
    testPolyLine(f.dumpChild(n), 3)
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    Tester("import Staging._ ; quad((15, 15), (25, 35), (40, 20), (35, 10))", "PolyLine(15,10)")
    testPolyLine(f.dumpChild(n), 4)
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    Tester("import Staging._ ; polyline(List((15, 15), (25, 35), (40, 20), (45, 25), (50, 10)))", "PolyLine(15,10)")
    testPolyLine(f.dumpChild(n), 5)
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    Tester("import Staging._ ; polygon(List((15, 15), (25, 35), (40, 20), (45, 25), (50, 10)))", "PolyLine(15,10)")
    testPolyLine(f.dumpChild(n), 5)
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    Tester("import Staging._ ; arc((15, 15), (20, 10), 40, 95)")
    // , "PArc(15,10)"
    val r9 = f.dumpChild(n)
    assertTrue(r9.isInstanceOf[net.kogics.kojo.kgeom.PArc])
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    Tester("import Staging._ ; ellipse((15, 15), (35, 25))")
    // , "PPath(15,15)")
    val r10 = f.dumpChild(n)
    assertTrue(r10.isInstanceOf[edu.umd.cs.piccolo.nodes.PPath])
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    Tester("import Staging._ ; circle((15, 15), 25)")
    assertEquals("PPath(-35,-35)", f.dumpChildString(n))
    val r11 = f.dumpChild(n)
    assertTrue(r11.isInstanceOf[edu.umd.cs.piccolo.nodes.PPath])
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    val points = """List((10, 20), (10, 50),
       |(20, 50), (20, 20),
       |(30, 20), (30, 50),
       |(40, 50), (40, 20),
       |(50, 20), (50, 50),
       |(60, 50), (60, 20))""".stripMargin
    Tester("import Staging._ ; linesShape(" + points + ")")
    assertEquals("PolyLine(10,20)", f.dumpChildString(n))
    n += 5 // 6 lines total are created, we'll look at the last one
    testPolyLine(f.dumpChild(n), 2)
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    Tester("import Staging._ ; trianglesShape(" + points + ")")
    assertEquals("PolyLine(10,20)", f.dumpChildString(n))
    n += 3 // 4 triangles total are created, we'll look at the last one
    testPolyLine(f.dumpChild(n), 3)
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    val tssPoints = """List((10, 20), (10, 50),
       |(20, 20), (20, 50),
       |(30, 20), (30, 50),
       |(40, 20), (40, 50),
       |(50, 20), (50, 50),
       |(60, 20), (60, 50))""".stripMargin
    Tester("import Staging._ ; triangleStripShape(" + tssPoints + ")")
    assertEquals("PolyLine(10,20)", f.dumpChildString(n))
    n += 9 // 10 triangles total are created, we'll look at the last one
    testPolyLine(f.dumpChild(n), 3)
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    Tester("import Staging._ ; quadsShape(" + points + ")")
    assertEquals("PolyLine(10,20)", f.dumpChildString(n))
    n += 2 // 3 quads total are created, we'll look at the last one
    testPolyLine(f.dumpChild(n), 4)
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    Tester("import Staging._ ; quadStripShape(" + points + ")")
    assertEquals("PolyLine(10,20)", f.dumpChildString(n))
    n += 4 // 5 quads total are created, we'll look at the last one
    testPolyLine(f.dumpChild(n), 4)
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    val tfsPoints = """List(
       |(30, 45), (40, 40),
       |(40, 40), (45, 30),
       |(45, 30), (40, 20),
       |(40, 20), (30, 15),
       |(30, 15), (20, 20),
       |(20, 20), (15, 30),
       |(15, 30), (20, 40))""".stripMargin
    Tester("import Staging._ ; triangleFanShape((30, 30), " + tfsPoints + ")")
    assertEquals("PolyLine(30,30)", f.dumpChildString(n))
    n += 6 // 7 triangles total are created, we'll look at the last one
    testPolyLine(f.dumpChild(n), 3)
    n += 1
    assertEquals(n, f.dumpNumOfChildren)
  }

  def stripCrLfs(str: String) = str.replaceAll("\r?\n", "")
}

