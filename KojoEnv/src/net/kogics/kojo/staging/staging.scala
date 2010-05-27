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

import edu.umd.cs.piccolo.PNode

import net.kogics.kojo.util.Utils
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
  //W
  //W=Overview=
  //W
  //W==Points==
  //W
  //WStaging uses {{{net.kogics.kojo.core.Point}}} for coordinates.
  //W

  val O = Point(0, 0)
  def M = Point(Screen.width / 2, Screen.height / 2)
  def E = Point(Screen.width, Screen.height)

  def point(x: Double, y: Double) = Point(x, y)

  //W
  //W==User Screen==
  //W
  //WThe zoom level and axis orientations can be set using `screenSize`.
  //W
  def screenWidth = Screen.width
  def screenHeight = Screen.height
  def screenSize(width: Int, height: Int) = Screen.size(width, height)

  implicit def tupleDToPoint(tuple: (Double, Double)) = Point(tuple._1, tuple._2)
  implicit def tupleIToPoint(tuple: (Int, Int)) = Point(tuple._1, tuple._2)
  implicit def ColorToRichColor (c: Color) = RichColor(c)

  def dot(x: Double, y: Double) = Dot(Point(x, y))
  def dot(p: Point) = Dot(p)

  def line(x: Double, y: Double, w: Double, h: Double) =
    Line(Point(x, y), Point(x + w, y + h))
  def line(p1: Point, w: Double, h: Double) =
    Line(p1, Point(p1.x + w, p1.y + h))
  def line(p1: Point, p2: Point) =
    Line(p1, p2)

  def rectangle(x: Double, y: Double, w: Double, h: Double) =
    Rectangle(Point(x, y), Point(x + w, y + h))
  def rectangle(p: Point, w: Double, h: Double) =
    Rectangle(p, Point(p.x + w, p.y + h))
  def rectangle(p1: Point, p2: Point) =
    Rectangle(p1, p2)
  def square(x: Double, y: Double, s: Double) =
    Rectangle(Point(x, y), Point(x + s, y + s))
  def square(p: Point, s: Double) =
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

  def polyline(pts: Seq[Point]) = Polyline(pts)

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
  def circle(x: Double, y: Double, r: Double) =
    Ellipse(Point(x, y), Point(x + r, y + r))
  def circle(p: Point, r: Double) =
    Ellipse(p, Point(p.x + r, p.y + r))

  def arc(x: Double, y: Double, w: Double, h: Double, s: Double, e: Double) =
    Arc(Point(x, y), Point(x + w / 2, y + h / 2), s, e)
  def arc(p: Point, w: Double, h: Double, s: Double, e: Double) =
    Arc(p, Point(p.x + w / 2, p.y + h / 2), s, e)
  def arc(p1: Point, p2: Point, s: Double, e: Double) =
    Arc(p1, p2, s, e)

  def linesShape(pts: Seq[Point]) = LinesShape(pts)

  def trianglesShape(pts: Seq[Point]) = TrianglesShape(pts)

  def triangleStripShape(pts: Seq[Point]) = TriangleStripShape(pts)

  def quadsShape(pts: Seq[Point]) = QuadsShape(pts)

  def quadStripShape(pts: Seq[Point]) = QuadStripShape(pts)

  def triangleFanShape(p0: Point, pts: Seq[Point]) = TriangleFanShape(p0, pts)

  def svgShape(node: scala.xml.Node) = SvgShape(node)

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
  def dist(p1: Point, p2: Point) =
    sqrt(sq(p2.x - p1.x) + sq(p2.y - p1.y))

  def mag(x: Double, y: Double) = dist(0, 0, x, y)
  def mag(p: Point) = dist(0, 0, p.x, p.y)
  //W
  //W=Usage=
  //W
} // end of API



object Point {
  def apply(x: Double, y: Double) = new Point(x, y)
  def unapply(p: Point) = Some((p.x, p.y))
}

object Screen {
  var width = 0
  var height = 0
  def size(width: Int, height: Int): (Int, Int) = {
    // TODO 560 is an value that works on my system, should be less ad-hoc
    val factor = 560
    val xfactor = factor / (if (width < 0) -(height.abs) else height.abs) // sic!
    val yfactor = factor / height
    Impl.canvas.zoomXY(xfactor, yfactor, width / 2, height / 2)
    this.width = width.abs
    this.height = height.abs
    (this.width, this.height)
  }
}

