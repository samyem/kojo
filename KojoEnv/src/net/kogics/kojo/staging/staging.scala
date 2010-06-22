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
import edu.umd.cs.piccolo.nodes.PPath
import edu.umd.cs.piccolo.util.PBounds
import edu.umd.cs.piccolo.activities.PActivity

import net.kogics.kojo.util.Utils
import net.kogics.kojo.core.Point
import java.awt.Color
import math._

object Impl {
  val figure0 = SpriteCanvas.instance.figure0
  val canvas = SpriteCanvas.instance
}

/** Staging API
  *
  * This object contains the API for using Staging within Kojo scripts.
  *
  * DISCLAIMER
  *
  * Parts of this interface is written to approximately conform to the
  * Processing API as described in the reference at
  * <URL: http://processing.org/reference/>.
  * The implementation code is the work of Peter Lewerin
  * (<peter.lewerin@tele2.se>) and is not in any way derived from the
  * Processing source. */
object API {
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
  //W=Examples=
  //W
  //W  * StagingHelloKojoExample
  //W  * StagingArrayExample
  //W  * StagingArrayTwoDeeExample
  //W  * StagingColorWheelExample
  //W  * StagingCreatingColorsExample
  //W  * StagingHueSaturationBrightnessExample
  //W  * StagingSineOfAnAngleExample
  //W
  //W=Overview=
  //W
  //W==Points==
  //W
  //WStaging uses {{{net.kogics.kojo.core.Point}}} for coordinates.
  //W

  def point(x: Double, y: Double) = Point(x, y)

  implicit def tupleDToPoint(tuple: (Double, Double)) = Point(tuple._1, tuple._2)
  implicit def tupleIToPoint(tuple: (Int, Int)) = Point(tuple._1, tuple._2)
  implicit def baseShapeToPoint(b: BaseShape) = b.origin
  implicit def awtPointToPoint(p: java.awt.geom.Point2D) = Point(p.getX, p.getY)
  implicit def awtDimToPoint(d: java.awt.geom.Dimension2D) = Point(d.getWidth, d.getHeight)

  /** The point of origin, located at a corner of the user screen if
    * `screenSize` has been called, or the middle of the screen otherwise. */
  val O = Point(0, 0)

  //W
  //W==User Screen==
  //W
  //WThe zoom level and axis orientations can be set using `screenSize`.
  //W
  def screenWidth = Screen.rect.width.toInt
  def screenHeight = Screen.rect.height.toInt
  def screenSize(width: Int, height: Int) = Screen.size(width, height)

  /** The middle point of the user screen, or (0, 0) if `screenSize` hasn't
    * been called. */
  def screenMid: Point = Screen.midpoint

  /** The extreme point of the user screen (i.e. the opposite corner from
    * the point of origin), or (0, 0) if `screenSize` hasn't been called. */
  def screenExt: Point = Screen.extpoint

  /** Fills the user screen with the specified color. */
  def background(bc: Color) = {
    withStyle(bc, null, 1) { rectangle(O, screenExt) }
  }
  
  //W
  //W==Simple shapes and text==
  //W
  //WGiven `Point`s or _x_ and _y_ coordinate values, simple shapes like dots,
  //Wlines, rectangles, ellipses, and elliptic arcs can be drawn.  Texts can
  //Walso be placed in this way.
  //W
  def dot(x: Double, y: Double) = Dot(Point(x, y))
  def dot(p: Point) = Dot(p)

  def line(x1: Double, y1: Double, x2: Double, y2: Double) =
    Line(Point(x1, y1), Point(x2, y2))
  def line(p1: Point, p2: Point) =
    Line(p1, p2)

  def vector(x1: Double, y1: Double, x2: Double, y2: Double, a: Double) =
    Vector(Point(x1, y1), Point(x2, y2), a)
  def vector(p1: Point, p2: Point, a: Double) =
    Vector(p1, p2, a)

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

  def ellipse(cx: Double, cy: Double, rx: Double, ry: Double) =
    Ellipse(Point(cx, cy), Point(cx + rx, cy + ry))
  def ellipse(p: Point, rx: Double, ry: Double) =
    Ellipse(p, Point(p.x + rx, p.y + ry))
  def ellipse(p1: Point, p2: Point) =
    Ellipse(p1, p2)
  def circle(x: Double, y: Double, r: Double) =
    Ellipse(Point(x, y), Point(x + r, y + r))
  def circle(p: Point, r: Double) =
    Ellipse(p, Point(p.x + r, p.y + r))

