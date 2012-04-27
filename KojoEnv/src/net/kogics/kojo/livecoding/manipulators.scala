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
package net.kogics.kojo.livecoding

import javax.swing.JEditorPane
import javax.swing.text.Document

trait ManipulationContext {
  def isRunningEnabled: Boolean
  def runCode(code: String): Unit
  def codePane: JEditorPane
  def addManipulator(im: InteractiveManipulator)
  def removeManipulator(im: InteractiveManipulator)
}

trait InteractiveManipulator {
  def isAbsent: Boolean
  def isPresent: Boolean
  def close(): Unit
  def inSliderChange: Boolean
  def isHyperlinkPoint(doc: Document, offset: Int): Boolean
  def getHyperlinkSpan(doc: Document, offset: Int): Array[Int]
  def activate(doc: Document, offset: Int)
}
