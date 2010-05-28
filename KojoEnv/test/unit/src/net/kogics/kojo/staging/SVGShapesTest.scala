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
class Shapes2Test extends KojoTestBase {

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

  def testPolyLine(r: PNode, s: String) = {
    assertTrue(r.isInstanceOf[net.kogics.kojo.kgeom.PolyLine])
    val pl = r.asInstanceOf[net.kogics.kojo.kgeom.PolyLine]
    val at = new java.awt.geom.AffineTransform
    val pi = pl.polyLinePath.getPathIterator(at)
    var res = new StringBuffer
    val z = getPathString(pi, res).toString
    if (s != z) println(z)
    assertEquals(s, z)
  }

  val CL = java.awt.geom.PathIterator.SEG_CLOSE   // 4
  val CT = java.awt.geom.PathIterator.SEG_CUBICTO // 3
  val LT = java.awt.geom.PathIterator.SEG_LINETO  // 1
  val MT = java.awt.geom.PathIterator.SEG_MOVETO  // 0
  val QT = java.awt.geom.PathIterator.SEG_QUADTO  // 2
//  public static final int 	WIND_EVEN_ODD 	0
//  public static final int 	WIND_NON_ZERO 	1

  def ppathSegToString (t: Int, coords: Array[Double]) = t match {
    case MT =>
      "M" + ("%.4g" format coords(0)) + "," + ("%.4g" format coords(1)) + " "
    case LT =>
      "L" + ("%.4g" format coords(0)) + "," + ("%.4g" format coords(1)) + " "
    case QT =>
      "Q" + ("%.4g" format coords(0)) + "," + ("%.4g" format coords(1)) + " " +
            ("%.4g" format coords(2)) + "," + ("%.4g" format coords(3)) + " "
    case CT =>
      "C" + ("%.4g" format coords(0)) + "," + ("%.4g" format coords(1)) + " " +
            ("%.4g" format coords(2)) + "," + ("%.4g" format coords(3)) + " " +
            ("%.4g" format coords(4)) + "," + ("%.4g" format coords(5)) + " "
    case CL =>
      "z "
  }

  def getPathString(pi: java.awt.geom.PathIterator, res: StringBuffer) = {
    while (!pi.isDone) {
      pi.next
      val coords = Array[Double](0, 0, 0, 0, 0, 0)
      val t = pi.currentSegment(coords)
      res.append(ppathSegToString(t, coords))
    }
    res.toString
  }

  def ppathToString (pp: edu.umd.cs.piccolo.nodes.PPath) = {
    val pr = pp.getPathReference
    val at = new java.awt.geom.AffineTransform
    val pi = pr.getPathIterator(at)
    var res = new StringBuffer
    res.append("m" + ("%.4g" format pp.getX) + "," + ("%.4g" format pp.getY) + " ")
    getPathString(pi, res)
  }

  def testPPath(r: PNode, path: String) = {
    assertTrue(r.isInstanceOf[edu.umd.cs.piccolo.nodes.PPath])
    val pp = r.asInstanceOf[edu.umd.cs.piccolo.nodes.PPath]
    val s = ppathToString(pp)
    if (path != s) println(s)
    assertEquals(path, s)
  }

  def dumpChildString(n: Int) = {
    try {
      val c = SpriteCanvas.instance.figure0.dumpChild(n)
      if (c.isInstanceOf[net.kogics.kojo.kgeom.PArc]) {
        "PArc(" + (c.getX.round + 1) + "," + (c.getY.round + 1) + ")"
      }
      else if (c.isInstanceOf[net.kogics.kojo.kgeom.PPoint]) {
        "PPoint(" + (c.getX.round + 1) + "," + (c.getY.round + 1) + ")"
      }
      else if (c.isInstanceOf[net.kogics.kojo.kgeom.PolyLine]) {
        "PolyLine(" + (c.getX.round + 2) + "," + (c.getY.round + 2) + ")"
      }
      else if (c.isInstanceOf[edu.umd.cs.piccolo.nodes.PPath]) {
        "PPath(" + (c.getX.round + 1) + "," + (c.getY.round + 1) + ")"
      }
      else c.toString
    }
    catch { case e => throw e }
  }