  def arc(cx: Double, cy: Double, rx: Double, ry: Double, s: Double, e: Double) =
    Arc(Point(cx, cy), Point(cx + rx, cy + ry), s, e, java.awt.geom.Arc2D.PIE)
  def arc(p: Point, rx: Double, ry: Double, s: Double, e: Double) =
    Arc(p, Point(p.x + rx, p.y + ry), s, e, java.awt.geom.Arc2D.PIE)
  def arc(p1: Point, p2: Point, s: Double, e: Double) =
    Arc(p1, p2, s, e, java.awt.geom.Arc2D.PIE)
  def pieslice(cx: Double, cy: Double, rx: Double, ry: Double, s: Double, e: Double) =
    Arc(Point(cx, cy), Point(cx + rx, cy + ry), s, e, java.awt.geom.Arc2D.PIE)
  def pieslice(p: Point, rx: Double, ry: Double, s: Double, e: Double) =
    Arc(p, Point(p.x + rx, p.y + ry), s, e, java.awt.geom.Arc2D.PIE)
  def pieslice(p1: Point, p2: Point, s: Double, e: Double) =
    Arc(p1, p2, s, e, java.awt.geom.Arc2D.PIE)
  def openArc(cx: Double, cy: Double, rx: Double, ry: Double, s: Double, e: Double) =
    Arc(Point(cx, cy), Point(cx + rx, cy + ry), s, e, java.awt.geom.Arc2D.OPEN)
  def openArc(p: Point, rx: Double, ry: Double, s: Double, e: Double) =
    Arc(p, Point(p.x + rx, p.y + ry), s, e, java.awt.geom.Arc2D.OPEN)
  def openArc(p1: Point, p2: Point, s: Double, e: Double) =
    Arc(p1, p2, s, e, java.awt.geom.Arc2D.OPEN)
  def chord(cx: Double, cy: Double, rx: Double, ry: Double, s: Double, e: Double) =
    Arc(Point(cx, cy), Point(cx + rx, cy + ry), s, e, java.awt.geom.Arc2D.CHORD)
  def chord(p: Point, rx: Double, ry: Double, s: Double, e: Double) =
    Arc(p, Point(p.x + rx, p.y + ry), s, e, java.awt.geom.Arc2D.CHORD)
  def chord(p1: Point, p2: Point, s: Double, e: Double) =
    Arc(p1, p2, s, e, java.awt.geom.Arc2D.CHORD)

  def text(s: String, x: Double, y: Double) = Text(s, Point(x, y))
  def text(s: String, p: Point) = Text(s, p)

  def star(cx: Double, cy: Double, inner: Double, outer: Double, points: Int) =
    Star(Point(cx, cy), inner, outer, points)
  def star(p: Point, inner: Double, outer: Double, points: Int) =
    Star(p, inner, outer, points)
  def star(p1: Point, p2: Point, p3: Point, points: Int) =
    Star(p1, dist(p1, p2), dist(p1, p3), points)

  //W
  //W==Complex Shapes==
  //W
  //WGiven a sequence of `Point`s, a number of complex shapes can be drawn,
  //Wincluding basic polylines and polygons, and patterns of polylines/polygons.
  //W
  def polyline(pts: Seq[Point]) = Polyline(pts)

  def polygon(pts: Seq[Point]): Polygon = Polygon(pts)
  def triangle(p0: Point, p1: Point, p2: Point) = polygon(Seq(p0, p1, p2))
  def quad(p0: Point, p1: Point, p2: Point, p3: Point) =
    polygon(Seq(p0, p1, p2, p3))

  def linesShape(pts: Seq[Point]) = LinesShape(pts)

  def trianglesShape(pts: Seq[Point]) = TrianglesShape(pts)

  def triangleStripShape(pts: Seq[Point]) = TriangleStripShape(pts)

  def quadsShape(pts: Seq[Point]) = QuadsShape(pts)

  def quadStripShape(pts: Seq[Point]) = QuadStripShape(pts)

  def triangleFanShape(p0: Point, pts: Seq[Point]) = TriangleFanShape(p0, pts)

  //W
  //W==SVG Shapes==
  //W
  //WGiven an SVG element, the corresponding shape can be drawn.
  //W
  def svgShape(node: scala.xml.Node) = SvgShape(node)

  //W
  //W==Color==
  //W
  //WColor values can be created with the method `color`, and the way color
  //Wis specified can be set with `colorMode`.  The methods `fill`, `noFill`,
  //W`stroke`, and `noStroke` set the colors used to draw the insides and edges
  //Wof figures.  The method `strokeWidth` doesn't actually affect color but is
  //Wtypically used together with the color setting methods.  The method
  //W`withStyle` allows the user to set fill color, stroke color, and stroke
  //Wwidth temporarily.
  //W
  //W
  abstract class ColorModes
  case class RGB(r: Int, g: Int, b: Int) extends ColorModes
  case class RGBA(r: Int, g: Int, b: Int, a: Int) extends ColorModes
  case class HSB(h: Int, s: Int, b: Int) extends ColorModes
  case class HSBA(h: Int, s: Int, b: Int, a: Int) extends ColorModes
  case class GRAY(v: Int) extends ColorModes
  case class GRAYA(v: Int, a: Int) extends ColorModes
  def colorMode(mode: ColorModes) = ColorMode(mode)
  def color(v: Int) = ColorMode.color(v)
  def color(v: Int, a: Int) = ColorMode.color(v, a)
  def color(v: Double) = ColorMode.color(v)
  def color(v: Double, a: Double) = ColorMode.color(v, a)
  def color(v1: Int, v2: Int, v3: Int) = ColorMode.color(v1, v2, v3)
  def color(v1: Int, v2: Int, v3: Int, a: Int) = ColorMode.color(v1, v2, v3, a)
  def color(v1: Double, v2: Double, v3: Double) = ColorMode.color(v1, v2, v3)
  def color(v1: Double, v2: Double, v3: Double, a: Double) = ColorMode.color(v1, v2, v3, a)
  def color(s: String) = ColorMode.color(s)
  def fill(c: Color) = Impl.figure0.setFillColor(c)
  def noFill = Impl.figure0.setFillColor(null)
  def stroke(c: Color) = Impl.figure0.setPenColor(c)
  def noStroke = Impl.figure0.setPenColor(null)
  def strokeWidth(w: Double) = Impl.figure0.setPenThickness(w)
  def withStyle(fc: Color, sc: Color, sw: Double)(body: => Unit) =
    Style(fc, sc, sw)(body)
  def saveStyle = Style.save
  def restoreStyle = Style.restore
  implicit def ColorToRichColor (c: java.awt.Color) = RichColor(c)

