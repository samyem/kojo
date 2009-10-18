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
  val Keywords = List("for", "if", "else", "while", "return", "true", "false")
  val KeywordTemplates = Map(
    "for" -> "for(i <- 1 to 10){\n}",
    "while" -> "while(){\n}",
    "if" -> "if(){\n}"
  )
  val MethodTemplates = Map(
    "repeat" -> "repeat(3){\n}"
  )
  
  val MethodFilter = List("animationDelay_=")
  val VarFilter = List("builtins", "predef")

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