  @Test
  // lalit sez: if we have more than five tests, we run out of heap space - maybe a leak in the Scala interpreter/compiler
  // subsystem. So we run (mostly) everything in one test
  def test = {
    val f = SpriteCanvas.instance.figure0
    f.clear
    var n = 0


    //W
    //W==SVG Shapes==
    //W
    //W{{{
    //WsvgShape(<rect x="15" y="15" width="25" height="5"/>)
    Tester("""import Staging._ ; svgShape(<rect x="15" y="15" width="25" height="5"/>)""")
    testPPath(
      f.dumpChild(n),
      "m14.00,14.00 L40.00,15.00 L40.00,20.00 L15.00,20.00 L15.00,15.00 z M0.000,0.000 "
    )
    n += 1

    //W}}}
    //W
    //W{{{
    //WsvgShape(<circle cx="15" cy="15" r="25"/>)
    Tester("""import Staging._ ; svgShape(<circle cx="15" cy="15" r="25"/>)""")
    testPPath(f.dumpChild(n),
              "m-11.00,-11.00 C40.00,28.81 28.81,40.00 15.00,40.00 " +
              "C1.193,40.00 -10.00,28.81 -10.00,15.00 " +
              "C-10.00,1.193 1.193,-10.00 15.00,-10.00 " +
              "C28.81,-10.00 40.00,1.193 40.00,15.00 z M0.000,0.000 ")
    n += 1

    //W}}}
    //W
    //W{{{
    //WsvgShape(<ellipse cx="15" cy="15" rx="35" ry="25"/>)
    Tester("""import Staging._ ; svgShape(<ellipse cx="15" cy="15" rx="35" ry="25"/>)""")
    testPPath(f.dumpChild(n), "m-21.00,-11.00 C50.00,28.81 34.33,40.00 15.00,40.00 " +
              "C-4.330,40.00 -20.00,28.81 -20.00,15.00 " +
              "C-20.00,1.193 -4.330,-10.00 15.00,-10.00 " +
              "C34.33,-10.00 50.00,1.193 50.00,15.00 z M0.000,0.000 ")
    n += 1

    //W}}}
    //W
    //W{{{
    //WsvgShape(<line x1="15" y1="15" x2="40" y2="20"/>)
    Tester("""import Staging._ ; svgShape(<line x1="15" y1="15" x2="40" y2="20"/>)""")
    testPolyLine(f.dumpChild(n), "L40.00,20.00 M0.000,0.000 ")
    n += 1

    //W}}}
    //W
    //W{{{
    //WsvgShape(<polyline points="15,15 25,35 40,20 45,25 50,10"/>)
    Tester("""import Staging._ ; svgShape(<polyline points="15,15 25,35 40,20 45,25 50,10"/>)""")
    testPolyLine(f.dumpChild(n), "L25.00,35.00 L40.00,20.00 L45.00,25.00 L50.00,10.00 M0.000,0.000 ")
    n += 1
    //W}}}
    //W
    //W{{{
    //WsvgShape(<polygon points="15,15 25,35 40,20 45,25 50,10"/>)
    Tester("""import Staging._ ; svgShape(<polygon points="15,15 25,35 40,20 45,25 50,10"/>)""")
    testPolyLine(f.dumpChild(n), "L25.00,35.00 L40.00,20.00 L45.00,25.00 L50.00,10.00 z M0.000,0.000 ")
    n += 1
    //W}}}
    //W
    //W{{{
    //WsvgShape(<path d="M15,15 40,15 40,20 15,20 z"/>)
    Tester("""import Staging._ ; svgShape(<path d="M15,15 40,15 40,20 15,20 z"/>)""")
//    println(ppathToString(f.dumpChild(n).asInstanceOf[edu.umd.cs.piccolo.nodes.PPath]))
    testPPath(f.dumpChild(n), "m14.00,14.00 L40.00,15.00 L40.00,20.00 L15.00,20.00 z M0.000,0.000 ")
    n += 1
    //W}}}
    //W

/* TODO restore these tests
    //M
    //M===Syntax===
    //M
    //M{{{
    //MsvgShape(<g>... svg elements ...</g>)
    Tester("""import Staging._
             |svgShape(<g>
             |           <rect x="15" y="15" width="25" height="5"/>
             |           <circle cx="15" cy="15" r="25"/>
             |</g>)""".stripMargin)
    n += 1
    testPPath(f.dumpChild(n),
              "m-11.00,-11.00 C40.00,28.8071 28.8071,40.00 15.00,40.00 " +
              "C1.19288,40.00 -10.00,28.8071 -10.00,15.00 " +
              "C-10.00,1.19288 1.19288,-10.00 15.00,-10.00 " +
              "C28.8071,-10.00 40.00,1.19288 40.00,15.00 " +
              "z M0.00,0.00 ")
    n += 1
    //M}}}
    //M
    //Mdraws and returns multiple shapes.

    //M
    //M===Syntax===
    //M
    //M{{{
    //MsvgShape(<svg>... svg elements ...</svg>)
    Tester("""import Staging._
             |svgShape(<svg>
             |           <rect x="45" y="45" width="25" height="5"/>
             |           <g>
             |             <rect x="15" y="15" width="25" height="5"/>
             |             <circle cx="15" cy="15" r="25"/>
             |           </g>
             |</svg>)""".stripMargin)
    n += 2
    testPPath(f.dumpChild(n),
              "m-11.00,-11.00 C40.00,28.81 28.81,40.00 15.00,40.00 " +
              "C1.19,40.00 -10.00,28.81 -10.00,15.00 " +
              "C-10.00,1.19 1.19,-10.00 15.00,-10.00 " +
              "C28.81,-10.00 40.00,1.19 40.00,15.00 " +
              "z M0.00,0.00 ")
    n += 1
    //M}}}
    //M
    //Mdraws and returns multiple shapes.
*/

//    println(ppathToString(f.dumpChild(n).asInstanceOf[edu.umd.cs.piccolo.nodes.PPath]))
  }
  def stripCrLfs(str: String) = str.replaceAll("\r?\n", "")
}