//M=Shapes=
//M
//M==Summary==
//M|| *Class* or trait     || *Extends*                 || *Defines* (methods in italics)  ||
//M|| Shape                ||                           || _draw_                          ||
//M
//M==Shape (trait)==
//M
//M{{{Shape}}} is the base type for all shapes.  Every class that extends
//Mit must implement the nullary method _draw_.  This method should create
//Man instance of the shape and add it to the canvas.
//M
//M_Not implemented yet: shapes should remember the colors and stroke style
//Mused and any transforms applied, and apply them again whenever _draw_ is
//Mcalled._
trait Shape extends core.VisualElement {
  val shapes: Seq[figure.FigShape]
  def hide() = shapes foreach (_.hide)
  def show() = shapes foreach (_.show)
  def setColor(color: Color) = shapes foreach (_.setColor(color))
  def draw: Shape
}
//M|| Rounded              ||                           || curvature, _radiusX_, _radiusY_ ||
//M
//M==Rounded (trait)==
//M
//M{{{Rounded}}} is a base type for shapes with rounded parts.  Every class
//Mthat extends it must have a value member, curvature, of type {{{Point}}}.
//MIt defines _radiusX_, _radiusY_ as access methods to the _x_ and _y_
//Mcomponents of curvature.
trait Rounded {
  val curvature: Point
  def radiusX = curvature.x
  def radiusY = curvature.y
}
//M|| !BaseShape           || Shape                     || origin, _toLine_                ||
//M
//M==!BaseShape (trait)==
//M
//M{{{BaseShape}}} is the base type for shapes that have a point of origin.
//MThis value member of type {{{Point}}} defines the lower left corner of
//Mthe shape bounds (except for {{{Elliptical}}} shapes, see below).
trait BaseShape extends Shape {
  val origin: Point
  def toLine(p: Point) = Line(origin, p)
}

//M|| !SimpleShape         || !BaseShape|| endpoint, _width_, _height_, _toLine_, _toRect_ ||
//M
//M==!SimpleShape (trait)==
//M
//M{{{SimpleShape}}} is the base type for shapes that are defined by two
//Mpoints, origin and endpoint (the upper right corner of the shape bounds).
//MThey have _width_ and _height_.  Every class that extends this trait must
//Mhave value members origin and endpoint, of type {{{Point}}}.
trait SimpleShape extends BaseShape {
  val endpoint: Point
  def width = endpoint.x - origin.x
  def height = endpoint.y - origin.y
  def toLine: Line = Line(origin, endpoint)
  def toRect: Rectangle = Rectangle(origin, endpoint)
  def toRect(p: Point): RoundRectangle = RoundRectangle(origin, endpoint, p)
}

//M|| Elliptical           || Rounded with !SimpleShape || `*`                             ||
//M`*`: {{{Elliptical}}} implements {{{curvature}}} and overrides {{{width}}} and {{{height}}}.
//M
//M==Elliptical (trait)==
//M
//M{{{Elliptical}}} is the base type for shapes that are rounded and whose
//Morigin value member defines their center.
trait Elliptical extends Rounded with SimpleShape {
  val curvature = endpoint - origin
  override def width = 2 * radiusX
  override def height = 2 * radiusY
}

//M|| *Dot*                || !BaseShape                ||                                 ||
//M
//M==Dot==
//M
//M{{{Dot}}} is drawn to the canvas as a dot of the stroke color.
class Dot(val origin: Point) extends BaseShape {
  val shapes = List(Impl.figure0.point(origin.x, origin.y))
  def draw = this

  override def toString = "Staging.Dot(" + origin + ")"
}
object Dot {
  def apply(p: Point) = {
    val shape = new Dot(p)
    shape.draw
  }
}

//M|| *Line*               || !SimpleShape              ||                                 ||
//M
//M==Line==
//M
//M{{{Line}}} is drawn to the canvas as a straight line of the stroke color
//Mfrom origin to endpoint.
class Line(val origin: Point, val endpoint: Point) extends SimpleShape {
  val shapes = List(Impl.figure0.line(origin, endpoint))
  def draw = this
  override def toString = "Staging.Line(" + origin + ", " + endpoint + ")"
}
object Line {
  def apply(p1: Point, p2: Point) = {
    val shape = new Line(p1, p2)
    shape.draw
  }
}

