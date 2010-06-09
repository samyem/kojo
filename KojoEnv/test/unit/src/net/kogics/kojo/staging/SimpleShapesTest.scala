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

import org.junit.Test
import org.junit.Assert._

import net.kogics.kojo.util._

class ASimpleShapesTest extends StagingTestBase {

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
    /*
    assertTrue(r.isInstanceOf[net.kogics.kojo.kgeom.PolyLine])
    val pl = r.asInstanceOf[net.kogics.kojo.kgeom.PolyLine]
    val at = new java.awt.geom.AffineTransform
    val pi = pl.polyLinePath.getPathIterator(at)
    var res = new StringBuffer
    val z = getPathString(pi, res).toString
    if (s != z) println(z)
    assertEquals(s, z)
    */
  }


  /*
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
  */

  def testPPath(r: PNode, path: String) = {
    /*
    assertTrue(r.isInstanceOf[edu.umd.cs.piccolo.nodes.PPath])
    val pp = r.asInstanceOf[edu.umd.cs.piccolo.nodes.PPath]
    val s = ppathToString(pp)
    if (path != s) println(s)
    assertEquals(path, s)
    */
  }

  val f = SpriteCanvas.instance.figure0
  var n = 0
  def peekPNode = {
    val res = f.dumpChild(n)
    n += 1
    res
  }
  def skipNode { n += 1 }

  @Test
  // lalit sez: if we have more than five tests, we run out of heap space - maybe a leak in the Scala interpreter/compiler
  // subsystem. So we run (mostly) everything in one test
  def test1 = {
    //W
    //W==Simple Shapes==
    //W
    //W===Dots===
    //W
    //WA dot is defined by a single coordinate pair, given as _x_, _y_ values
    //Wor as a `Point`.
    //W
    //W{{{
    //Wdot(x, y)
    Tester("Staging.dot(15, 10)")
    assertEquals("PPoint(15,10)", makeString(peekPNode))

    //Wdot(point)
    Tester("Staging.dot(Staging.point(15, 10))")
    assertEquals("PPoint(15,10)", makeString(peekPNode))

    //W}}}

    //W
    //W===Lines===
    //W
    //WA line is defined either by
    //W# a coordinate pair, given as _x_, _y_ values or as a `Point`, and a _width_-_height_ pair, or
    //W# two points.
    //W
    //W{{{
    //Wline(x, y, width, height)
    Tester("import Staging._ ; line(15, 15, 25, 5)")
    assertEquals(
      "PolyLine(15,15 L40.0000,20.0000 M0.00000,0.00000 )",
      makeString(peekPNode)
    )

    //Wline(point1, width, height)
    Tester("import Staging._ ; line((15, 15), 25, 5)")
    assertEquals(
      "PolyLine(15,15 L40.0000,20.0000 M0.00000,0.00000 )",
      makeString(peekPNode)
    )

    //Wline(point1, point2)
    Tester("import Staging._ ; line((15, 15), (40, 20))")
    assertEquals(
      "PolyLine(15,15 L40.0000,20.0000 M0.00000,0.00000 )",
      makeString(peekPNode)
    )
    //W}}}
    //W

    //W
    //WA line can also be defined from an existing shape:
    //W
    //W{{{
    //WaDot.toLine(p1)
    Tester("""import Staging._
             |val p0 = point(15, 15)
             |val p1 = point(30, 40)
             |val aDot = dot(p0)
             |aDot.toLine(p1)""".stripMargin)
    skipNode
    assertEquals(
      "PolyLine(15,15 L30.0000,40.0000 M0.00000,0.00000 )",
      makeString(peekPNode)
    )

    //WaLine.toLine
    Tester("""import Staging._
             |val p0 = point(15, 15)
             |val p1 = point(30, 40)
             |val aLine = line(p0, p1)
             |aLine.toLine""".stripMargin)
    skipNode
    assertEquals(
      "PolyLine(15,15 L30.0000,40.0000 M0.00000,0.00000 )",
      makeString(peekPNode)
    )

    //WaRect.toLine
    Tester("""import Staging._
             |val p0 = point(15, 15)
             |val p1 = point(30, 40)
             |val aRect = rectangle(p0, p1)
             |aRect.toLine""".stripMargin)
    skipNode
    assertEquals(
      "PolyLine(15,15 L30.0000,40.0000 M0.00000,0.00000 )",
      makeString(peekPNode)
    )

    //WaRoundRectangle.toLine
    Tester("import Staging._ ; roundRectangle((15, 15), (40, 20), (3, 5)).toLine")
    skipNode
    assertEquals(
      "PolyLine(15,15 L40.0000,20.0000 M0.00000,0.00000 )",
      makeString(peekPNode)
    )

    //W}}}
    //W

    //W
    //W===Rectangles===
    //W
    //WA rectangle is defined the same way as a line, either by
    //W# a coordinate pair for the lower left corner, given as _x_, _y_ values or as a `Point`, and a _width_-_height_ pair, or
    //W# two points (in lower left / upper right order).
    //W# from another shape.
    //W
    //W{{{
    //Wrectangle(x, y, width, height)
    Tester("import Staging._ ; rectangle(15, 15, 25, 5)")
    assertEquals(
      "PPath(15,15 L40.0000,15.0000 L40.0000,20.0000 L15.0000,20.0000 " +
              "L15.0000,15.0000 z M0.00000,0.00000 )",
      makeString(peekPNode)
    )

    //Wrectangle(point1, width, height)
    Tester("import Staging._ ; rectangle((15, 15), 25, 5)")
    assertEquals(
      "PPath(15,15 L40.0000,15.0000 L40.0000,20.0000 L15.0000,20.0000 " +
              "L15.0000,15.0000 z M0.00000,0.00000 )",
      makeString(peekPNode)
    )

    //Wrectangle(point1, point2)
    Tester("import Staging._ ; rectangle((15, 15), (40, 20))")
    assertEquals(
      "PPath(15,15 L40.0000,15.0000 L40.0000,20.0000 L15.0000,20.0000 " +
              "L15.0000,15.0000 z M0.00000,0.00000 )",
      makeString(peekPNode)
    )

    //WaLine.toRect
    Tester("""import Staging._
             |val p0 = point(15, 15)
             |val p1 = point(30, 40)
             |val aLine = line(p0, p1)
             |aLine.toRect""".stripMargin)
    skipNode
    assertEquals(
      "PPath(15,15 L30.0000,15.0000 L30.0000,40.0000 " +
              "L15.0000,40.0000 L15.0000,15.0000 z M0.00000,0.00000 )",
      makeString(peekPNode)
    )

    //WaRect.toRect
    Tester("""import Staging._
             |val p0 = point(15, 15)
             |val p1 = point(30, 40)
             |val aRect = rectangle(p0, p1)
             |aRect.toRect""".stripMargin)
    skipNode
    assertEquals(
      "PPath(15,15 L30.0000,15.0000 L30.0000,40.0000 " +
              "L15.0000,40.0000 L15.0000,15.0000 z M0.00000,0.00000 )",
      makeString(peekPNode)
    )

    //WaRoundRectangle.toRect
    Tester("import Staging._ ; roundRectangle((15, 15), (40, 20), (3, 5)).toRect")
    skipNode
    assertEquals(
      "PPath(15,15 L40.0000,15.0000 L40.0000,20.0000 L15.0000,20.0000 " +
        "L15.0000,15.0000 z M0.00000,0.00000 )",
      makeString(peekPNode)
    )

    //W}}}
    //W
    //W{{{
    //Wsquare(x, y, size)
    Tester("import Staging._ ; square(15, 15, 20)")
    assertEquals(
      "PPath(15,15 L35.0000,15.0000 L35.0000,35.0000 L15.0000,35.0000 " +
        "L15.0000,15.0000 z M0.00000,0.00000 )",
      makeString(peekPNode)
    )

    //Wsquare(point, size)
    Tester("import Staging._ ; square((15, 15), 20)")
    assertEquals(
      "PPath(15,15 L35.0000,15.0000 L35.0000,35.0000 L15.0000,35.0000 " +
        "L15.0000,15.0000 z M0.00000,0.00000 )",
      makeString(peekPNode)
    )

    //W}}}
    //W

    //W
    //W===Rectangles with round corners===
    //W
    //WA rectangle with rounded corners is defined just like a rectangle, with
    //Wan additional _x-radius_, _y-radius_ pair or point that defines the
    //Wcurvature of the corners.
    //W
    //W{{{
    //WroundRectangle(x, y, width, height, radiusx, radiusy)
    Tester("import Staging._ ; roundRectangle(15, 15, 25, 5, 3, 5)")
    skipNode
    //WroundRectangle(point1, width, height, radiusx, radiusy)
    Tester("import Staging._ ; roundRectangle((15, 15), 25, 5, 3, 5)")
    skipNode
    //WroundRectangle(point1, point2, radiusx, radiusy)
    Tester("import Staging._ ; roundRectangle((15, 15), (40, 20), 3, 5)")
    skipNode
    //WroundRectangle(point1, point2, point3)
    Tester("import Staging._ ; roundRectangle((15, 15), (40, 20), (3, 5))")
    assertEquals(
      "PPath(15,15 L15.0000,17.5000 C15.0000,18.8807 15.6716,20.0000 16.5000,20.0000 " +
        "L38.5000,20.0000 C39.3284,20.0000 40.0000,18.8807 40.0000,17.5000 " +
        "L40.0000,17.5000 C40.0000,16.1193 39.3284,15.0000 38.5000,15.0000 " +
        "L16.5000,15.0000 C15.6716,15.0000 15.0000,16.1193 15.0000,17.5000 " +
        "z M0.00000,0.00000 )",
      makeString(peekPNode)
    )

    //WaLine.toRect(p)
    Tester("""import Staging._
             |val p0 = point(15, 15)
             |val p1 = point(30, 40)
             |val aLine = line(p0, p1)
             |aLine.toRect((3, 5))""".stripMargin)
    skipNode
    skipNode

    //WaRect.toRect(p)
    Tester("""import Staging._
             |val p0 = point(15, 15)
             |val p1 = point(30, 40)
             |val p  = point(3, 5)
             |val aRect = rectangle(p0, p1)
             |aRect.toRect(p)""".stripMargin)
    skipNode
    skipNode

    //W}}}
    //W
    //WNote:
    //W
    //W{{{
    //WaRoundRectangle.toRect(point)
    Tester("import Staging._ ; roundRectangle((15, 15), (40, 20), (3, 5)).toRect(O)")
    skipNode
    assertEquals(
      "PPath(15,15 L15.0000,20.0000 C15.0000,20.0000 15.0000,20.0000 15.0000,20.0000 " +
        "L40.0000,20.0000 C40.0000,20.0000 40.0000,20.0000 40.0000,20.0000 " +
        "L40.0000,15.0000 C40.0000,15.0000 40.0000,15.0000 40.0000,15.0000 " +
        "L15.0000,15.0000 C15.0000,15.0000 15.0000,15.0000 15.0000,15.0000 " +
        "z M0.00000,0.00000 )",
      makeString(peekPNode)
    )

    //W}}}
    //W
    //Wdraws another instance of {{{RoundRectangle}}} with the same dimensions.
    //WThe argument to the method isn't used but must be present.
    //W
  }

  @Test
  def test2 = {

    //W
    //W===Ellipses===
    //W
    //WAn ellipse is defined by a center point and a curvature.  The center point
    //Wcan be given as _cx_, _cy_ coordinates or as a `Point`, and the
    //Wcurvature can be given as _rx_, _ry_ radii or as an absolute `Point`.
    //W
    //W(Thus, `ellipse((15, 15), 35, 25)` and `ellipse((15, 15), (50, 40))`
    //Wdefine the same shape.)
    //W
    //W{{{
    //Wellipse(cx, cy, rx, ry)
    Tester("import Staging._ ; ellipse(15, 15, 35, 25)")
    println(makeString(peekPNode)) ; assertTrue(false)
    testPPath(f.dumpChild(n),
              "m-21,-11 C50.0000,28.8071 34.3300,40.0000 15.0000,40.0000 " +
              "C-4.32997,40.0000 -20.0000,28.8071 -20.0000,15.0000 " +
              "C-20.0000,1.19288 -4.32997,-10.0000 15.0000,-10.0000 " +
              "C34.3300,-10.0000 50.0000,1.19288 50.0000,15.0000 " +
              "z M0.00000,0.00000 ")
    n += 1

    //Wellipse(p, rx, ry)
    Tester("import Staging._ ; ellipse((15, 15), 35, 25)")
    testPPath(f.dumpChild(n),
              "m-21,-11 C50.0000,28.8071 34.3300,40.0000 15.0000,40.0000 " +
              "C-4.32997,40.0000 -20.0000,28.8071 -20.0000,15.0000 " +
              "C-20.0000,1.19288 -4.32997,-10.0000 15.0000,-10.0000 " +
              "C34.3300,-10.0000 50.0000,1.19288 50.0000,15.0000 " +
              "z M0.00000,0.00000 ")
    n += 1

    //Wellipse(p1, p2)
    Tester("import Staging._ ; ellipse((15, 15), (50, 40))")
    testPPath(f.dumpChild(n),
              "m-21,-11 C50.0000,28.8071 34.3300,40.0000 15.0000,40.0000 " +
              "C-4.32997,40.0000 -20.0000,28.8071 -20.0000,15.0000 " +
              "C-20.0000,1.19288 -4.32997,-10.0000 15.0000,-10.0000 " +
              "C34.3300,-10.0000 50.0000,1.19288 50.0000,15.0000 " +
              "z M0.00000,0.00000 ")
    n += 1

    //W}}}
    //W
    //W{{{
    //Wcircle(cx, cy, radius)
    Tester("import Staging._ ; circle(15, 15, 25)")
    testPPath(f.dumpChild(n),
              "m-11,-11 C40.0000,28.8071 28.8071,40.0000 15.0000,40.0000 " +
              "C1.19288,40.0000 -10.0000,28.8071 -10.0000,15.0000 " +
              "C-10.0000,1.19288 1.19288,-10.0000 15.0000,-10.0000 " +
              "C28.8071,-10.0000 40.0000,1.19288 40.0000,15.0000 " +
              "z M0.00000,0.00000 ")
    n += 1

    //Wcircle(p, radius)
    Tester("import Staging._ ; circle((15, 15), 25)")
    testPPath(f.dumpChild(n),
              "m-11,-11 C40.0000,28.8071 28.8071,40.0000 15.0000,40.0000 " +
              "C1.19288,40.0000 -10.0000,28.8071 -10.0000,15.0000 " +
              "C-10.0000,1.19288 1.19288,-10.0000 15.0000,-10.0000 " +
              "C28.8071,-10.0000 40.0000,1.19288 40.0000,15.0000 " +
              "z M0.00000,0.00000 ")
    n += 1

    //W}}}
    //W

    //W
    //W===Elliptical arcs===
    //W
    //WAn elliptical arc is defined just like an ellipsis, with two additional
    //Warguments for _starting angle_ and _extent_.  A starting angle of 0
    //Wis the "three o'clock" direction, and 90 is the "twelve o'clock" direction.
    //WBoth angles are given in 1/360 degrees.
    //W
    //W{{{
    //Warc(cx, cy, rx, ry, s, e)
    Tester("import Staging._ ; arc(15, 15, 20, 10, 40, 95)")
    n += 1

    //Warc(cp, rx, ry, s, e)
    Tester("import Staging._ ; arc((15, 15), 20, 10, 40, 95)")
    n += 1

    //Warc(p1, p2, s, e)
    Tester("import Staging._ ; arc((15, 15), (20, 10), 40, 95)")
    n += 1

    //W}}}
    //W

    //W
    //W===Vectors===
    //W
    //WA vector is a specialized line with an arrowhead at the endpoint.  An
    //Wadditional argument specifies the length of the arrowhead.
    //W
    //W{{{
    //Wvector(x, y, width, height, length)
    Tester("import Staging._ ; vector(15, 15, 25, 5, 3)")
    testPolyLine(f.dumpChild(n), "L40.4951,15.0000 M40.4951,15.0000 " +
              "L37.4951,14.0000 L37.4951,16.0000 z M0.00000,0.00000 ")
    n += 1

    //Wvector(point1, width, height, length)
    Tester("import Staging._ ; vector((15, 15), 25, 5, 3)")
    testPolyLine(f.dumpChild(n), "L40.4951,15.0000 M40.4951,15.0000 " +
              "L37.4951,14.0000 L37.4951,16.0000 z M0.00000,0.00000 ")
    n += 1

    //Wvector(point1, point2, length)
    Tester("import Staging._ ; vector((15, 15), (40, 20), 3)")
    testPolyLine(f.dumpChild(n), "L40.4951,15.0000 M40.4951,15.0000 " +
              "L37.4951,14.0000 L37.4951,16.0000 z M0.00000,0.00000 ")
    n += 1
    //W}}}
    //W

//    println(ppathToString(f.dumpChild(n).asInstanceOf[edu.umd.cs.piccolo.nodes.PPath]))
  }
}

