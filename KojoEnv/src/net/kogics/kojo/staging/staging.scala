/*
 * Copyright (C) 2010 Peter Lewerin
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

import net.kogics.kojo.core.Point
import java.awt.Color
import math._

object Impl {
  val figure0 = SpriteCanvas.instance.figure0
  val canvas = SpriteCanvas.instance
}

object API {
  /* DISCLAIMER
   Parts of this interface is written to approximately
   conform to the Processing API as described in the
   reference at <URL: http://processing.org/reference/>.
   The implementation code is the work of Peter Lewerin
   <peter.lewerin@tele2.se> and is not in any way
   derived from the Processing source.
   */
  //W#summary Developer home-page for the Staging Module
  //W
  //W=Introduction=
  //W
  //WThe Staging Module is currently being developed by Peter Lewerin.
  //WThe original impetus came from a desire to run Processing-style code in Kojo.
  //W
  //WAt this point, the shape hierarchy is the most complete part, but
  //Wutilities for color definition, time keeping etc are being added.

  val O = Point(0, 0)
  def M = Point(Screen.width / 2, Screen.height / 2)
  def E = Point(Screen.width, Screen.height)

  //def point(x: Double, y: Double) = Point(x, y)
  def screenWidth = Screen.width
  def screenHeight = Screen.height
  def screenSize(width: Int, height: Int): (Int, Int) = Screen.size(width, height)

  implicit def tupleDToPoint(tuple: (Double, Double)) = Point(tuple._1, tuple._2)
  implicit def tupleIToPoint(tuple: (Int, Int)) = Point(tuple._1, tuple._2)
  implicit def ColorToRichColor (c: Color) = RichColor(c)

  def dot(x: Double, y: Double) = Dot(Point(x, y))
  def dot(p: Point) = Dot(p)

  def line(x: Double, y: Double, w: Double, h: Double): Line =
    Line(Point(x, y), Point(x + w, y + h))
  def line(p1: Point, w: Double, h: Double): Line = {
    Line(p1, Point(p1.x + w, p1.y + h))
  }
  def line(p1: Point, p2: Point): Line = {
    Line(p1, p2)
  }

  def rectangle(x: Double, y: Double, w: Double, h: Double): Rectangle =
    Rectangle(Point(x, y), Point(x + w, y + h))
  def rectangle(p: Point, w: Double, h: Double): Rectangle =
    Rectangle(p, Point(p.x + w, p.y + h))
  def rectangle(p1: Point, p2: Point): Rectangle =
    Rectangle(p1, p2)
  def square(x: Double, y: Double, s: Double): Rectangle =
    Rectangle(Point(x, y), Point(x + s, y + s))
  def square(p: Point, s: Double): Rectangle =
    Rectangle(p, Point(p.x + s, p.y + s))

  def roundRectangle(
    x: Double, y: Double,
    w: Double, h: Double,
    rx: Double, ry: Double
  ) =
    RoundRectangle(Point(x, y), Point(x + w, y + h), Point(rx, ry))
  def roundRectangle(
    p: Point,
    w: Double, h: Double,
    rx: Double, ry: Double
  ) =
    RoundRectangle(p, Point(p.x + w, p.y + h), Point(rx, ry))
  def roundRectangle(p1: Point, p2: Point, rx: Double, ry: Double) =
    RoundRectangle(p1, p2, Point(rx, ry))
  def roundRectangle(p1: Point, p2: Point, p3: Point) =
    RoundRectangle(p1, p2, p3)

  def polyline(pts: Seq[Point]): Polyline = Polyline(pts)

  def polygon(pts: Seq[Point]): Polygon = Polygon(pts)
  def triangle(p0: Point, p1: Point, p2: Point) = polygon(Seq(p0, p1, p2))
  def quad(p0: Point, p1: Point, p2: Point, p3: Point) =
    polygon(Seq(p0, p1, p2, p3))

  def ellipse(x: Double, y: Double, rx: Double, ry: Double) =
    Ellipse(Point(x, y), Point(x + rx, y + ry))
  def ellipse(p: Point, rx: Double, ry: Double) =
    Ellipse(p, Point(p.x + rx, p.y + ry))
  def ellipse(p1: Point, p2: Point) =
    Ellipse(p1, p2)
  def circle(x: Double, y: Double, r: Double): Ellipse =
    Ellipse(Point(x, y), Point(x + r, y + r))
  def circle(p: Point, r: Double): Ellipse =
    Ellipse(p, Point(p.x + r, p.y + r))

  def arc(x: Double, y: Double, w: Double, h: Double, s: Double, e: Double): Arc =
    Arc(Point(x, y), Point(x + w / 2, y + h / 2), s, e)
  def arc(p: Point, w: Double, h: Double, s: Double, e: Double): Arc =
    Arc(p, Point(p.x + w / 2, p.y + h / 2), s, e)
  def arc(p1: Point, p2: Point, s: Double, e: Double): Arc =
    Arc(p1, p2, s, e)

  def linesShape(pts: Seq[Point]): LinesShape = LinesShape(pts)

  def trianglesShape(pts: Seq[Point]) = TrianglesShape(pts)

  def triangleStripShape(pts: Seq[Point]) = TriangleStripShape(pts)

  def quadsShape(pts: Seq[Point]) = QuadsShape(pts)

  def quadStripShape(pts: Seq[Point]) = QuadStripShape(pts)

  def triangleFanShape(p0: Point, pts: Seq[Point]) = TriangleFanShape(p0, pts)

  def svgShape(node: scala.xml.Node): SvgShape = SvgShape(node)

  def millis = System.currentTimeMillis()

  def second = (millis / 1000) % 60
  def minute = (millis / 60000) % 60

  import java.util.Calendar
  def hour   = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
  def day    = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
  def month  = Calendar.getInstance().get(Calendar.MONTH) + 1
  def year   = Calendar.getInstance().get(Calendar.YEAR)

  def lerpColor(from: RichColor, to: RichColor, amt: Double) =
    RichColor.lerpColor(from, to, amt)

  def constrain(value: Double, min: Double, max: Double) =
    Math.constrain(value, min, max)

  def norm(value: Double, low: Double, high: Double) =
    Math.map(value, low, high, 0, 1)

  def map(value: Double, low1: Double, high1: Double, low2: Double, high2: Double) =
    Math.map(value, low1, high1, low2, high2)

  def lerp(value1: Double, value2: Double, amt: Double) =
    Math.lerp(value1, value2, amt)

  def sq(x: Double) = x * x

  def dist(x0: Double, y0: Double, x1: Double, y1: Double) =
    sqrt(sq(x1 - x0) + sq(y1 - y0))

  def mag(x: Double, y: Double) = dist(0, 0, x, y)
} // end of API



