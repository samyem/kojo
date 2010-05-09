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

    def apply (cmd: String) = {
      //res += stripCrLfs(Delimiter) + "res" + resCounter + ": " + s
      resCounter += 1
      pane.setText(cmd)
      runCtx.success.set(false)
      runCode()
      Utils.runInSwingThreadAndWait {  /* noop */  }
      assertTrue(runCtx.success.get)
    }
  }

  type PNode = edu.umd.cs.piccolo.PNode

  def testPolyLine(r: PNode, size: Int) = {
    assertTrue(r.isInstanceOf[net.kogics.kojo.kgeom.PolyLine])
    val pl = r.asInstanceOf[net.kogics.kojo.kgeom.PolyLine]
    assertEquals(size, pl.size)
  }

  def testPolyLine(r: PNode, x: Int, y: Int, size: Int) = {
    assertTrue(r.isInstanceOf[net.kogics.kojo.kgeom.PolyLine])
    val pl = r.asInstanceOf[net.kogics.kojo.kgeom.PolyLine]
    assertEquals(x, pl.getX.round.toInt)
    assertEquals(y, pl.getY.round.toInt)
    assertEquals(size, pl.size)
  }

  val CL = java.awt.geom.PathIterator.SEG_CLOSE   // 4
  val CT = java.awt.geom.PathIterator.SEG_CUBICTO // 3
  val LT = java.awt.geom.PathIterator.SEG_LINETO  // 1
  val MT = java.awt.geom.PathIterator.SEG_MOVETO  // 0
  val QT = java.awt.geom.PathIterator.SEG_QUADTO  // 2
//  public static final int 	WIND_EVEN_ODD 	0
//  public static final int 	WIND_NON_ZERO 	1

  def ppathToString (pp: edu.umd.cs.piccolo.nodes.PPath) {
    val pr = pp.getPathReference
    val at = new java.awt.geom.AffineTransform
    val pi = pr.getPathIterator(at)
    var res = "m" + ("%g" format pp.getX) + "," + ("%g" format pp.getY) + " "
    while (!pi.isDone) {
      pi.next
      val coords = Array[Double](0, 0, 0, 0, 0, 0)
      val t = pi.currentSegment(coords)
      t match {
        case MT =>
          res += "M"
          res += ("%g" format coords(0)) + ","
          res += ("%g" format coords(1)) + " "
        case LT =>
          res += "L"
          res += ("%g" format coords(0)) + ","
          res += ("%g" format coords(1)) + " "
        case QT =>
          res += "Q"
          res += ("%g" format coords(0)) + ","
          res += ("%g" format coords(1)) + " "
          res += ("%g" format coords(2)) + ","
          res += ("%g" format coords(3)) + " "
        case CT =>
          res += "C"
          res += ("%g" format coords(0)) + ","
          res += ("%g" format coords(1)) + " "
          res += ("%g" format coords(2)) + ","
          res += ("%g" format coords(3)) + " "
          res += ("%g" format coords(4)) + ","
          res += ("%g" format coords(5)) + " "
        case CL =>
          res += "z"
      }
    }
    res
  }

  def testPPath(r: PNode, path: String) = {
    assertTrue(r.isInstanceOf[edu.umd.cs.piccolo.nodes.PPath])
    val pp = r.asInstanceOf[edu.umd.cs.piccolo.nodes.PPath]
    assertEquals(path, ppathToString(pp))
  }

  @Test
  // lalit sez: if we have more than five tests, we run out of heap space - maybe a leak in the Scala interpreter/compiler
  // subsystem. So we run (mostly) everything in one test
  def testEvalSession = {
    val f = SpriteCanvas.instance.figure0
    var n = f.dumpNumOfChildren
    assertEquals(n, 0)

    Tester("Staging.dot(Staging.Point(15, 10))")
    assertEquals("PPoint(15,10)", f.dumpChildString(n))
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    Tester("import Staging._ ; line((15, 15), (40, 20))")
    testPolyLine(f.dumpChild(n), 13, 13, 2)
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    Tester("import Staging._ ; line((15, 15), (40, 20)).toRectangle")
    testPolyLine(f.dumpChild(n), 13, 13, 2)
    n += 1
    testPPath(f.dumpChild(n), "")
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    Tester("import Staging._ ; rectangle((15, 15), (40, 20))")
    testPPath(f.dumpChild(n), "")
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    Tester("import Staging._ ; square((15, 15), 20)")
    testPPath(f.dumpChild(n), "")
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    Tester("import Staging._ ; roundRectangle((15, 15), (40, 20), (3, 5))")
    testPPath(f.dumpChild(n), "")
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    Tester("import Staging._ ; triangle((15, 15), (25, 35), (35, 15))")
    assertEquals("PolyLine(15,15)", f.dumpChildString(n))
    testPolyLine(f.dumpChild(n), 3)
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    Tester("import Staging._ ; quad((15, 15), (25, 35), (40, 20), (35, 10))")
    assertEquals("PolyLine(15,10)", f.dumpChildString(n))
    testPolyLine(f.dumpChild(n), 4)
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    Tester("import Staging._ ; polyline(List((15, 15), (25, 35), (40, 20), (45, 25), (50, 10)))")
    assertEquals("PolyLine(15,10)", f.dumpChildString(n))
    testPolyLine(f.dumpChild(n), 5)
    n += 1
    assertEquals(n, f.dumpNumOfChildren)

    Tester("import Staging._ ; polygon(List((15, 15), (25, 35), (40, 20), (45, 25), (50, 10)))")
    assertEquals("PolyLine(15,10)", f.dumpChildString(n))
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