  colorMode(RGB(255, 255, 255))
  Inputs.init()

  //W
  //W==Timekeeping==
  //W
  //WA number of methods report the current time.
  //W
  //W
  //W{{{
  //Wmillis // milliseconds
  def millis = System.currentTimeMillis()

  import java.util.Calendar

  //Wsecond // second of the minute
  def second = Calendar.getInstance().get(Calendar.SECOND)

  //Wminute // minute of the hour
  def minute = Calendar.getInstance().get(Calendar.MINUTE)

  //Whour   // hour of the day
  def hour   = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

  //Wday    // day of the month
  def day    = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

  //Wmonth  // month of the year (1..12)
  def month  = Calendar.getInstance().get(Calendar.MONTH) + 1

  //Wyear   // year C.E.
  def year   = Calendar.getInstance().get(Calendar.YEAR)
  //W}}}

  //W
  //W==Math==
  //W
  //WA number of methods perform number processing tasks.
  //W
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
  //W==Trignometry==
  //W
  //WA number of methods perform trignometry tasks.
  //W
  val Pi = math.Pi
  def sin(a: Double) = math.sin(a)
  def cos(a: Double) = math.cos(a)
  def radians(deg: Double) = deg * Pi / 180


  def loop(fn: => Unit) = Impl.figure0.refresh(fn)
  def stop = Impl.figure0.stopRefresh()
  def clear() = Impl.figure0.clear()
  def fgClear() = Impl.figure0.fgClear()

  def mouseX() = Inputs.stepMousePos.x
  def mouseY() = Inputs.stepMousePos.y
  def pmouseX() = Inputs.prevMousePos.x
  def pmouseY() = Inputs.prevMousePos.y
  val LEFT = 1
  val CENTER = 2
  val RIGHT = 3
  def mouseButton = Inputs.mouseBtn
  def mousePressed = Inputs.mousePressedFlag

  //W
  //W=Usage=
  //W
} // end of API


object Point {
  def apply(x: Double, y: Double) = new Point(x, y)
  def unapply(p: Point) = Some((p.x, p.y))
}

object Screen {
  var rect = new PBounds(0, 0, 0, 0)

  def size(width: Int, height: Int) = {
    // TODO 560 is a value that works on my system, should be less ad-hoc
    val factor = 560
    val xfactor = factor / (if (width < 0) -(height.abs) else height.abs) // sic!
    val yfactor = factor / height
    Impl.canvas.zoomXY(xfactor, yfactor, width / 2, height / 2)
    rect.setRect(0, 0, width.abs, height.abs)
    (rect.width.toInt, rect.height.toInt)
  }

  def midpoint = rect.getCenter2D
  def extpoint = rect.getSize
}

trait Shape {
  def node: PNode
  def hide() = node.setVisible(false)
  def show() = node.setVisible(true)
  def fill_=(color: Color) { node.setPaint(color) }
  def fill = node.getPaint
  def rotate(amount: Double) = node.rotate(amount)
  def scale(amount: Double) = node.scale(amount)
  def offset_=(p: Point) = node.setOffset(p.x, p.y)
  def offset = { val o = node.getOffset ; Point(o.getX, o.getY) }
  //def addActivity(a: PActivity) = Impl.canvas.getRoot.addActivity(a)
}

trait Rounded {
  val curvature: Point
  def radiusX = curvature.x
  def radiusY = curvature.y
}

trait BaseShape extends Shape {
  val origin: Point
  def toLine(p: Point) = Line(origin, p)
}

trait StrokedShape extends BaseShape {
  val path: PPath
  def node = path
  var style: java.awt.BasicStroke = Impl.figure0.DefaultStroke
  def stroke_=(color: Color) = path.setStrokePaint(color)
  def stroke = path.getStrokePaint
  def strokeWidth_=(width: Double) {
    this style =
      new java.awt.BasicStroke(width.toFloat, style.getEndCap, style.getLineJoin)
  }
  def strokeWidth = this.strokeStyle.getLineWidth.toDouble
  def strokeStyle_=(style: java.awt.BasicStroke) { this.style = style }
  def strokeStyle = style

  def setPalette {
    Utils.runInSwingThread {
      fill = Impl.figure0.fillColor
      strokeStyle = Impl.figure0.lineStroke.asInstanceOf[java.awt.BasicStroke]
      stroke = Impl.figure0.lineColor
    }
  }
}

trait SimpleShape extends StrokedShape {
  val endpoint: Point
  def width = endpoint.x - origin.x
  def height = endpoint.y - origin.y
  def toLine: Line = Line(origin, endpoint)
  def toRect: Rectangle = Rectangle(origin, endpoint)
  def toRect(p: Point): RoundRectangle = RoundRectangle(origin, endpoint, p)
}

trait Elliptical extends SimpleShape with Rounded {
  val curvature = endpoint - origin
  override def width = 2 * radiusX
  override def height = 2 * radiusY
}

class Dot(val origin: Point) extends StrokedShape {
  val path = PPath.createLine(
    origin.x.toFloat, origin.y.toFloat,
    origin.x.toFloat, origin.y.toFloat
  )

  override def toString = "Staging.Dot(" + origin + ")"
}
object Dot {
  def apply(p: Point) = {
    val shape = new Dot(p)
    Impl.figure0.pnode(shape.node)
    shape
  }
}

class Text(val text: String, val origin: Point) extends BaseShape {
  import java.awt.Font
  val tnode = new edu.umd.cs.piccolo.nodes.PText(text)
  def node = tnode

