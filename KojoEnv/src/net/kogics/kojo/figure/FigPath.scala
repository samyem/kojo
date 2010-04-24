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

  abstract sealed class SVGCmd(arg: Any) { def apply () }
  case class MoveToAbs(arg: Any) extends SVGCmd(arg) {
    def apply () = arg match {
        case pts: List[_] =>
          pts foreach { case(x: Float, y: Float) =>
            pos = (x, y)
            pPath.moveTo(pos._1, pos._2)
          }
        case _ =>
          error("Bad argument for SVG element")
      }
  }
  case class MoveToRel(arg: Any) extends SVGCmd(arg) {
    def apply () = arg match {
    case pts: List[_] =>
      pts foreach { case(x: Float, y: Float) =>
        pos = (pos._1 + x, pos._2 + y)
        pPath.moveTo(pos._1, pos._2)
      }
    case _ =>
      error("Bad argument for SVG element")
  }
  }
  case class LineToAbs(arg: Any) extends SVGCmd(arg) {
    def apply () = arg match {
    case pts: List[_] =>
      pts foreach { case(x: Float, y: Float) =>
        pos = (x, y)
        pPath.lineTo(pos._1, pos._2)
      }
    case _ =>
      error("Bad argument for SVG element")
  }
  }
  case class LineToRel(arg: Any) extends SVGCmd(arg) {
    def apply () = arg match {
    case pts: List[_] =>
      pts foreach { case(x: Float, y: Float) =>
        pos = (pos._1 + x, pos._2 + y)
        pPath.lineTo(pos._1, pos._2)
      }
    case _ =>
      error("Bad argument for SVG element")
  }
  }
  case class HLineToAbs(arg: Any) extends SVGCmd(arg) {
    def apply () = arg match {
    case pts: List[_] =>
      pts foreach { case x: Float =>
        pos = (x, pos._2)
        pPath.lineTo(pos._1, pos._2)
      }
    case _ =>
      error("Bad argument for SVG element")
  }
  }
  case class HLineToRel(arg: Any) extends SVGCmd(arg) {
    def apply () = arg match {
    case pts: List[_] =>
      pts foreach { case x: Float =>
        pos = (pos._1 + x, pos._2)
        pPath.lineTo(pos._1, pos._2)
      }
    case _ =>
      error("Bad argument for SVG element")
  }
  }
  case class VLineToAbs(arg: Any) extends SVGCmd(arg) {
    def apply () = arg match {
    case pts: List[_] =>
      pts foreach { case y: Float =>
        pos = (pos._1, y)
        pPath.lineTo(pos._1, pos._2)
      }
    case _ =>
      error("Bad argument for SVG element")
  }
  }
  case class VLineToRel(arg: Any) extends SVGCmd(arg) {
    def apply () = arg match {
    case pts: List[_] =>
      pts foreach { case y: Float =>
        pos = (pos._1, pos._2 + y)
        pPath.lineTo(pos._1, pos._2)
      }
    case _ =>
      error("Bad argument for SVG element")
  }
  }
  def foo (bar: Any): (Float, Float) = bar match {
    case (x: Float, y: Float) => (x, y)
    case _ => error("Bad coordinate pair")
  }
  case class CurveToAbs(arg: Any) extends SVGCmd(arg) {
    def apply () = arg match {
    case pts: List[_] =>
      pts foreach { case(pa, pb, pc) =>
        val (p0, p1, p2) = (foo(pa), foo(pb), foo(pc))
        pos = p2
        pPath.curveTo(p0._1, p0._2, p1._1, p1._2, p2._1, p2._2)
      }
    case _ =>
      error("Bad argument for SVG element")
  }
  }
  case class CurveToRel(arg: Any) extends SVGCmd(arg) {
    def apply () = arg match {
    case pts: List[_] =>
      pts foreach { case(pa, pb, pc) =>
        val (p0, p1, p2) = (foo(pa), foo(pb), foo(pc))
        pos = (pos._1 + p2._1, pos._2 + p2._2)
        pPath.curveTo(
          pos._1 + p0._1, pos._2 + p0._2,
          pos._1 + p1._1, pos._2 + p1._2,
          pos._1 + p2._1, pos._2 + p2._2
        )
      }
    case _ =>
      error("Bad argument for SVG element")
  }
  }
  case class SmoothCurveToAbs(arg: Any) extends SVGCmd(arg) {
    def apply () = {}
  }
  case class SmoothCurveToRel(arg: Any) extends SVGCmd(arg) {
    def apply () = {}
  }
  case class QuadBezierAbs(arg: Any) extends SVGCmd(arg) {
    def apply () = {}
  }
  case class QuadBezierRel(arg: Any) extends SVGCmd(arg) {
    def apply () = {}
  }
  case class SmoothQuadBezierAbs(arg: Any) extends SVGCmd(arg) {
    def apply () = {}
  }
  case class SmoothQuadBezierRel(arg: Any) extends SVGCmd(arg) {
    def apply () = {}
  }
  case class EllipticalArcAbs(arg: Any) extends SVGCmd(arg) {
    def apply () = {}
  }
  case class EllipticalArcRel(arg: Any) extends SVGCmd(arg) {
    def apply () = {}
  }
  case class Close(arg: Any) extends SVGCmd(arg) {
    def this() = this(null)
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
    def apply (d: String) = {
      parseAll(svgpath, d)
    }
  }

  var pos: (Float, Float) = (0, 0)
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


