/*
 * Copyright (C) 2010 Peter Lewerin <peter.lewerin@tele2.se>
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
package figure

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.nodes._
import edu.umd.cs.piccolo.util._
import edu.umd.cs.piccolo.event._

import javax.swing._

import core._

class FigPath (val canvas: PCanvas, d: String) extends core.Path(d) with FigShape {
  val pPath = new PPath()

  type Point = (Float, Float)
  type CurvePointList = (Point, Point, Point)
  type QuadPointList = (Point, Point)

  var currentPoint: Point = _
  var currentControlPoint = currentPoint
  def reflectedControlPoint = (
    2 * currentPoint._1 - currentControlPoint._1,
    2 * currentPoint._2 - currentControlPoint._2
  )

  trait Coords {
    var t1: Point = _
    var t2: Point = _
    var t: Point = _

    def toPoint(x: Float, y: Float): Point
    def toPoint(p: Any): Point = p match {
      case (x: Float, y: Float) => toPoint(x, y)
      case _ => error("Expected coordinate values")
    }
  }
  trait AbsoluteCoords extends Coords {
    def toPoint(x: Float, y: Float) = (x, y)

    def foreachPoint(pts: List[_])(fn: Point => Unit) {
      pts map toPoint foreach fn
      currentPoint = toPoint(pts.last)
    }

    def foreachX(xs: List[_])(fn: Point => Unit) {
      xs foreach { case x: Float => fn(x, currentPoint._2) }
      currentPoint = (xs.last.asInstanceOf[Float], currentPoint._2)
    }

    def foreachY(ys: List[_])(fn: Point => Unit) {
      ys foreach { case y: Float => fn(currentPoint._1, y) }
      currentPoint = (currentPoint._1, ys.last.asInstanceOf[Float])
    }

    def foreachCurve(cs: List[_])(fn: CurvePointList => Unit) {
      cs foreach {
        case (p1, p2, p) =>
          val t1 = toPoint(p1)
          t2 = toPoint(p2)
          t = toPoint(p)
          fn(t1, t2, t)
      }
      currentControlPoint = t2
      currentPoint = t
    }

    def foreachSmoothCurve(cs: List[_])(fn: CurvePointList => Unit) {
      cs foreach {
        case (p2, p) =>
          val t1 = reflectedControlPoint
          t2 = toPoint(p2)
          t = toPoint(p)
          fn(t1, t2, t)
      }
      currentControlPoint = t2
      currentPoint = t
    }

    def foreachQuad(cs: List[_])(fn: QuadPointList => Unit) {
      cs foreach {
        case (p1, p) =>
          t1 = toPoint(p1)
          t = toPoint(p)
          fn(t1, t)
      }
      currentControlPoint = t1
      currentPoint = t
    }

    def foreachSmoothQuad(cs: List[_])(fn: QuadPointList => Unit) {
      cs foreach {
        case p =>
          val t1 = reflectedControlPoint
          t = toPoint(p)
          fn(t1, t)
      }
      currentControlPoint = t1
      currentPoint = t
    }
  }


  trait RelativeCoords extends Coords {
    def toPoint(x: Float, y: Float) = (currentPoint._1 + x, currentPoint._2 + y)

    def foreachPoint(pts: List[_])(fn: Point => Unit) {
      pts map toPoint foreach fn
      currentPoint = toPoint(pts.last)
    }

    def foreachX(xs: List[_])(fn: Point => Unit) {
      xs foreach { case x0: Float =>
        val (x, y) = toPoint(x0, 0)
        t = (x, y)
        fn(x, y)
      }
      currentPoint = t
    }

    def foreachY(ys: List[_])(fn: Point => Unit) {
      ys foreach { case y0: Float =>
        val (x, y) = toPoint(0, y0)
        t = (x, y)
        fn(x, y)
      }
      currentPoint = t
    }

    def foreachCurve(cs: List[_])(fn: CurvePointList => Unit) {
      cs foreach {
        case (p1, p2, p) =>
          val t1 = toPoint(p1)
          t2 = toPoint(p2)
          t = toPoint(p)
          fn(t1, t2, t)
      }
      currentControlPoint = t2
      currentPoint = t
    }

    def foreachSmoothCurve(cs: List[_])(fn: CurvePointList => Unit) {
      var t2 = (0f, 0f)
      var t = (0f, 0f)
      cs foreach {
        case (p2, p) =>
          val t1 = reflectedControlPoint
          t2 = toPoint(p2)
          t = toPoint(p)
          fn(t1, t2, t)
      }
      currentControlPoint = t2
      currentPoint = t
    }

    def foreachQuad(cs: List[_])(fn: QuadPointList => Unit) {
      cs foreach {
        case (p1, p) =>
          t1 = toPoint(p1)
          t = toPoint(p)
          fn(t1, t)
      }
      currentControlPoint = t1
      currentPoint = t
    }

    def foreachSmoothQuad(cs: List[_])(fn: QuadPointList => Unit) {
      cs foreach {
        case p =>
          val t1 = reflectedControlPoint
          t = toPoint(p)
          fn(t1, t)
      }
      currentControlPoint = t1
      currentPoint = t
    }
  }

  abstract sealed class SVGCmd { def apply (): Unit }
  case class MoveToAbs(pts: List[_]) extends SVGCmd with AbsoluteCoords {
    def apply () = foreachPoint(pts) {
      case(x: Float, y: Float) => pPath.moveTo(x, y)
    }
  }
  case class MoveToRel(pts: List[_]) extends SVGCmd with RelativeCoords {
    def apply () = foreachPoint(pts) {
      case(x: Float, y: Float) => pPath.moveTo(x, y)
    }
  }
  case class LineToAbs(pts: List[_]) extends SVGCmd with AbsoluteCoords {
    def apply () = foreachPoint(pts) {
      case(x: Float, y: Float) => pPath.lineTo(x, y)
    }
  }
  case class LineToRel(pts: List[_]) extends SVGCmd with RelativeCoords {
    def apply () = foreachPoint(pts) {
      case(x: Float, y: Float) => pPath.lineTo(x, y)
    }
  }
  case class HLineToAbs(xs: List[_]) extends SVGCmd with AbsoluteCoords {
    def apply () = foreachX(xs) {
      case(x: Float, y: Float) => pPath.lineTo(x, y)
    }
  }
  case class HLineToRel(xs: List[_]) extends SVGCmd with RelativeCoords {
    def apply () = foreachX(xs) {
      case(x: Float, y: Float) => pPath.lineTo(x, y)
    }
  }
  case class VLineToAbs(ys: List[_]) extends SVGCmd with AbsoluteCoords {
    def apply () = foreachY(ys) {
      case(x: Float, y: Float) => pPath.lineTo(x, y)
    }
  }
  case class VLineToRel(ys: List[_]) extends SVGCmd with RelativeCoords {
    def apply () = foreachY(ys) {
      case(x: Float, y: Float) => pPath.lineTo(x, y)
    }
  }
  case class CurveToAbs(cs: List[_]) extends SVGCmd with AbsoluteCoords {
    def apply () = foreachCurve(cs) {
      case(p1, p2, p) =>
        val (x1, y1) = toPoint(p1)
        val (x2, y2) = toPoint(p2)
        val (x, y)   = toPoint(p)
        pPath.curveTo(x1, y1, x2, y2, x, y)
    }
  }
  case class CurveToRel(cs: List[_]) extends SVGCmd with RelativeCoords {
    def apply () = foreachCurve(cs) {
      case(p1, p2, p) =>
        val (x1, y1) = toPoint(p1)
        val (x2, y2) = toPoint(p2)
        val (x, y)   = toPoint(p)
        pPath.curveTo(x1, y1, x2, y2, x, y)
    }
  }
  case class SmoothCurveToAbs(cs: List[_]) extends SVGCmd with AbsoluteCoords {
    def apply () = foreachSmoothCurve(cs) {
      case(p1, p2, p) =>
        val (x1, y1) = toPoint(p1)
        val (x2, y2) = toPoint(p2)
        val (x, y)   = toPoint(p)
        pPath.curveTo(x1, y1, x2, y2, x, y)
    }
  }
  case class SmoothCurveToRel(cs: List[_]) extends SVGCmd with RelativeCoords {
    def apply () = foreachSmoothCurve(cs) {
      case(p1, p2, p) =>
        val (x1, y1) = toPoint(p1)
        val (x2, y2) = toPoint(p2)
        val (x, y)   = toPoint(p)
        pPath.curveTo(x1, y1, x2, y2, x, y)
    }
  }
  case class QuadBezierAbs(cs: List[_]) extends SVGCmd with AbsoluteCoords {
    def apply () = foreachQuad(cs) {
      case(p1, p) =>
        val (x1, y1) = toPoint(p1)
        val (x, y)   = toPoint(p)
        pPath.quadTo(x1, y1, x, y)
    }
  }
  case class QuadBezierRel(cs: List[_]) extends SVGCmd with RelativeCoords {
    def apply () = foreachQuad(cs) {
      case(p1, p) =>
        val (x1, y1) = toPoint(p1)
        val (x, y)   = toPoint(p)
        pPath.quadTo(x1, y1, x, y)
    }
  }
  case class SmoothQuadBezierAbs(cs: List[_]) extends SVGCmd with AbsoluteCoords {
    def apply () = foreachSmoothQuad(cs) {
      case(p1, p) =>
        val (x1, y1) = toPoint(p1)
        val (x, y)   = toPoint(p)
        pPath.quadTo(x1, y1, x, y)
    }
  }
  case class SmoothQuadBezierRel(cs: List[_]) extends SVGCmd with RelativeCoords {
    def apply () = foreachSmoothQuad(cs) {
      case(p1, p) =>
        val (x1, y1) = toPoint(p1)
        val (x, y)   = toPoint(p)
        pPath.quadTo(x1, y1, x, y)
    }
  }
  case class EllipticalArcAbs(as: List[_]) extends SVGCmd {
    def apply () = {}
  }
  case class EllipticalArcRel(as: List[_]) extends SVGCmd {
    def apply () = {}
  }
  case class Close() extends SVGCmd {
    def apply () = pPath.closePath
  }

  import scala.util.parsing.combinator._

  class SVGPathParser extends JavaTokenParsers {
    def drawto: Parser[Any]       = lineto | closepath | hlineto | vlineto | curveto | sCurveto | qBezierto | sqBezierto | elliptArc
    def svgpath: Parser[Any]      = rep(pathcommand)
    def pathcommand: Parser[Any]  = moveto~rep(drawto)                   ^^ { case a~b => (a, b) }
    def closepath: Parser[Any]    = "Z" | "z"                            ^^ { case _ => Close() }
    def moveto: Parser[Any]       = ("M" | "m")~repsep(coordPair, oc)    ^^ { case "M"~b => MoveToAbs(b)           ; case "m"~b => MoveToRel(b) }
    def lineto: Parser[Any]       = ("L" | "l")~repsep(coordPair, oc)    ^^ { case "L"~b => LineToAbs(b)           ; case "l"~b => LineToRel(b) }
    def hlineto: Parser[Any]      = ("H" | "h")~repsep(number, oc)       ^^ { case "H"~b => HLineToAbs(b)          ; case "h"~b => HLineToRel(b) }
    def vlineto: Parser[Any]      = ("V" | "v")~repsep(number, oc)       ^^ { case "V"~b => VLineToAbs(b)          ; case "v"~b => VLineToRel(b) }
    def curveto: Parser[Any]      = ("C" | "c")~repsep(coordPairTri, oc) ^^ { case "C"~b => CurveToAbs(b)          ; case "c"~b => CurveToRel(b) }
    def sCurveto: Parser[Any]     = ("S" | "s")~repsep(coordPairDbl, oc) ^^ { case "S"~b => SmoothCurveToAbs(b)    ; case "s"~b => SmoothCurveToRel(b) }
    def qBezierto: Parser[Any]    = ("Q" | "q")~repsep(coordPairDbl, oc) ^^ { case "Q"~b => QuadBezierAbs(b)       ; case "q"~b => QuadBezierRel(b) }
    def sqBezierto: Parser[Any]   = ("T" | "t")~repsep(coordPair, oc)    ^^ { case "T"~b => SmoothQuadBezierAbs(b) ; case "t"~b => SmoothQuadBezierRel(b) }
    def elliptArc: Parser[Any]    = ("A" | "a")~repsep(arcArg, oc)       ^^ { case "A"~b => EllipticalArcAbs(b)    ; case "a"~b => EllipticalArcRel(b) }
    def coordPair: Parser[Any]    = number~oc~number                     ^^ { case a~c~b => (a, b) }
    def coordPairDbl: Parser[Any] = coordPair~oc~coordPair               ^^ { case a~c~b => (a, b) }
    def coordPairTri: Parser[Any] = coordPair~oc~coordPair~oc~coordPair  ^^ { case a~d~b~e~c => (a, b, c) }
    def arcArg: Parser[Any]       =
      number~oc~number~oc~number~oc~flag~oc~flag~oc~coordPair            ^^ {
      case n1~a~n2~b~n3~c~f1~d~f2~e~cp => (n1, n2, n3, f1, f2, cp)
    }
    def flag: Parser[Any]         = "0" | "1"                            ^^ (_.toInt)
    def oc: Parser[Any]           = opt(",")
    def number: Parser[Any]       = floatingPointNumber                  ^^ (_.toFloat)
  }

  object ParseExpr extends SVGPathParser {
    def apply (d: String) = parseAll(svgpath, d)
  }

  try {
    ParseExpr(d).get match {
      case List((move: SVGCmd, drawcmds: List[_])) =>
        move()
        drawcmds foreach (_.asInstanceOf[SVGCmd]())
      case List() =>
    }
  }
  catch {
    case e => throw e
  }

  protected val piccoloNode = pPath
}