//M|| *Rectangle*          || !SimpleShape              ||                                 ||
//M
//M==Rectangle==
//M
//M{{{Rectangle}}} is drawn to the canvas as a rectangle of the fill and
//Mstroke color from origin to endpoint.
//M
//MThe width and height of the rectangle must both be positive, so if the
//Mdimensions are given in the form of a {{{Point}}} it must be to the right
//Mof and above origin.
class Rectangle(val origin: Point, val endpoint: Point) extends SimpleShape {
  // precondition endpoint > origin
  require(width > 0 && height > 0)
  val shapes = List(Impl.figure0.rectangle(origin, endpoint))
  def draw = this
  override def toString = "Staging.Rectangle(" + origin + ", " + endpoint + ")"
}
object Rectangle {
  def apply(p1: Point, p2: Point) = {
    val shape = new Rectangle(p1, p2)
    shape.draw
  }
}


//M|| !PolyShape           || Shape                   || points, _toPolyline_, _toPolygon_ ||
//M
//M==!PolyShape (trait)==
//M
//M{{{PolyShape}}} is the base type for shapes that are defined by several
//Mpoints.  The points are stored in a value member of type sequence of
//M{{{Point}}}s.
trait PolyShape extends Shape {
  val points: Seq[Point]
  def toPolygon: Polygon = Polygon(points)
  def toPolyline: Polyline = Polyline(points)
}

//M|| *!RoundRectangle*    || Rounded with !SimpleShape ||                                 ||
//M
//M==!RoundRectangle==
//M
//M{{{RoundRectangle}}} is drawn to the canvas as a rectangle with rounded
//Mcorners of the fill and stroke color from origin to endpoint.  The
//Mcurvature of the corners can be determined by x-radius and y-radius
//Mvalues or by a point value.
//M
//MThe width and height of the rectangle must both be positive, so if the
//Mdimensions are given in the form of a {{{Point}}} it must be to the right
//Mof and above origin.
class RoundRectangle(
  val origin: Point,
  val endpoint: Point,
  val curvature: Point
) extends Rounded with SimpleShape {
  // precondition endpoint > origin
  require(width > 0 && height > 0)
  val shapes = List(Impl.figure0.roundRectangle(origin, endpoint, radiusX, radiusY))
  def draw = this
  override def toString =
    "Staging.RoundRectangle(" + origin + ", " + endpoint + ", " + curvature + ")"
}
object RoundRectangle {
  def apply(p1: Point, p2: Point, p3: Point) = {
    val shape = new RoundRectangle(p1, p2, p3)
    shape.draw
  }
}

//M|| *Polyline*           || !PolyShape                ||                                 ||
//M
//M==Polyline==
//M
//M{{{Polyline}}} is drawn to the canvas as a segmented line connecting the
//Mgiven points by straight edges, using the fill and stroke color.
class Polyline(val points: Seq[Point]) extends PolyShape {
  val shapePath = new kgeom.PolyLine()
  points foreach { case Point(x, y) =>
      shapePath.addPoint(x, y)
  }
  val shapes = List(Impl.figure0.polyLine(shapePath))
  def draw = this

  override def toString = "Staging.Polyline(" + points + ")"
}
object Polyline {
  def apply(pts: Seq[Point]) = {
    val shape = new Polyline(pts)
    shape.draw
  }
}

//M|| *Polygon*            || !PolyShape                ||                                 ||
//M
//M==Polygon==
//M
//M{{{Polygon}}} is drawn to the canvas as a segmented line connecting the
//Mgiven points by straight edges, using the fill and stroke color.  The
//Mshape is closed, meaning that the last point connects to the first point.
class Polygon(val points: Seq[Point]) extends PolyShape {
  val shapePath = new kgeom.PolyLine()
  points foreach { case Point(x, y) =>
      shapePath.addPoint(x, y)
  }
  shapePath.polyLinePath.closePath
  val shapes = List(Impl.figure0.polyLine(shapePath))
  def draw = this

  override def toString = "Staging.Polygon(" + points + ")"
}
object Polygon {
  def apply(pts: Seq[Point]) = {
    val shape = new Polygon(pts)
    shape.draw
  }
}

//M|| *Ellipse*            || Elliptical                ||                                 ||
//M
//M==Ellipse==
//M
//M{{{Ellipse}}} is drawn to the canvas as an ellipse of the fill and stroke
//Mcolor centering on origin, with a curvature defined by the distance from
//Morigin to endpoint.
class Ellipse(val origin: Point, val endpoint: Point) extends Elliptical {
  val shapes = List(Impl.figure0.ellipse(origin, width, height))
  def draw = this

  override def toString = "Staging.Ellipse(" + origin + "," + endpoint + ")"
}
object Ellipse {
  def apply(p1: Point, p2: Point) = {
    val shape = new Ellipse(p1, p2)
    shape.draw
  }
}