  tnode.getTransformReference(true).setToScale(1, -1)
  tnode.setOffset(origin.x, origin.y)
  val font = new Font(tnode.getFont.getName, Font.PLAIN, 14)
  tnode.setFont(font)

  override def toString = "Staging.Text(" + text + ", " + origin + ")"
}
object Text {
  def apply(s: String, p: Point) = {
    val shape = new Text(s, p)
    Impl.figure0.pnode(shape.node)
    shape
  }
}

class Line(val origin: Point, val endpoint: Point) extends SimpleShape {
  val path =
    PPath.createLine(origin.x.toFloat, origin.y.toFloat, endpoint.x.toFloat, endpoint.y.toFloat)
  
  override def toString = "Staging.Line(" + origin + ", " + endpoint + ")"
}
object Line {
  def apply(p1: Point, p2: Point) = {
    val shape = new Line(p1, p2)
    Impl.figure0.pnode(shape.node)
    shape
  }
}

class Rectangle(val origin: Point, val endpoint: Point) extends SimpleShape {
  // precondition endpoint > origin
  require(width > 0 && height > 0)
  val path =
    PPath.createRectangle(origin.x.toFloat, origin.y.toFloat, width.toFloat, height.toFloat)

  override def toString = "Staging.Rectangle(" + origin + ", " + endpoint + ")"
}
object Rectangle {
  def apply(p1: Point, p2: Point) = {
    val shape = new Rectangle(p1, p2)
    Impl.figure0.pnode(shape.node)
    shape
  }
}


trait PolyShape extends BaseShape {
  val points: Seq[Point]
  val origin = points(0)
  def toPolygon: Polygon = Polygon(points)
  def toPolyline: Polyline = Polyline(points)
}

class RoundRectangle(
  val origin: Point,
  val endpoint: Point,
  val curvature: Point
) extends SimpleShape with Rounded {
  // precondition endpoint > origin
  require(width > 0 && height > 0)
  val path =
    PPath.createRoundRectangle(
      origin.x.toFloat, origin.y.toFloat,
      width.toFloat, height.toFloat,
      curvature.x.toFloat, curvature.y.toFloat
    )

  override def toString =
    "Staging.RoundRectangle(" + origin + ", " + endpoint + ", " + curvature + ")"
}
object RoundRectangle {
  def apply(p1: Point, p2: Point, p3: Point) = {
    val shape = new RoundRectangle(p1, p2, p3)
    Impl.figure0.pnode(shape.node)
    shape
  }
}

class Polyline(val points: Seq[Point]) extends PolyShape with StrokedShape {
  val path = PPath.createPolyline((points map {
        case Point(x, y) => new java.awt.geom.Point2D.Double(x, y)
      }).toArray)

  override def toString = "Staging.Polyline(" + points + ")"
}
object Polyline {
  def apply(pts: Seq[Point]) = {
    val shape = new Polyline(pts)
    Impl.figure0.pnode(shape.node)
    shape
  }
}

class Polygon(val points: Seq[Point]) extends PolyShape with StrokedShape {
  val path = PPath.createPolyline((points map {
        case Point(x, y) => new java.awt.geom.Point2D.Double(x, y)
      }).toArray)
  path.closePath

  override def toString = "Staging.Polygon(" + points + ")"
}
object Polygon {
  def apply(pts: Seq[Point]) = {
    val shape = new Polygon(pts)
    Impl.figure0.pnode(shape.node)
    shape
  }
}

class Ellipse(val origin: Point, val endpoint: Point) extends Elliptical {
  val path = PPath.createEllipse(
    (origin.x - radiusX).toFloat, (origin.y - radiusY).toFloat,
    width.toFloat, height.toFloat
  )

  override def toString = "Staging.Ellipse(" + origin + "," + endpoint + ")"
}
object Ellipse {
  def apply(p1: Point, p2: Point) = {
    val shape = new Ellipse(p1, p2)
    Impl.figure0.pnode(shape.node)
    shape
  }
}

class Arc(
  val origin: Point, val endpoint: Point,
  val start: Double, val extent: Double,
  val kind: Int
) extends Elliptical {
  val path = new PPath
  path.setPathTo(new java.awt.geom.Arc2D.Double(
    (origin.x - radiusX), (origin.y - radiusY), width, height,
    -start, -extent, kind
  ))

  override def toString = "Staging.Arc(" + origin + "," + endpoint + start + "," + extent + ")"
}
object Arc {
  def apply(p1: Point, p2: Point, s: Double, e: Double, k: Int) = {
    val shape = new Arc(p1, p2, s, e, k)
    Impl.figure0.pnode(shape.node)
    shape
  }
}

object Star {
  def apply(origin: Point, inner: Double, outer: Double, points: Int) = {
    val a = math.Pi / points // the angle between outer and inner point
    val pts = Seq.tabulate(2 * points){ i =>
      val aa = math.Pi / 2 + a * i
      if (i % 2 == 0) { origin + Point(outer * cos(aa), outer * sin(aa)) }
      else { origin + Point(inner * cos(aa), inner * sin(aa)) }
    }
    Polygon(pts)
  }
}

class Vector(val origin: Point, val endpoint: Point, val length: Double) extends SimpleShape {
  val path = new PPath

  val vlength = API.dist(origin, endpoint)
  val arrowHalfWidth = length / 3

  def init = {
    path.moveTo(origin.x.toFloat, origin.y.toFloat)
    val (x, y) = ((origin.x + vlength).toFloat, origin.y.toFloat)
    path.lineTo(x, y)
    path.moveTo(x, y)
    path.lineTo(x - length.toFloat, y - arrowHalfWidth.toFloat)
    path.lineTo(x - length.toFloat, y + arrowHalfWidth.toFloat)
    path.closePath
  }