//W
//W=Points=
//W
//WStaging uses {{{net.kogics.kojo.core.Point}}} for coordinates.  A companion
//Wobject provides _apply_ and _unapply_ methods.
//WTuples of {{{Double}}}s or {{{Int}}}s are implicitly converted to
//W{{{Point}}}s where applicable.
object Point {
  def apply(x: Double, y: Double) = new Point(x, y)
  def unapply(p: Point) = Some((p.x, p.y))
}

//W
//W=Screen=
//W
//WStaging defines an object {{{Screen}}} that provides methods to set
//Wbackground color and screen size.
object Screen {
  var width = 0
  var height = 0
  def size(width: Int, height: Int): (Int, Int) = {
    this.width = width
    this.height = height
    // TODO make less ad-hoc
    Impl.canvas.zoom(560 / height, width / 2, height / 2)
    (width, height)
  }
}

//W=Shapes=
//W
//W==Summary==
//W|| *Class* or trait     || *Extends*                 || *Defines* (methods in italics)  ||
//W|| Shape                ||                           || _draw_                          ||
//W
//W==Shape (trait)==
//W
//W{{{Shape}}} is the base type for all shapes.  Every class that extends
//Wit must implement the nullary method _draw_.  This method should create
//Wan instance of the shape and add it to the canvas.
//W
//W_Not implemented yet: shapes should remember the colors and stroke style
//Wused and any transforms applied, and apply them again whenever _draw_ is
//Wcalled._
trait Shape {
  def draw: Shape
}
//W|| Rounded              ||                           || curvature, _radiusX_, _radiusY_ ||
//W
//W==Rounded (trait)==
//W
//W{{{Rounded}}} is a base type for shapes with rounded parts.  Every class
//Wthat extends it must have a value member, curvature, of type {{{Point}}}.
//WIt defines _radiusX_, _radiusY_ as access methods to the _x_ and _y_
//Wcomponents of curvature.
trait Rounded {
  val curvature: Point
  def radiusX = curvature.x
  def radiusY = curvature.y
}
//W|| !BaseShape           || Shape                     || origin, _toLine_                ||
//W
//W==!BaseShape (trait)==
//W
//W{{{BaseShape}}} is the base type for shapes that have a point of origin.
//WThis value member of type {{{Point}}} defines the lower left corner of
//Wthe shape bounds (except for {{{Elliptical}}} shapes, see below).
trait BaseShape extends Shape {
  val origin: Point
  def toLine(p: Point): Line = Line(origin, p)
}