//M|| *Arc*                || Elliptical                || start, extent                   ||
//M
//M==Arc==
//M
//M{{{Arc}}} is drawn to the canvas as an elliptical sector of the fill and
//Mstroke color centering on origin, with a curvature defined by the
//Mdistance from origin to endpoint.  The class defines two value members of
//Mtype {{{Double}}}: start is angle where the arc begins, and extent is the
//Mangle between start and end of the arc.  Both angles are given in degrees,
//Mwith 0 at "three o'clock", 90 at "twelve o'clock" and so on.
class Arc(
  val origin: Point, val endpoint: Point,
  val start: Double, val extent: Double
) extends Elliptical {
  val shapes = List(Impl.figure0.arc(origin.x, origin.y, width, height, start, extent))
  def draw = this

  override def toString = "Staging.Arc(" + origin + "," + endpoint + start + "," + extent + ")"
}
object Arc {
  def apply(p1: Point, p2: Point, s: Double, e: Double) = {
    val shape = new Arc(p1, p2, s, e)
    shape.draw
  }
}

//M|| *!LinesShape*        || !PolyShape                ||                                 ||
//M
//M==!LinesShape==
//M
//M{{{LinesShape}}} takes a sequence of {{{Point}}}s and connects them
//Mpairwise by straight lines of the stroke color.
class LinesShape(val points: Seq[Point]) extends PolyShape {
  val shapes = init

  def init = {
    val shapePath = new kgeom.PolyLine()
    shapePath.reset
    points grouped(2) foreach {
      case List() =>
      case Seq(Point(x1, y1), Point(x2, y2)) =>
        //println("points " + x1 + "," + y1 + " " + x2 + "," + y2)
        shapePath.polyLinePath.moveTo(x1, y1)
        shapePath.polyLinePath.lineTo(x2, y2)
      case p :: Nil =>
    }
    shapePath.updateBounds
    List(Impl.figure0.polyLine(shapePath))
  }

  def draw = this

  override def toString = "Staging.LinesShape(" + points + ")"
}
object LinesShape {
  def apply(pts: Seq[Point]) = {
    val shape = new LinesShape(pts)
    shape.draw
  }
}

//M|| *!TrianglesShape*    || !PolyShape                ||                                 ||
//M
//M==!TrianglesShape==
//M
//M{{{TrianglesShape}}} takes a sequence of {{{Point}}}s and connects them
//Mas triangles of the fill and stroke color.
class TrianglesShape(val points: Seq[Point]) extends PolyShape {
  val shapes = init

  def init = {
    val shapePath = new kgeom.PolyLine()
    shapePath.reset
    points grouped(3) foreach {
      case List() =>
      case Seq(Point(x0, y0), Point(x1, y1), Point(x2, y2)) =>
        shapePath.polyLinePath.moveTo(x0, y0)
        shapePath.polyLinePath.lineTo(x1, y1)
        shapePath.polyLinePath.lineTo(x2, y2)
        shapePath.polyLinePath.closePath
      case _ =>
    }
    shapePath.updateBounds
    List(Impl.figure0.polyLine(shapePath))
  }

  def draw = this

  override def toString = "Staging.TrianglesShape(" + points + ")"
}
object TrianglesShape {
  def apply(pts: Seq[Point]) = {
    val shape = new TrianglesShape(pts)
    shape.draw
  }
}

//M|| *!TriangleStripShape*|| !PolyShape                ||                                 ||
//M
//M==!TriangleStripShape==
//M
//M{{{TriangleStripShape}}} takes a sequence of {{{Point}}}s and connects
//Mthem as adjoining triangles of the fill and stroke color.
class TriangleStripShape(val points: Seq[Point]) extends PolyShape {
  val shapes = init

  def init = {
    val shapePath = new kgeom.PolyLine()
    shapePath.reset
    points sliding(3) foreach {
      case List() =>
      case Seq(Point(x0, y0), Point(x1, y1), Point(x2, y2)) =>
        shapePath.polyLinePath.moveTo(x0, y0)
        shapePath.polyLinePath.lineTo(x1, y1)
        shapePath.polyLinePath.lineTo(x2, y2)
        shapePath.polyLinePath.closePath
      case _ =>
    }
    shapePath.updateBounds
    List(Impl.figure0.polyLine(shapePath))
  }

  def draw = this