  init

  val angle =
    if (origin.x < endpoint.x) { math.asin((endpoint.y - origin.y) / vlength) }
    else { math.Pi - math.asin((endpoint.y - origin.y) / vlength) }

  node.rotateAboutPoint(angle, origin.x, origin.y)

  override def toString = "Staging.Vector(" + origin + ", " + endpoint + ")"
}
object Vector {
  def apply(p1: Point, p2: Point, length: Double) = {
    val shape = new Vector(p1, p2, length)
    Impl.figure0.pnode(shape.node)
    shape
  }
}

class LinesShape(val points: Seq[Point]) extends PolyShape with StrokedShape {
  val path = new PPath

  def init = {
    points grouped(2) foreach {
      case List() =>
      case Seq(Point(x1, y1), Point(x2, y2)) =>
        path.moveTo(x1.toFloat, y1.toFloat)
        path.lineTo(x2.toFloat, y2.toFloat)
      case p :: Nil =>
    }
  }

  init

  override def toString = "Staging.LinesShape(" + points + ")"
}
object LinesShape {
  def apply(pts: Seq[Point]) = {
    val shape = new LinesShape(pts)
    Impl.figure0.pnode(shape.node)
    shape
  }
}

class TrianglesShape(val points: Seq[Point]) extends PolyShape with StrokedShape {
  val path = new PPath

  def init = {
    points grouped(3) foreach {
      case List() =>
      case Seq(Point(x0, y0), Point(x1, y1), Point(x2, y2)) =>
        path.moveTo(x0.toFloat, y0.toFloat)
        path.lineTo(x1.toFloat, y1.toFloat)
        path.lineTo(x2.toFloat, y2.toFloat)
        path.closePath
      case _ =>
    }
  }

  init

  override def toString = "Staging.TrianglesShape(" + points + ")"
}
object TrianglesShape {
  def apply(pts: Seq[Point]) = {
    val shape = new TrianglesShape(pts)
    Impl.figure0.pnode(shape.node)
    shape
  }
}

class TriangleStripShape(val points: Seq[Point]) extends PolyShape with StrokedShape {
  val path = new PPath

  def init = {
    points sliding(3) foreach {
      case List() =>
      case Seq(Point(x0, y0), Point(x1, y1), Point(x2, y2)) =>
        path.moveTo(x0.toFloat, y0.toFloat)
        path.lineTo(x1.toFloat, y1.toFloat)
        path.lineTo(x2.toFloat, y2.toFloat)
        path.closePath
      case _ =>
    }
  }

  init

  override def toString = "Staging.TriangleStripShape(" + points + ")"
}
object TriangleStripShape {
  def apply(pts: Seq[Point]) = {
    val shape = new TriangleStripShape(pts)
    Impl.figure0.pnode(shape.node)
    shape
  }
}

class QuadsShape(val points: Seq[Point]) extends PolyShape with StrokedShape {
  val path = new PPath

  def init = {
    points grouped(4) foreach {
      case List() =>
      case Seq(Point(x0, y0), Point(x1, y1), Point(x2, y2), Point(x3, y3)) =>
        path.moveTo(x0.toFloat, y0.toFloat)
        path.lineTo(x1.toFloat, y1.toFloat)
        path.lineTo(x2.toFloat, y2.toFloat)
        path.lineTo(x3.toFloat, y3.toFloat)
        path.closePath
      case _ =>
    }
  }

  init

  override def toString = "Staging.QuadsShape(" + points + ")"
}
object QuadsShape {
  def apply(pts: Seq[Point]) = {
    val shape = new QuadsShape(pts)
    Impl.figure0.pnode(shape.node)
    shape
  }
}

class QuadStripShape(val points: Seq[Point]) extends PolyShape with StrokedShape {
  val path = new PPath

  def init = {
    points sliding(4, 2) foreach {
      case List() =>
      case Seq(Point(x0, y0), Point(x1, y1), Point(x2, y2), Point(x3, y3)) =>
        path.moveTo(x0.toFloat, y0.toFloat)
        path.lineTo(x1.toFloat, y1.toFloat)
        path.lineTo(x2.toFloat, y2.toFloat)
        path.lineTo(x3.toFloat, y3.toFloat)
        path.closePath
      case _ =>
    }
  }

  init

  override def toString = "Staging.QuadStripShape(" + points + ")"
}
object QuadStripShape {
  def apply(pts: Seq[Point]) = {
    val shape = new QuadStripShape(pts)
    Impl.figure0.pnode(shape.node)
    shape
  }
}

class TriangleFanShape(override val origin: Point, val points: Seq[Point]) extends PolyShape
                                                                     with StrokedShape {
  val path = new PPath

  def init = {
    points grouped(2) foreach {
      case List() =>
      case Seq(Point(x1, y1), Point(x2, y2)) =>
        path.moveTo(origin.x.toFloat, origin.y.toFloat)
        path.lineTo(x1.toFloat, y1.toFloat)
        path.lineTo(x2.toFloat, y2.toFloat)
      case _ =>
    }
  }

  init

  override def toString = "Staging.QuadStripShape(" + origin + "," + points + ")"
}
object TriangleFanShape {
  def apply(p0: Point, pts: Seq[Point]) = {
    val shape = new TriangleFanShape(p0, pts)
    Impl.figure0.pnode(shape.node)
    shape
  }
}