//W|| !SimpleShape         || !BaseShape|| endpoint, _width_, _height_, _toLine_, _toRect_ ||
//W
//W==!SimpleShape (trait)==
//W
//W{{{SimpleShape}}} is the base type for shapes that are defined by two
//Wpoints, origin and endpoint (the upper right corner of the shape bounds).
//WThey have _width_ and _height_.  Every class that extends this trait must
//Whave value members origin and endpoint, of type {{{Point}}}.
trait SimpleShape extends BaseShape {
  val endpoint: Point
  def width = endpoint.x - origin.x
  def height = endpoint.y - origin.y
  def toLine: Line = Line(origin, endpoint)
  def toRect: Rectangle = Rectangle(origin, endpoint)
  def toRect(p: Point): RoundRectangle = RoundRectangle(origin, endpoint, p)
}

//W|| Elliptical           || Rounded with !SimpleShape || `*`                             ||
//W`*`: {{{Elliptical}}} implements {{{curvature}}} and overrides {{{width}}} and {{{height}}}.
//W
//W==Elliptical (trait)==
//W
//W{{{Elliptical}}} is the base type for shapes that are rounded and whose
//Worigin value member defines their center.
trait Elliptical extends Rounded with SimpleShape {
  val curvature = endpoint - origin
  override def width = 2 * radiusX
  override def height = 2 * radiusY
}

//W|| *Dot*                || !BaseShape                ||                                 ||
//W
//W==Dot==
//W
//W{{{Dot}}} is drawn to the canvas as a dot of the stroke color.
class Dot(val origin: Point) extends BaseShape {
  def draw = {
    origin match {
      case Point(x, y) =>
        Impl.figure0.point(x, y)
    }
    this
  }

  override def toString = "Staging.Dot(" + origin + ")"
}
object Dot {
  def apply(p: Point) = {
    val shape = new Dot(p)
    shape.draw
  }
}

//W|| *Line*               || !SimpleShape              ||                                 ||
//W
//W==Line==
//W
//W{{{Line}}} is drawn to the canvas as a straight line of the stroke color
//Wfrom origin to endpoint.
class Line(val origin: Point, val endpoint: Point) extends SimpleShape {
  def draw = {
    Impl.figure0.line(origin, endpoint)
    this
  }
  override def toString = "Staging.Line(" + origin + ", " + endpoint + ")"
}
object Line {
  def apply(p1: Point, p2: Point) = {
    val shape = new Line(p1, p2)
    shape.draw
  }
}

//W|| *Rectangle*          || !SimpleShape              ||                                 ||
//W
//W==Rectangle==
//W
//W{{{Rectangle}}} is drawn to the canvas as a rectangle of the fill and
//Wstroke color from origin to endpoint.
//W
//WThe width and height of the rectangle must both be positive, so if the
//Wdimensions are given in the form of a {{{Point}}} it must be to the right
//Wof and above origin.
class Rectangle(val origin: Point, val endpoint: Point) extends SimpleShape {
  // precondition endpoint > origin
  require(width > 0 && height > 0)
  def draw = {
    Impl.figure0.rectangle(origin, endpoint)
    this
  }
  override def toString = "Staging.Rectangle(" + origin + ", " + endpoint + ")"
}
object Rectangle {
  def apply(p1: Point, p2: Point) = {
    val shape = new Rectangle(p1, p2)
    shape.draw
  }
}


