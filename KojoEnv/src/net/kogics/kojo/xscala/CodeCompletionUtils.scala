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

package net.kogics.kojo.xscala

object CodeCompletionUtils {
  val NotIdChars = """ .(){}!%&+\-<=>?@\\^`|~#:/*""" + "\n\r\t"

  import org.netbeans.modules.scala.core.lexer.ScalaTokenId
  val Keywords = 
    ScalaTokenId.values.filter { v =>
      v.asInstanceOf[ScalaTokenId.V].primaryCategory == "keyword"
    }.map { v =>
      v.asInstanceOf[ScalaTokenId.V].fixedText
    }.toList

  val KeywordTemplates = Map(
    "for" -> "for (i <- 1 to ${n}) {\n    ${cursor}\n}",
    "while" -> "while (${condition}) {\n    ${cursor}\n}",
    "if" -> "if (${condition}) {\n    ${cursor}\n}"
  )
  val MethodTemplates = Map(
    "repeat" -> "repeat (${n}) {\n    ${cursor}\n}",
    "forward" -> "forward(${n})",
    "back" -> "back(${n})",
    "right" -> "right(${cursor})",
    "left" -> "left(${cursor})",
    "clear" -> "clear()",
    "home" -> "home()",
    "turn" -> "turn(${angleInDegrees})",
    "setHeading" -> "setHeading(${angleInDegrees})",
    "towards" -> "towards(${x}, ${y})",
    "moveTo" -> "moveTo(${x}, ${y})",
    "jumpTo" -> "jumpTo(${x}, ${y})",
    "setPosition" -> "setPosition(${x}, ${y})",
    "setPenColor" -> "setPenColor(${color})",
    "setPenThickness" -> "setPenThickness(${n})",
    "setFillColor" -> "setFillColor(${color})",
    "setAnimationDelay" -> "setAnimationDelay(${milliseconds})",
    "println" -> "println(${cursor})",
    "inspect" -> "inspect(${cursor})",
    "point" -> "point(${x}, ${y})",
    "line" -> "line(${x0}, ${y0}, ${x1}, ${y1})",
    "rectangle" -> "rectangle(${x0}, ${y0}, ${width}, ${height})",
    "text" -> "text(${content}, ${x}, ${y})",
    "circle" -> "circle(${cx}, ${cy}, ${radius})",
    "ellipse" -> "ellipse(${cx}, ${cy}, ${width}, ${height})",
    "arc" -> "arc(${cx}, ${cy}, ${radius}, ${startDegree}, ${extentDegree})",
    "refresh" -> "refresh {\n    ${cursor}\n}",
    "random" -> "random(${upperBound})",
    "randomDouble" -> "randomDouble(${upperBound})",
    "zoom" -> "zoom(${zoomFactor}, ${centerX}, ${centerY})"
  )
  
  val MethodDropFilter = List("turtle0")
  val VarDropFilter = List("builtins", "predef")
  val InternalVarsRe = java.util.regex.Pattern.compile("""res\d+""")

  def notIdChar(c: Char): Boolean =  NotIdChars.contains(c)

  def findLastIdentifier(rstr: String): Option[String] = {
    val str = " " + rstr
    var remaining = str.length
    while(remaining > 0) {
      if (notIdChar(str(remaining-1))) return Some(str.substring(remaining))
      remaining -= 1
    }
    None
  }

  def findIdentifier(str: String): (Option[String], Option[String]) = {
    if (str.length == 0) return (None, None)

    if (str.endsWith(".")) {
      (findLastIdentifier(str.substring(0, str.length-1)), None)
    }
    else {
      val lastDot = str.lastIndexOf('.')
      if (lastDot == -1) (None, findLastIdentifier(str))
      else {
        val tPrefix = str.substring(lastDot+1)
        if (tPrefix == findLastIdentifier(tPrefix).get)
          (findLastIdentifier(str.substring(0, lastDot)), Some(tPrefix))
        else
          (None, findLastIdentifier(tPrefix))
      }
    }
  }
}