class Composite(val shapes: Seq[Shape]) extends Shape {
  val node = new PNode
  shapes foreach { shape => node.addChild(shape.node) }

  override def toString = "Staging.Group(" + shapes.mkString(",") + ")"
}
object Composite {
  def apply(shapes: Seq[Shape]) = {
    new Composite(shapes)
  }
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

  private def matchFill(ns: scala.xml.Node) = getAttr(ns, "fill")

  private def matchStroke(ns: scala.xml.Node) = getAttr(ns, "stroke")

  private def matchStrokeWidth(ns: scala.xml.Node) = getAttr(ns, "stroke-width")

  private def matchPoints (ns: scala.xml.Node): Seq[Point] = {
    val pointsStr = ns \ "@points" text
    val splitter = "(:?,\\s*|\\s+)".r
    val pointsItr = (splitter split pointsStr) map (_.toDouble) grouped(2)
    (pointsItr map { a => Point(a(0), a(1)) }).toList
  }

  def setStyle(ns: scala.xml.Node) {
    val fc_? = matchFill(ns)
    val sc_? = matchStroke(ns)
    val sw_? = matchStrokeWidth(ns)
    API.saveStyle
    fc_? foreach { fc => API.fill(API.color(fc)) }
    sc_? foreach { sc => API.stroke(API.color(sc)) }
    sw_? foreach { sw => API.strokeWidth(sw.toDouble) }
  }

  private def matchRect(ns: scala.xml.Node) = {
    val p0 = matchXY(ns)
    val (width, height) = matchWH(ns)
    val p1 = p0 + Point(width, height)
    val p2 = matchRXY(ns)
    setStyle(ns)
    val res =
      if (p2.x != 0. || p2.y != 0.) {
        RoundRectangle(p0, p1, p2)
      } else {
        Rectangle(p0, p1)
      }
    API.restoreStyle
    res
  }

  private def matchCircle(ns: scala.xml.Node) = {
    val p0 = matchXY(ns, "cx", "cy")
    val r = (getAttr(ns, "r") getOrElse "0").toDouble
    val p1 = p0 + Point(r, r)
    setStyle(ns)
    val res = Ellipse(p0, p1)
    API.restoreStyle
    res
  }

  private def matchEllipse(ns: scala.xml.Node) = {
    val p0 = matchXY(ns, "cx", "cy")
    val p1 = p0 + matchRXY(ns)
    setStyle(ns)
    val res = Ellipse(p0, p1)
    API.restoreStyle
    res
  }

  private def matchLine(ns: scala.xml.Node) = {
    val p1 = matchXY(ns, "x1", "y1")
    val p2 = matchXY(ns, "x2", "y2")
    setStyle(ns)
    val res = Line(p1, p2)
    API.restoreStyle
    res
  }

  private def matchText(ns: scala.xml.Node) = {
    //TODO hmm not working
    val p1 = matchXY(ns)
    // should also support dx/dy, rotate, textLength, lengthAdjust
    // and font attributes (as far as piccolo/awt can support them)
    setStyle(ns)
    val res = Text(ns.text, p1)
    API.restoreStyle
    res
  }

  private def matchPath(ns: scala.xml.Node): Shape = {
    val d = getAttr(ns, "d")
    if (d.nonEmpty) SvgPath(d.get)
    else new Shape { val node = null }
  }

  def apply(node: scala.xml.Node): Shape = {
    // should handle some of
    //   color, fill-rule, stroke, stroke-dasharray, stroke-dashoffset,
    //   stroke-linecap, stroke-linejoin, stroke-miterlimit, stroke-width,
    //   color-interpolation, color-rendering
    // and
    //   transform-list
    //
  node match {
      case <rect></rect> =>
        matchRect(node)
      case <circle></circle> =>
        matchCircle(node)
      case <ellipse></ellipse> =>
        matchEllipse(node)
      case <line></line> =>
        matchLine(node)
      case <text></text> =>
        matchText(node)
      case <polyline></polyline> =>
        setStyle(node)
        val res = Polyline(matchPoints(node))
        API.restoreStyle
        res
      case <polygon></polygon> =>
        setStyle(node)
        val res = Polygon(matchPoints(node))
        API.restoreStyle
        res
      case <path></path> =>
        matchPath(node)
      case <g>{ shapes @ _* }</g> =>
        new Shape { val node = null }
        //for (s <- shapes) yield SvgShape(s)
      case <svg>{ shapes @ _* }</svg> =>
        new Shape { val node = null }
        //for (s <- shapes) yield SvgShape(s)
      case _ => // unknown element, ignore
        new Shape { val node = null }
  }
  }
}

object ColorMode {
  type Color = java.awt.Color
  var mode: API.ColorModes = API.RGB(255, 255, 255)

  def apply(cm: API.ColorModes) { mode = cm }

  def color(v: Int) = {
    require(mode.isInstanceOf[API.GRAY] ||
            mode.isInstanceOf[API.RGB],
            "Color mode isn't GRAY or RGB")
    if (mode.isInstanceOf[API.GRAY]) {
      val vv = API.norm(v, 0, mode.asInstanceOf[API.GRAY].v).toFloat
      new Color(vv, vv, vv)
    } else {
      new Color(v)
    }
  }
  def color(v: Double) = {
    require(mode.isInstanceOf[API.GRAY], "Color mode isn't GRAY")
    val vv = v.toFloat
    new Color(vv, vv, vv)
  }