//W|| !PolyShape           || Shape                   || points, _toPolyline_, _toPolygon_ ||
//W
//W==!PolyShape (trait)==
//W
//W{{{PolyShape}}} is the base type for shapes that are defined by several
//Wpoints.  The points are stored in a value member of type sequence of
//W{{{Point}}}s.
trait PolyShape extends Shape {
  val points: Seq[Point]
  def toPolygon: Polygon = Polygon(points)
  def toPolyline: Polyline = Polyline(points)
}

//W|| *!RoundRectangle*    || Rounded with !SimpleShape ||                                 ||
//W
//W==!RoundRectangle==
//W
//W{{{RoundRectangle}}} is drawn to the canvas as a rectangle with rounded
//Wcorners of the fill and stroke color from origin to endpoint.  The
//Wcurvature of the corners can be determined by x-radius and y-radius
//Wvalues or by a point value.
//W
//WThe width and height of the rectangle must both be positive, so if the
//Wdimensions are given in the form of a {{{Point}}} it must be to the right
//Wof and above origin.
class RoundRectangle(
  val origin: Point,
  val endpoint: Point,
  val curvature: Point
) extends Rounded with SimpleShape {
  // precondition endpoint > origin
  require(width > 0 && height > 0)
  def draw = {
    Impl.figure0.roundRectangle(origin, endpoint, radiusX, radiusY)
    this
  }
  override def toString =
    "Staging.RoundRectangle(" + origin + ", " + endpoint + ", " + curvature + ")"
}
object RoundRectangle {
  def apply(p1: Point, p2: Point, p3: Point) = {
    val shape = new RoundRectangle(p1, p2, p3)
    shape.draw
  }
}

//W|| *Polyline*           || !PolyShape                ||                                 ||
//W
//W==Polyline==
//W
//W{{{Polyline}}} is drawn to the canvas as a segmented line connecting the
//Wgiven points by straight edges, using the fill and stroke color.
class Polyline(val points: Seq[Point]) extends PolyShape {
  val shapePath = new kgeom.PolyLine()
  points foreach { case Point(x, y) =>
      shapePath.addPoint(x, y)
  }
  def draw = {
    Impl.figure0.polyLine(shapePath)
    this
  }

  override def toString = "Staging.Polyline(" + points + ")"
}
object Polyline {
  def apply(pts: Seq[Point]) = {
    val shape = new Polyline(pts)
    shape.draw
  }
}

//W|| *Polygon*            || !PolyShape                ||                                 ||
//W
//W==Polygon==
//W
//W{{{Polygon}}} is drawn to the canvas as a segmented line connecting the
//Wgiven points by straight edges, using the fill and stroke color.  The
//Wshape is closed, meaning that the last point connects to the first point.
class Polygon(val points: Seq[Point]) extends PolyShape {
  val shapePath = new kgeom.PolyLine()
  points foreach { case Point(x, y) =>
      shapePath.addPoint(x, y)
  }
  shapePath.polyLinePath.closePath
  def draw = {
    Impl.figure0.polyLine(shapePath)
    this
  }

  override def toString = "Staging.Polygon(" + points + ")"
}
object Polygon {
  def apply(pts: Seq[Point]) = {
    val shape = new Polygon(pts)
    shape.draw
  }
}

//W|| *Ellipse*            || Elliptical                ||                                 ||
//W
//W==Ellipse==
//W
//W{{{Ellipse}}} is drawn to the canvas as an ellipse of the fill and stroke
//Wcolor centering on origin, with a curvature defined by the distance from
//Worigin to endpoint.
class Ellipse(val origin: Point, val endpoint: Point) extends Elliptical {
  def draw = {
    Impl.figure0.ellipse(origin, width, height)
    this
  }
}
object Ellipse {
  def apply(p1: Point, p2: Point) = {
    val shape = new Ellipse(p1, p2)
    shape.draw
  }
}

