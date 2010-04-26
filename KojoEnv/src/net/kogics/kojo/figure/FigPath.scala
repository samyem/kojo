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

  import scala.util.parsing.combinator._

  trait Coords {
    protected var curp: (Float, Float) = (0, 0)
    protected var lcp = curp  // last control point
    def toXY (p: (Float, Float)): (Float, Float) = p match {
      case (x: Float, y: Float) => (x, y)
    }
    def toXY (p: Any): (Float, Float) = p match {
      case (x: Float, y: Float) => (x, y)
      case _ => error("Expected coordinate values")
    }
  }
  trait AbsoluteCoords extends Coords {
    def foreachXY(pts: List[_])(fn: (Float, Float) => Unit) {
      pts foreach {
        case (x: Float, y: Float) => fn(x, y)
      }
      curp = toXY(pts.last)
    }
    def foreachX(xs: List[_])(fn: (Float, Float) => Unit) {
      xs foreach {
        case x: Float =>
          fn(x, curp._2)
      }
      curp = (xs.last.asInstanceOf[Float], curp._2)
    }
    def foreachY(ys: List[_])(fn: (Float, Float) => Unit) {
      ys foreach {
        case y: Float =>
          fn(curp._1, y)
      }
      curp = (curp._1, ys.last.asInstanceOf[Float])
    }
    def foreachCurve(cs: List[_])(fn: (Any, Any, Any) => Unit) {
      var tempCP: (Float, Float) = (0, 0)
      var tempP: (Float, Float) = (0, 0)
      cs foreach {
        case (p1, p2, p) =>
          val temp = toXY(p1)
          tempCP = toXY(p2)
          tempP = toXY(p)
          fn(temp, tempCP, tempP)
      }
      lcp = tempCP
      curp = tempP
    }
    def foreachSmoothCurve(cs: List[_])(fn: (Any, Any, Any) => Unit) {
      var tempCP: (Float, Float) = (0, 0)
      var tempP: (Float, Float) = (0, 0)
      cs foreach {
        case (p2, p) =>
          val temp = (
            curp._1 + (curp._1 - lcp._1),
            curp._2 + (curp._2 - lcp._2)
          )
          tempCP = toXY(p2)
          tempP = toXY(p)
          fn(temp, tempCP, tempP)
      }
      lcp = tempCP
      curp = tempP
    }
    def foreachQuad(cs: List[_])(fn: (Any, Any) => Unit) {
      var tempCP: (Float, Float) = (0, 0)
      var tempP: (Float, Float) = (0, 0)
      cs foreach {
        case (p1, p) =>
          tempCP = toXY(p1)
          tempP = toXY(p)
          fn(tempCP, tempP)
      }
      lcp = tempCP
      curp = tempP
    }
    def foreachSmoothQuad(cs: List[_])(fn: (Any, Any) => Unit) {
      var tempCP: (Float, Float) = (0, 0)
      var tempP: (Float, Float) = (0, 0)
      cs foreach {
        case (p) =>
          tempCP = (
            curp._1 + (curp._1 - lcp._1),
            curp._2 + (curp._2 - lcp._2)
          )
          tempP = toXY(p)
          fn(tempCP, tempP)
      }
      lcp = tempCP
      curp = tempP
    }
  }
  trait RelativeCoords extends Coords {
    def foreachXY(pts: List[_])(fn: (Float, Float) => Unit) {
      var temp: (Float, Float) = (0, 0)
      pts foreach {
        case (x: Float, y: Float) =>
          temp = (curp._1 + x, curp._2 + y)
          fn(temp._1, temp._2)
      }
      curp = temp
    }
    def foreachX(xs: List[_])(fn: (Float, Float) => Unit) {
      var temp: Float = 0
      xs foreach {
        case x: Float =>
          temp = curp._1 + x
          fn(temp, curp._2)
      }
      curp = (temp, curp._2)
    }
    def foreachY(ys: List[_])(fn: (Float, Float) => Unit) {
      var temp: Float = 0
      ys foreach {
        case y: Float =>
          temp = curp._2 + y
          fn(curp._1, temp)
      }
      curp = (curp._1, temp)
    }
    def foreachCurve(cs: List[_])(fn: (Any, Any, Any) => Unit) {
      var tempCP: (Float, Float) = (0, 0)
      var tempP: (Float, Float) = (0, 0)
      cs foreach {
        case (p1, p2, p) =>
          val (x1, y1) = toXY(p1)
          val (x2, y2) = toXY(p2)
          val (x, y) = toXY(p)
          val temp = (curp._1 + x1, curp._2 + y1)
          tempCP = (curp._1 + x2, curp._2 + y2)
          tempP = (curp._1 + x, curp._2 + y)
          fn(temp, tempCP, tempP)
      }
      lcp = tempCP
      curp = tempP
    }
    def foreachSmoothCurve(cs: List[_])(fn: (Any, Any, Any) => Unit) {
      var tempCP: (Float, Float) = (0, 0)
      var tempP: (Float, Float) = (0, 0)
      cs foreach {
        case (p2, p) =>
          val temp = (
            curp._1 + (curp._1 - lcp._1),
            curp._2 + (curp._2 - lcp._2)
          )
          val (x2, y2) = toXY(p2)
          val (x, y) = toXY(p)
          tempCP = (curp._1 + x2, curp._2 + y2)
          tempP = (curp._1 + x, curp._2 + y)
          fn(temp, tempCP, tempP)
      }
      lcp = tempCP
      curp = tempP
    }
    def foreachQuad(cs: List[_])(fn: (Any, Any) => Unit) {
      var tempCP: (Float, Float) = (0, 0)
      var tempP: (Float, Float) = (0, 0)
      cs foreach {
        case (p1, p) =>
          val (x1, y1) = toXY(p1)
          val (x, y) = toXY(p)
          tempCP = (curp._1 + x1, curp._2 + y1)
          tempP = (curp._1 + x, curp._2 + y)
          fn(tempCP, tempP)
      }
      lcp = tempCP
      curp = tempP
    }
    def foreachSmoothQuad(cs: List[_])(fn: (Any, Any) => Unit) {
      var tempCP: (Float, Float) = (0, 0)
      var tempP: (Float, Float) = (0, 0)
      cs foreach {
        case (p) =>
          val (x, y) = toXY(p)
          tempCP = (
            curp._1 + (curp._1 - lcp._1),
            curp._2 + (curp._2 - lcp._2)
          )
          tempP = (curp._1 + x, curp._2 + y)
          fn(tempCP, tempP)
      }
      lcp = tempCP
      curp = tempP
    }
  }

  abstract sealed class SVGCmd { def apply (): Unit }
  case class MoveToAbs(pts: List[_]) extends SVGCmd with AbsoluteCoords {
    def apply () = foreachXY(pts) {
      case(x: Float, y: Float) => pPath.moveTo(x, y)
    }
  }
  case class MoveToRel(pts: List[_]) extends SVGCmd with RelativeCoords {
    def apply () = foreachXY(pts) {
      case(x: Float, y: Float) => pPath.moveTo(x, y)
    }
  }
  case class LineToAbs(pts: List[_]) extends SVGCmd with AbsoluteCoords {
    def apply () = foreachXY(pts) {
      case(x: Float, y: Float) => pPath.lineTo(x, y)
    }
  }
  case class LineToRel(pts: List[_]) extends SVGCmd with RelativeCoords {
    def apply () = foreachXY(pts) {
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
        val (x1, y1) = toXY(p1)
        val (x2, y2) = toXY(p2)
        val (x, y)   = toXY(p)
        pPath.curveTo(x1, y1, x2, y2, x, y)
    }
  }
  case class CurveToRel(cs: List[_]) extends SVGCmd with RelativeCoords {
    def apply () = foreachCurve(cs) {
      case(p1, p2, p) =>
        val (x1, y1) = toXY(p1)
        val (x2, y2) = toXY(p2)
        val (x, y)   = toXY(p)
        pPath.curveTo(x1, y1, x2, y2, x, y)
    }
  }
  case class SmoothCurveToAbs(cs: List[_]) extends SVGCmd with AbsoluteCoords {
    def apply () = foreachSmoothCurve(cs) {
      case(p1, p2, p) =>
        val (x1, y1) = toXY(p1)
        val (x2, y2) = toXY(p2)
        val (x, y)   = toXY(p)
        pPath.curveTo(x1, y1, x2, y2, x, y)
    }
  }
  case class SmoothCurveToRel(cs: List[_]) extends SVGCmd with RelativeCoords {
    def apply () = foreachSmoothCurve(cs) {
      case(p1, p2, p) =>
        val (x1, y1) = toXY(p1)
        val (x2, y2) = toXY(p2)
        val (x, y)   = toXY(p)
        pPath.curveTo(x1, y1, x2, y2, x, y)
    }
  }
  case class QuadBezierAbs(cs: List[_]) extends SVGCmd with AbsoluteCoords {
    def apply () = foreachQuad(cs) {
      case(p1, p) =>
        val (x1, y1) = toXY(p1)
        val (x, y)   = toXY(p)
        pPath.quadTo(x1, y1, x, y)
    }
  }
  case class QuadBezierRel(cs: List[_]) extends SVGCmd with RelativeCoords {
    def apply () = foreachQuad(cs) {
      case(p1, p) =>
        val (x1, y1) = toXY(p1)
        val (x, y)   = toXY(p)
        pPath.quadTo(x1, y1, x, y)
    }
  }
  case class SmoothQuadBezierAbs(cs: List[_]) extends SVGCmd with AbsoluteCoords {
    def apply () = foreachSmoothQuad(cs) {
      case(p1, p) =>
        val (x1, y1) = toXY(p1)
        val (x, y)   = toXY(p)
        pPath.quadTo(x1, y1, x, y)
    }
  }
  case class SmoothQuadBezierRel(cs: List[_]) extends SVGCmd with RelativeCoords {
    def apply () = foreachSmoothQuad(cs) {
      case(p1, p) =>
        val (x1, y1) = toXY(p1)
        val (x, y)   = toXY(p)
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


