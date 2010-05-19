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

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._

import org.netbeans.modules.csl.api.test.CslTestBase;


class ScalaKeystrokeHandlerTest extends CslTestBase("ScalaKeystrokeHandlerTest") {

  override def setUp: Unit = {
  }

  override def tearDown: Unit = {
  }

  override def runInEQ = true
  override def getPreferredLanguage = new ScalaLanguage
  override def getPreferredMimeType = org.netbeans.modules.scala.core.lexer.ScalaTokenId.SCALA_MIME_TYPE

  def testClosingBraceInsertion {
    insertChar("class X ^", '{', "class X {^}", null, false);
  }

  def testClosingBracketInsertion {
    insertChar("class X^", '[', "class X[^]", null, false);
  }

  def testClosingParenInsertion {
    insertChar("class X^", '(', "class X(^)", null, false);
  }

  def testClosingQuoteInsertion {
    insertChar("val x = ^", '"', "val x = \"^\"", null, false);
  }

  def testClosingSingleQuoteInsertion {
    insertChar("val x = ^", '\'', "val x = '^'", null, false);
  }
}