//W|| *Arc*                || Elliptical                || start, extent                   ||
//W
//W==Arc==
//W
//W{{{Arc}}} is drawn to the canvas as an elliptical sector of the fill and
//Wstroke color centering on origin, with a curvature defined by the
//Wdistance from origin to endpoint.  The class defines two value members of
//Wtype {{{Double}}}: start is angle where the arc begins, and extent is the
//Wangle between start and end of the arc.  Both angles are given in degrees,
//Wwith 0 at "three o'clock", 90 at "twelve o'clock" and so on.
class Arc(
  val origin: Point, val endpoint: Point,
  val start: Double, val extent: Double
) extends Elliptical {
  def draw = {
    origin match {
      case Point(x, y) =>
        Impl.figure0.arc(x, y, width, height, start, extent)
    }
    this
  }
}
object Arc {
  def apply(p1: Point, p2: Point, s: Double, e: Double) = {
    val shape = new Arc(p1, p2, s, e)
    shape.draw
  }
}

//W|| *!LinesShape*        || !PolyShape                ||                                 ||
//W
//W==!LinesShape==
//W
//W{{{LinesShape}}} takes a sequence of {{{Point}}}s and connects them
//Wpairwise by straight lines of the stroke color.
class LinesShape(val points: Seq[Point]) extends PolyShape {
  def draw = {
    points grouped(2) foreach {
      case List() =>
      case Seq(p0, p1) =>
        API.line(p0, p1)
      case Point(x, y) :: Nil =>
        API.dot(x, y)
    }
    this
  }
}
object LinesShape {
  def apply(pts: Seq[Point]) = {
    val shape = new LinesShape(pts)
    shape.draw
  }
}

//W|| *!TrianglesShape*    || !PolyShape                ||                                 ||
//W
//W==!TrianglesShape==
//W
//W{{{TrianglesShape}}} takes a sequence of {{{Point}}}s and connects them
//Was triangles of the fill and stroke color.
class TrianglesShape(val points: Seq[Point]) extends PolyShape {
  def draw = {
    points grouped(3) foreach {
      case List() =>
      case s @ Seq(p0, p1, p2) =>
        API.polygon(s)
      case p0 :: p1 :: Nil =>
        API.line(p0, p1)
      case Point(x, y) :: Nil =>
        API.dot(x, y)
    }
    this
  }
}
object TrianglesShape {
  def apply(pts: Seq[Point]) = {
    val shape = new TrianglesShape(pts)
    shape.draw
  }
}

//W|| *!TriangleStripShape*|| !PolyShape                ||                                 ||
//W
//W==!TriangleStripShape==
//W
//W{{{TriangleStripShape}}} takes a sequence of {{{Point}}}s and connects
//Wthem as adjoining triangles of the fill and stroke color.
class TriangleStripShape(val points: Seq[Point]) extends PolyShape {
  def draw = {
    points sliding(3) foreach {
      case List() =>
      case s @ Seq(p0, p1, p2) =>
        API.polygon(s)
      case p0 :: p1 :: Nil =>
        API.line(p0, p1)
      case Point(x, y) :: Nil =>
        API.dot(x, y)
    }
    this
  }
}
object TriangleStripShape {
  def apply(pts: Seq[Point]) = {
    val shape = new TriangleStripShape(pts)
    shape.draw
  }
}

//W|| *!QuadsShape*        || !PolyShape                ||                                 ||
//W
//W==!QuadsShape==
//W
//W{{{QuadsShape}}} takes a sequence of {{{Point}}}s and connects them as
//Wquads (polygons of four points) of the fill and stroke color.
class QuadsShape(val points: Seq[Point]) extends PolyShape {
  def draw = {
    points grouped(4) foreach {
      case List() =>
      case s @ Seq(p0, p1, p2, p3) =>
        API.polygon(s)
      case s @ p0 :: p1 :: p2 :: Nil =>
        Polyline(s)
      case p0 :: p1 :: Nil =>
        API.line(p0, p1)
      case Point(x, y) :: Nil =>
        API.dot(x, y)
    }
    this
  }
}
object QuadsShape {
  def apply(pts: Seq[Point]) = {
    val shape = new QuadsShape(pts)
    shape.draw
  }
}