  def color(v: Int, a: Int) = {
    require(mode.isInstanceOf[API.GRAYA] ||
            mode.isInstanceOf[API.RGBA],
            "Color mode isn't GRAYA (gray with alpha) or RGBA")
    if (mode.isInstanceOf[API.GRAYA]) {
      val vv = API.norm(v, 0, mode.asInstanceOf[API.GRAYA].v).toFloat
      val aa = API.norm(a, 0, mode.asInstanceOf[API.GRAYA].a).toFloat
      new Color(vv, vv, vv, aa)
    } else {
      val aa = API.norm(a, 0, mode.asInstanceOf[API.RGBA].a).toFloat
      new Color(v | Math.lerp(0, 255, aa).toInt << 12, true)
    }
  }
  def color(v: Double, a: Double) = {
    require(v >= 0 && v <= 1, "Grayscale value off range")
    require(a >= 0 && a <= 1, "Alpha value off range")
    val vv = v.toFloat
    new Color(vv, vv, vv, a.toFloat)
  }

  def color(v1: Int, v2: Int, v3: Int) = {
    require(mode.isInstanceOf[API.RGB] ||
            mode.isInstanceOf[API.HSB],
            "Color mode isn't RGB or HSB")
    if (mode.isInstanceOf[API.RGB]) {
      val r = API.norm(v1, 0, mode.asInstanceOf[API.RGB].r).toFloat
      val g = API.norm(v2, 0, mode.asInstanceOf[API.RGB].g).toFloat
      val b = API.norm(v3, 0, mode.asInstanceOf[API.RGB].b).toFloat
      new Color(r, g, b)
    } else {
      val h = API.norm(v1, 0, mode.asInstanceOf[API.HSB].h).toFloat
      val s = API.norm(v2, 0, mode.asInstanceOf[API.HSB].s).toFloat
      val b = API.norm(v3, 0, mode.asInstanceOf[API.HSB].b).toFloat
      java.awt.Color.getHSBColor(h, s, b)
    }
  }
  def color(v1: Int, v2: Int, v3: Int, a: Int) = {
    require(mode.isInstanceOf[API.RGBA] ||
            mode.isInstanceOf[API.HSBA],
            "Color mode isn't RGBA or HSBA")
    if (mode.isInstanceOf[API.RGBA]) {
      val r = API.norm(v1, 0, mode.asInstanceOf[API.RGBA].r).toFloat
      val g = API.norm(v2, 0, mode.asInstanceOf[API.RGBA].g).toFloat
      val b = API.norm(v3, 0, mode.asInstanceOf[API.RGBA].b).toFloat
      val aa = API.norm(a, 0, mode.asInstanceOf[API.RGBA].a).toFloat
      new Color(r, g, b, aa)
    } else {
      //TODO transparency not working
      val h = API.norm(v1, 0, mode.asInstanceOf[API.HSBA].h).toFloat
      val s = API.norm(v2, 0, mode.asInstanceOf[API.HSBA].s).toFloat
      val b = API.norm(v3, 0, mode.asInstanceOf[API.HSBA].b).toFloat
      val aa = API.norm(a, 0, mode.asInstanceOf[API.HSBA].a).toFloat
      val c = java.awt.Color.getHSBColor(h, s, b)
      new Color(c.getRGB | Math.lerp(0, 255, aa).toInt << 12, true)
    }
  }

  def color(v1: Double, v2: Double, v3: Double) = {
    require(mode.isInstanceOf[API.RGB] ||
            mode.isInstanceOf[API.HSB],
            "Color mode isn't RGB or HSB")
    if (mode.isInstanceOf[API.RGB]) {
      val r = v1.toFloat
      val g = v2.toFloat
      val b = v3.toFloat
      new Color(r, g, b)
    } else {
      val h = v1.toFloat
      val s = v2.toFloat
      val b = v3.toFloat
      java.awt.Color.getHSBColor(h, s, b)
    }
  }
  def color(v1: Double, v2: Double, v3: Double, a: Double) = {
    require(mode.isInstanceOf[API.RGBA] ||
            mode.isInstanceOf[API.HSBA],
            "Color mode isn't RGBA or HSBA")
    if (mode.isInstanceOf[API.RGBA]) {
      val r = v1.toFloat
      val g = v2.toFloat
      val b = v3.toFloat
      val aa = a.toFloat
      new Color(r, g, b, aa)
    } else {
      val h = v1.toFloat
      val s = v2.toFloat
      val b = v3.toFloat
      val aa = a.toFloat
      val c = java.awt.Color.getHSBColor(h, s, b)
      new Color(c.getRGB | Math.lerp(0, 255, a).toInt << 12, true)
    }
  }
  def color(s: String): Color = s match {
    case ColorName(cc) => cc
    case "none"        => null
    case z             => java.awt.Color.decode(s)
  }
}

class RichColor (val c: java.awt.Color) {
  type Color = java.awt.Color
  def alpha = c.getAlpha
  def red = c.getRed
  def blue = c.getBlue
  def green = c.getGreen
  private def hsb =
    java.awt.Color.RGBtoHSB(c.getRed, c.getBlue, c.getGreen, null)
  def hue = {
    val h = floor(255 * (1 - this.hsb(0))) + 1
    if (h > 255) 0 else h.toInt
  }
  def saturation = (this.hsb(1) * 255).toInt
  def brightness = (this.hsb(2) * 255).toInt
  // TODO blendColor
}
object RichColor {
  def apply(c: java.awt.Color) = new RichColor(c)

  def lerpColor(from: RichColor, to: RichColor, amt: Double) = {
    require(amt >= 0d && amt <= 1d)
    new java.awt.Color(
      Math.lerp(from.red, to.red, amt).round.toInt,
      Math.lerp(from.green, to.green, amt).round.toInt,
      Math.lerp(from.blue, to.blue, amt).round.toInt
    )
  }
}