  override def toString = "Staging.TriangleStripShape(" + points + ")"
}
object TriangleStripShape {
  def apply(pts: Seq[Point]) = {
    val shape = new TriangleStripShape(pts)
    shape.draw
  }
}

//M|| *!QuadsShape*        || !PolyShape                ||                                 ||
//M
//M==!QuadsShape==
//M
//M{{{QuadsShape}}} takes a sequence of {{{Point}}}s and connects them as
//Mquads (polygons of four points) of the fill and stroke color.
class QuadsShape(val points: Seq[Point]) extends PolyShape {
  val shapes = init

  def init = {
    val shapePath = new kgeom.PolyLine()
    shapePath.reset
    points grouped(4) foreach {
      case List() =>
      case Seq(Point(x0, y0), Point(x1, y1), Point(x2, y2), Point(x3, y3)) =>
        shapePath.polyLinePath.moveTo(x0, y0)
        shapePath.polyLinePath.lineTo(x1, y1)
        shapePath.polyLinePath.lineTo(x2, y2)
        shapePath.polyLinePath.lineTo(x3, y3)
        shapePath.polyLinePath.closePath
      case _ =>
    }
    shapePath.updateBounds
    List(Impl.figure0.polyLine(shapePath))
  }

  def draw = this

  override def toString = "Staging.QuadsShape(" + points + ")"
}
object QuadsShape {
  def apply(pts: Seq[Point]) = {
    val shape = new QuadsShape(pts)
    shape.draw
  }
}

//M|| *!QuadStripShape*    || !PolyShape                ||                                 ||
//M
//M==!QuadStripShape==
//M
//M{{{QuadStripShape}}} takes a sequence of {{{Point}}}s and connects them
//Mas adjoining quads of the fill and stroke color.
class QuadStripShape(val points: Seq[Point]) extends PolyShape {
  val shapes = init

  def init = {
    val shapePath = new kgeom.PolyLine()
    shapePath.reset
    points sliding(4, 2) foreach {
      case List() =>
      case Seq(Point(x0, y0), Point(x1, y1), Point(x2, y2), Point(x3, y3)) =>
        shapePath.polyLinePath.moveTo(x0, y0)
        shapePath.polyLinePath.lineTo(x1, y1)
        shapePath.polyLinePath.lineTo(x2, y2)
        shapePath.polyLinePath.lineTo(x3, y3)
        shapePath.polyLinePath.closePath
      case _ =>
    }
    shapePath.updateBounds
    List(Impl.figure0.polyLine(shapePath))
  }

  def draw = this

  override def toString = "Staging.QuadStripShape(" + points + ")"
}
object QuadStripShape {
  def apply(pts: Seq[Point]) = {
    val shape = new QuadStripShape(pts)
    shape.draw
  }
}

//M|| *!TriangleFanShape*  || !PolyShape with !BaseShape||                                 ||
//M
//M==!TriangleFanShape==
//M
//M{{{TriangleFanShape}}} takes a center point (origin) and a sequence of
//M{{{Point}}}s, and connects the points pairwise with each other and with
//Mthe center point with straight edges of the fill and stroke color.
class TriangleFanShape(val origin: Point, val points: Seq[Point]) extends PolyShape
                                                                     with BaseShape {
  val shapes = init

  def init = {
    val shapePath = new kgeom.PolyLine()
    shapePath.reset
    points grouped(2) foreach {
      case List() =>
      case Seq(Point(x1, y1), Point(x2, y2)) =>
        shapePath.polyLinePath.moveTo(origin.x, origin.y)
        shapePath.polyLinePath.lineTo(x1, y1)
        shapePath.polyLinePath.lineTo(x2, y2)
      case _ =>
    }
    shapePath.updateBounds
    List(Impl.figure0.polyLine(shapePath))
  }

  def draw = this

  override def toString = "Staging.QuadStripShape(" + origin + "," + points + ")"
}
object TriangleFanShape {
  def apply(p0: Point, pts: Seq[Point]) = {
    val shape = new TriangleFanShape(p0, pts)
    shape.draw
  }
}

//M|| *!SvgShape*  || Shape || node                            ||
//M
//M==!SvgShape==
//M
//M{{{SvgShape}}} takes a SVG element (rect, circle, ellipse, line, polyline,
//Mpolygon, or path) and draws it as a shape of the fill and stroke color.
//M
//MTODO: Should handle g and svg elements in the future.
abstract class SvgShape(val node: scala.xml.Node) extends Shape {
}
object SvgShape {
  def getAttr (ns: scala.xml.Node, s: String): Option[String] = {
    ns \ ("@" + s) text match {
      case "" => None
      case z  => Some(z)
    }
  }