//W|| *!QuadStripShape*    || !PolyShape                ||                                 ||
//W
//W==!QuadStripShape==
//W
//W{{{QuadStripShape}}} takes a sequence of {{{Point}}}s and connects them
//Was adjoining quads of the fill and stroke color.
class QuadStripShape(val points: Seq[Point]) extends PolyShape {
  def draw = {
    points sliding(4, 2) foreach {
      case List() =>
      case s @ Seq(p0, p1, p2, p3) =>
        API.polygon(s)
      case s @ p0 :: p1 :: p2 :: Nil =>
        API.polyline(s)
      case p0 :: p1 :: Nil =>
        API.line(p0, p1)
      case Point(x, y) :: Nil =>
        API.dot(x, y)
    }
    this
  }
}
object QuadStripShape {
  def apply(pts: Seq[Point]) = {
    val shape = new QuadStripShape(pts)
    shape.draw
  }
}

//W|| *!TriangleFanShape*  || !PolyShape with !BaseShape||                                 ||
//W
//W==!TriangleFanShape==
//W
//W{{{TriangleFanShape}}} takes a center point (origin) and a sequence of
//W{{{Point}}}s, and connects the points pairwise with each other and with
//Wthe center point with straight edges of the fill and stroke color.
class TriangleFanShape(val origin: Point, val points: Seq[Point]) extends PolyShape
                                                                     with BaseShape {
  def draw = {
    points grouped(2) foreach {
      case List() =>
      case s @ Seq(p0, p1) =>
        API.polyline(Seq(origin) ++ s)
      case p1 :: Nil =>
        API.line(origin, p1)
    }
    this
  }
}
object TriangleFanShape {
  def apply(p0: Point, pts: Seq[Point]) = {
    val shape = new TriangleFanShape(p0, pts)
    shape.draw
  }
}

//W|| *!SvgShape*  || Shape || node                            ||
//W
//W==!SvgShape==
//W
//W{{{SvgShape}}} takes a SVG element (rect, circle, ellipse, line, polyline,
//Wpolygon, or path) and draws it as a shape of the fill and stroke color.
//W
//WTODO: Should handle g and svg elements in the future.
class SvgShape(val node: scala.xml.Node) extends Shape {
  private def matchXY (ns: scala.xml.Node, xn: String = "x", yn: String = "y"): (Double, Double) = {
    val xStr = (ns \ ("@" + xn)).toString
    val yStr = (ns \ ("@" + yn)).toString
    val x = if (xStr == "") 0 else xStr.toDouble
    val y = if (yStr == "") 0 else yStr.toDouble
    (x, y)
  }

  private def matchWH (ns: scala.xml.Node): (Double, Double) = {
    val widthStr = (ns \ "@width").toString
    val heightStr = (ns \ "@height").toString
    val width = if (widthStr == "") 0 else widthStr.toDouble
    val height = if (heightStr == "") 0 else heightStr.toDouble
    require(width >= 0, "Bad width for XML element " + ns)
    require(height >= 0, "Bad height for XML element " + ns)
    (width, height)
  }

  private def matchRXY (ns: scala.xml.Node): (Double, Double) = {
    val xStr = (ns \ "@rx").toString
    val yStr = (ns \ "@ry").toString
    val x = if (xStr == "") 0 else xStr.toDouble
    require(x >= 0, "Bad rx for XML element " + ns)
    val y = if (yStr == "") 0 else yStr.toDouble
    require(y >= 0, "Bad ry for XML element " + ns)
    val rx = if (x != 0) x else y
    val ry = if (y != 0) y else x
    (rx, ry)
  }

  private def matchFillStroke (ns: scala.xml.Node): (Option[Color], Option[Color]) = {
    val fStr = (ns \ "@fill").toString
    val sStr = (ns \ "@stroke").toString
    //TODO
    (None, None)
  }

