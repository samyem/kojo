/*
 * Copyright (C) 2012 Lalit Pant <pant.lalit@gmail.com>
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
package livecoding

import javax.swing.text.Document

class IpmProvider {
  val ces = CodeExecutionSupport.instance
  val manips = List(new IntManipulator(ces), new FloatManipulator(ces))
  
  def isHyperlinkPoint(doc: Document, offset: Int): Boolean = {
    manips.exists {_ isHyperlinkPoint(doc, offset)}
  }
  
  def getHyperlinkSpan(doc: Document, offset: Int): Array[Int] = {
    val manip = manips.find { _ isHyperlinkPoint(doc, offset) }
    manip.map { _ getHyperlinkSpan(doc, offset)}.getOrElse(null)
  }
  
  def performClickAction(doc: Document, offset: Int) {
    val manip = manips.find { _ isHyperlinkPoint(doc, offset) }
    manip.foreach { _ activate(doc, offset)}
  }
}