  private def matchXY (ns: scala.xml.Node, xn: String = "x", yn: String = "y") = {
    val x = (getAttr(ns, xn) getOrElse "0").toDouble
    val y = (getAttr(ns, yn) getOrElse "0").toDouble
    Point(x, y)
  }

  private def matchWH (ns: scala.xml.Node) = {
    val w = (getAttr(ns, "width") getOrElse "0").toDouble
    val h = (getAttr(ns, "height") getOrElse "0").toDouble
    require(w >= 0, "Bad width for XML element " + ns)
    require(h >= 0, "Bad height for XML element " + ns)
    (w, h)
  }

  private def matchRXY (ns: scala.xml.Node) = {
    val x = (getAttr(ns, "rx") getOrElse "0").toDouble
    val y = (getAttr(ns, "ry") getOrElse "0").toDouble
    require(x >= 0, "Bad rx for XML element " + ns)
    require(y >= 0, "Bad ry for XML element " + ns)
    val rx = if (x != 0) x else y
    val ry = if (y != 0) y else x
    Point(rx, ry)
  }

  private def matchFillStroke (ns: scala.xml.Node) = {
    //TODO
    (getAttr(ns, "fill"), getAttr(ns, "stroke"))
  }

  private def matchPoints (ns: scala.xml.Node): Seq[Point] = {
    val pointsStr = ns \ "@points" text
    val splitter = "(:?,\\s*|\\s+)".r
    val pointsItr = (splitter split pointsStr) map (_.toDouble) grouped(2)
    (pointsItr map { a => Point(a(0), a(1)) }).toList
  }

  private def matchRect(ns: scala.xml.Node) = {
    val p0 = matchXY(ns)
    val (width, height) = matchWH(ns)
    val p1 = p0 + Point(width, height)
    val p2 = matchRXY(ns)
    if (p2.x != 0. || p2.y != 0.) {
      RoundRectangle(p0, p1, p2)
    } else {
      Rectangle(p0, p1)
    }
  }

  private def matchCircle(ns: scala.xml.Node) = {
    val p0 = matchXY(ns, "cx", "cy")
    val r = (getAttr(ns, "r") getOrElse "0").toDouble
    val p1 = p0 + Point(r, r)
    Ellipse(p0, p1)
  }

  private def matchEllipse(ns: scala.xml.Node) = {
    val p0 = matchXY(ns, "cx", "cy")
    val p1 = p0 + matchRXY(ns)
    Ellipse(p0, p1)
  }

  private def matchLine(ns: scala.xml.Node) = {
    val p1 = matchXY(ns, "x1", "y1")
    val p2 = matchXY(ns, "x2", "y2")
    Line(p1, p2)
  }

  private def matchPath(ns: scala.xml.Node) = {
    val d = (ns \ "@d" text)
    new Shape {
      val shapes = List(Impl.figure0.path(d))
      def draw = this
    }
  }

  def apply(node: scala.xml.Node): Shape = {
    // should handle some of
    //   color, fill-rule, stroke, stroke-dasharray, stroke-dashoffset,
    //   stroke-linecap, stroke-linejoin, stroke-miterlimit, stroke-width,
    //   color-interpolation, color-rendering
    // and
    //   transform-list
  node match {
      case <rect></rect> =>
        matchRect(node)
      case <circle></circle> =>
        matchCircle(node)
      case <ellipse></ellipse> =>
        matchEllipse(node)
      case <line></line> =>
        matchLine(node)
      case <polyline></polyline> =>
        Polyline(matchPoints(node))
      case <polygon></polygon> =>
        Polygon(matchPoints(node))
      case <path></path> =>
        matchPath(node)
      case <g>{ shapes @ _* }</g> =>
        new Shape {
          val shapes = Nil
          def draw = this
        }
        //for (s <- shapes) yield SvgShape(s)
      case <svg>{ shapes @ _* }</svg> =>
        new Shape {
          val shapes = Nil
          def draw = this
        }
        //for (s <- shapes) yield SvgShape(s)
      case _ => // unknown element, ignore
        new Shape {
          val shapes = Nil
          def draw = this
        }
  }
//    val shape = new SvgShape(node)
//    shape.draw
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
    low2 + range2 * (value - low1) / range1
  }

  def lerp(value1: Double, value2: Double, amt: Double) = {
    require(amt >= 0d && amt <= 1d)
    val range: Double = value2 - value1
    value1 + amt * range
  }
}
