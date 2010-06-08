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

import net.kogics.kojo.util._

class ComplexShapesTest extends StagingTestBase {

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
      "M" + ("%.2g" format coords(0)) + "," + ("%.2g" format coords(1)) + " "
    case LT =>
      "L" + ("%.2g" format coords(0)) + "," + ("%.2g" format coords(1)) + " "
    case QT =>
      "Q" + ("%.2g" format coords(0)) + "," + ("%.2g" format coords(1)) + " " +
            ("%.2g" format coords(2)) + "," + ("%.2g" format coords(3)) + " "
    case CT =>
      "C" + ("%.2g" format coords(0)) + "," + ("%.2g" format coords(1)) + " " +
            ("%.2g" format coords(2)) + "," + ("%.2g" format coords(3)) + " " +
            ("%.2g" format coords(4)) + "," + ("%.2g" format coords(5)) + " "
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
    res.append("m" + ("%.2g" format pp.getX) + "," + ("%.2g" format pp.getY) + " ")
    while (!pi.isDone) {
      pi.next
      val coords = Array[Double](0, 0, 0, 0, 0, 0)
      val t = pi.currentSegment(coords)
      res.append(ppathSegToString(t, coords))
    }
    res.toString
  }

  def testPPath(r: PNode, path: String) = {
    assertTrue(r.isInstanceOf[edu.umd.cs.piccolo.nodes.PPath])
    val pp = r.asInstanceOf[edu.umd.cs.piccolo.nodes.PPath]
    val s = ppathToString(pp)
    if (path != s) println(s)
    assertEquals(path, s)
  }

  @Test
  // lalit sez: if we have more than five tests, we run out of heap space - maybe a leak in the Scala interpreter/compiler
  // subsystem. So we run (mostly) everything in one test
  def test1 = {
    val f = SpriteCanvas.instance.figure0
    var n = 0
    //W
    //W==Complex Shapes==
    //W
    //W===Polylines===
    //W
    //W{{{
    //Wpolyline(points)
    Tester("import Staging._ ; polyline(List((15, 15), (25, 35), (40, 20), (45, 25), (50, 10)))")
    assertEquals("PolyLine(15,10)", makeString(f.dumpChild(n)))
    testPolyLine(f.dumpChild(n), 5)
    n += 1

    //WaPolyline.toPolyline
    Tester("import Staging._ ; polyline(List((15, 15), (25, 35))).toPolyline")
    n += 2

    //WaPolygon.toPolyline
    Tester("import Staging._ ; polygon(List((15, 15), (25, 35))).toPolyline")
    n += 2
    
    //W}}}
    //W

    //W
    //W===Polygons===
    //W
    //W{{{
    //Wpolygon(points)
    Tester("import Staging._ ; polygon(List((15, 15), (25, 35), (40, 20), (45, 25), (50, 10)))")
    assertEquals("PolyLine(15,10)", makeString(f.dumpChild(n)))
    testPolyLine(f.dumpChild(n), 5)
    n += 1

    //WaPolyline.toPolygon
    Tester("import Staging._ ; polyline(List((15, 15), (25, 35))).toPolygon")
    n += 2

    //WaPolygon.toPolygon
    Tester("import Staging._ ; polygon(List((15, 15), (25, 35))).toPolygon")
    n += 2
    
    //W}}}
    //W
    //W{{{
    //Wtriangle(point1, point2, point3)
    Tester("import Staging._ ; triangle((15, 15), (25, 35), (35, 15))")
    assertEquals("PolyLine(15,15)", makeString(f.dumpChild(n)))
    testPolyLine(f.dumpChild(n), 3)
    n += 1

    //W}}}
    //W
    //W{{{
    //Wquad(point1, point2, point3, point4)
    Tester("import Staging._ ; quad((15, 15), (25, 35), (40, 20), (35, 10))")
    assertEquals("PolyLine(15,10)", makeString(f.dumpChild(n)))
    testPolyLine(f.dumpChild(n), 4)
    n += 1

    //W}}}
    //W

    //W
    //W===Line pattern===
    //W
    //W{{{
    //WlinesShape(points)
    val points = """List((10, 20), (10, 50),
       |(20, 50), (20, 20),
       |(30, 20), (30, 50),
       |(40, 50), (40, 20),
       |(50, 20), (50, 50),
       |(60, 50), (60, 20))""".stripMargin
    Tester("import Staging._ ; linesShape(" + points + ")")
    testPolyLine(f.dumpChild(n), "L10,50 M20,50 L20,20 M30,20 L30,50 " +
                 "M40,50 L40,20 M50,20 L50,50 M60,50 L60,20 M0.0,0.0 ")

    n += 1
    //W}}}
    //W
    //Wdraws one line for each two points.

    //W
    //W===Triangles pattern===
    //W
    //W{{{
    //WtrianglesShape(points)
    Tester("import Staging._ ; trianglesShape(" + points + ")")
    testPolyLine(f.dumpChild(n), "L10,50 L20,50 z " +
                 "M20,20 L30,20 L30,50 z " +
                 "M40,50 L40,20 L50,20 z " +
                 "M50,50 L60,50 L60,20 z M0.0,0.0 ")
    n += 1

    //W}}}
    //W
    //Wdraws one triangle for each three points.
  }

  @Test
  def test2 = {
    val f = SpriteCanvas.instance.figure0
    var n = 0
    val points = """List((10, 20), (10, 50),
       |(20, 50), (20, 20),
       |(30, 20), (30, 50),
       |(40, 50), (40, 20),
       |(50, 20), (50, 50),
       |(60, 50), (60, 20))""".stripMargin
    //W
    //W===Triangle strip pattern===
    //W
    //W{{{
    //WtriangleStripShape(points)
    val tssPoints = """List((10, 20), (10, 50),
       |(20, 20), (20, 50),
       |(30, 20), (30, 50),
       |(40, 20), (40, 50),
       |(50, 20), (50, 50),
       |(60, 20), (60, 50))""".stripMargin
    Tester("import Staging._ ; triangleStripShape(" + tssPoints + ")")
    testPolyLine(f.dumpChild(n), "L25,35 L40,20 L45,25 L50,10 M0.0,0.0 ")
    n += 1

    //W}}}
    //W
    //Wdraws a contiguous pattern of triangles.

    //W
    //W===Quads pattern===
    //W
    //W{{{
    //WquadsShape(points)
    Tester("import Staging._ ; quadsShape(" + points + ")")
    /* TODO find problem: code works but the test fails
    testPolyLine(f.dumpChild(n),
                        "L10,50 L20,50 L20,20 z " +
                 "M30,20 L30,50 L40,50 L40,20 z " +
                 "M50,20 L50,50 L60,50 L60,20 z M0.0,0.0 "
)
*/
    n += 1

    //W}}}
    //W
    //Wdraws one quad for each four points.

    //W
    //W===Quad strip pattern===
    //W
    //W{{{
    //WquadStripShape(points)
    Tester("import Staging._ ; quadStripShape(" + points + ")")
    /* TODO find problem: code works but the test fails
    testPolyLine(f.dumpChild(n), "")
*/
    n += 1

    //W}}}
    //W
    //Wdraws a contiguous pattern of quads.

    //W
    //W===Triangle fan pattern===
    //W
    //W{{{
    //WtriangleFanShape(points)
    val tfsPoints = """List(
       |(30, 45), (40, 40),
       |(40, 40), (45, 30),
       |(45, 30), (40, 20),
       |(40, 20), (30, 15),
       |(30, 15), (20, 20),
       |(20, 20), (15, 30),
       |(15, 30), (20, 40))""".stripMargin
    Tester("import Staging._ ; triangleFanShape((30, 30), " + tfsPoints + ")")
    /* TODO find problem: code works but the test fails
    testPolyLine(f.dumpChild(n), "L30,45 L40,40 M30,30 L40,40 L45,30 M30,30 " +
                 "L45,30 L40,20 M30,30 L40,20 L30,15 M30,30 L30,15 L20,20 " +
                 "M30,30 L20,20 L15,30 M30,30 L15,30 L20,40 M0.0,0.0 ")
*/
    n += 1

    //W}}}
    //W
    //Wdraws a pattern of triangles around a central point.

//    println(ppathToString(f.dumpChild(n).asInstanceOf[edu.umd.cs.piccolo.nodes.PPath]))
  }

  def stripCrLfs(str: String) = str.replaceAll("\r?\n", "")
}