object Style {
  val savedStyles =
    new scala.collection.mutable.Stack[(Color, Color, java.awt.Stroke)]()
  val f = Impl.figure0

  def save {
    Utils.runInSwingThread {
      savedStyles push Tuple3(f.fillColor, f.lineColor, f.lineStroke)
    }
  }

  def restore {
    Utils.runInSwingThread {
      if (savedStyles nonEmpty) {
        val (fc, sc, st) = savedStyles.pop
        f.setFillColor(fc)
        f.setPenColor(sc)
        f.lineStroke = st
      }
    }
  }

  def apply(fc: Color, sc: Color, sw: Double)(body: => Unit) = {
    save
    f.setFillColor(fc)
    f.setPenColor(sc)
    f.setPenThickness(sw)
    try { body }
    finally { restore }
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

object Inputs {
  import edu.umd.cs.piccolo.event._
  //import java.awt.event.InputEvent

  var mousePos: Point = API.O
  var prevMousePos: Point = API.O
  var stepMousePos: Point = API.O
  var mouseBtn = 0
  var mousePressedFlag = false

  def activityStep() = {
    prevMousePos = stepMousePos
    stepMousePos = mousePos
  }

  def init() {
    val iel = new PBasicInputEventHandler {
      // This method is invoked when a node gains the keyboard focus.
      override def keyboardFocusGained(e: PInputEvent) {
        e match { case ee => println("keyboardFocusGained: e=" + ee) }
      }
      // This method is invoked when a node loses the keyboard focus.
      override def keyboardFocusLost(e: PInputEvent) {
        e match { case ee => println("keyboardFocusLost: e=" + ee) }
      }
      // Will get called whenever a key has been pressed down.
      override def keyPressed(e: PInputEvent) {
        e match { case ee => println("keyPressed: e=" + ee) }
      }
      // Will get called whenever a key has been released.
      override def keyReleased(e: PInputEvent) {
        e match { case ee => println("keyReleased: e=" + ee) }
      }
      // Will be called at the end of a full keystroke (down then up).
      override def keyTyped(e: PInputEvent) {
        e match { case ee => println("keyTyped: e=" + ee) }
      }
      // Will be called at the end of a full click (mouse pressed followed by mouse released).
      override def mouseClicked(e: PInputEvent) {
        super.mouseClicked(e)
        val p = e.getPosition
        mousePos = Point(p.getX, p.getY)
        mouseBtn = e.getButton
        e match { case ee => println("mouseClicked: e=" + ee) }
      }
      // Will be called when a drag is occurring.
      override def mouseDragged(e: PInputEvent) {
        super.mouseDragged(e)
        val p = e.getPosition
        mousePos = Point(p.getX, p.getY)
        e match { case ee => println("mouseDragged: e=" + ee) }
      }
      // Will be invoked when the mouse enters a specified region.
      override def mouseEntered(e: PInputEvent) {
        super.mouseEntered(e)
        e.pushCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR))
        val p = e.getPosition
        mousePos = Point(p.getX, p.getY)
        //e match { case ee => println("mouseEntered: e=" + ee) }
      }
      // Will be invoked when the mouse leaves a specified region.
      override def mouseExited(e: PInputEvent) {
        super.mouseExited(e)
        e.popCursor
        val p = e.getPosition
        mousePos = Point(p.getX, p.getY)
        mousePressedFlag = false
        //e match { case ee => println("mouseExited: e=" + ee) }
      }
      // Will be called when the mouse is moved.
      override def mouseMoved(e: PInputEvent) {
        super.mouseMoved(e)
        val p = e.getPosition
        mousePos = Point(p.getX, p.getY)
        //e match { case ee => println("mouseMoved: e=" + ee) }
      }
      // Will be called when a mouse button is pressed down.
      override def mousePressed(e: PInputEvent) {
        super.mousePressed(e)
        val p = e.getPosition
        mousePos = Point(p.getX, p.getY)
        mouseBtn = e.getButton
        mousePressedFlag = true
        //e match { case ee => println("mousePressed: e=" + ee) }
      }
      // Will be called when any mouse button is released.
      override def mouseReleased(e: PInputEvent) {
        super.mouseReleased(e)
        val p = e.getPosition
        mousePos = Point(p.getX, p.getY)
        mouseBtn = e.getButton
        mousePressedFlag = false
        //e match { case ee => println("mouseReleased: e=" + ee) }
      }
      // This method is invoked when the mouse wheel is rotated.
      override def mouseWheelRotated(e: PInputEvent) {
        super.mouseWheelRotated(e)
        val p = e.getPosition
        mousePos = Point(p.getX, p.getY)
        e match { case ee => println("mouseWheelRotated: e=" + ee) }
      }
      // This method is invoked when the mouse wheel is rotated by a block.
      override def mouseWheelRotatedByBlock(e: PInputEvent) {
        super.mouseWheelRotatedByBlock(e)
        val p = e.getPosition
        mousePos = Point(p.getX, p.getY)
        e match { case ee => println("mouseWheelRotatedByBlock: e=" + ee) }
      }
    }
    
    //iel.setEventFilter(new PInputEventFilter(PInputEventFilter.ALL_MODIFIERS_MASK))
    //InputEvent.
    //KEY_EVENT_MASK, MOUSE_EVENT_MASK, MOUSE_MOTION_EVENT_MASK, MOUSE_WHEEL_EVENT_MASK,

    Impl.canvas.addInputEventListener(iel)
  }
}