  private def matchPoints (ns: scala.xml.Node): Seq[Point] = {
    val pointsStr = (node \ "@points").toString
    val splitter = "(:?,\\s*|\\s+)".r
    val pointsItr = (splitter split pointsStr) map (_.toDouble) grouped(2)
    (pointsItr map { a => Point(a(0), a(1)) }).toList
  }

  def draw = {
    // should handle some of
    //   color, fill-rule, stroke, stroke-dasharray, stroke-dashoffset,
    //   stroke-linecap, stroke-linejoin, stroke-miterlimit, stroke-width,
    //   color-interpolation, color-rendering
    // and
    //   transform-list
    node match {
      case <rect></rect> =>
        val (x, y) = matchXY(node)
        val (width, height) = matchWH(node)
        val (fc, sc) = matchFillStroke(node)
        val (rx, ry) = matchRXY(node)
        if (rx != 0) {
          API.roundRectangle(x, y, width, height, rx, ry)
        } else {
          API.rectangle(x, y, width, height)
        }
      case <circle></circle> =>
        val (cx, cy) = matchXY(node, "cx", "cy")
        val rStr = (node \ "@r").toString
        val r = if (rStr == "") 0 else rStr.toDouble
        API.circle(cx, cy, r)
      case <ellipse></ellipse> =>
        val (cx, cy) = matchXY(node, "cx", "cy")
        val (rx, ry) = matchRXY(node)
        API.ellipse(cx, cy, rx, ry)
      case <line></line> =>
        val (x1, y1) = matchXY(node, "x1", "y1")
        val (x2, y2) = matchXY(node, "x2", "y2")
        API.line(x1, y1, x2 - x1, y2 - y1)
      case <polyline></polyline> =>
        val points = matchPoints(node)
        API.polyline(points)
      case <polygon></polygon> =>
        val points = matchPoints(node)
        API.polygon(points)
      case <path></path> =>
        val d = (node \ "@d").toString
        Impl.figure0.path(d)
      case <g>{ shapes @ _* }</g> =>
        for (s <- shapes) {
          API.svgShape(s)
        }
      case <svg>{ shapes @ _* }</svg> =>
        for (s <- shapes) {
          API.svgShape(s)
        }
      case _ => // unknown element, ignore
    }
    this
  }
}
object SvgShape {
  def apply(node: scala.xml.Node) = {
    val shape = new SvgShape(node)
    shape.draw
  }
}

class RichColor (val c: Color) {
  def alpha = c.getAlpha
  def red = c.getRed
  def blue = c.getBlue
  def green = c.getGreen
  private def hsb =
    Color.RGBtoHSB(c.getRed, c.getBlue, c.getGreen, null)
  def hue = {
    val h = floor(255 * (1 - this.hsb(0))) + 1
    if (h > 255) 0 else h.toInt
  }
  def saturation = (this.hsb(1) * 255).toInt
  def brightness = (this.hsb(2) * 255).toInt
  // TODO blendColor
}
object RichColor {
  def apply(c: Color) = new RichColor(c)

  def lerpColor(from: RichColor, to: RichColor, amt: Double) = {
    require(amt >= 0d && amt <= 1d)
    new Color(
      Math.lerp(from.red, to.red, amt).round.toInt,
      Math.lerp(from.green, to.green, amt).round.toInt,
      Math.lerp(from.blue, to.blue, amt).round.toInt
    )
  }
}

object Math {
  def constrain(value: Double, min: Double, max: Double) = {
    if (value < min) min
    else if (value > max) max
    else value
  }

  def map(value: Double, low1: Double, high1: Double, low2: Double, high2: Double) = {
    val range1: Double = high1 - low1
    val range2: Double = high2 - low2
    if (value >= low1 && value <= high1) range2 * value / range1
    else value
  }

  def lerp(value1: Double, value2: Double, amt: Double) = {
    require(amt >= 0d && amt <= 1d)
    val range: Double = value2 - value1
    value1 + amt * range
  }
}
