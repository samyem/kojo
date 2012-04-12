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
package util

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.ShouldMatchers


@RunWith(classOf[JUnitRunner])
class UtilsTest extends FunSuite with ShouldMatchers with BeforeAndAfter {

  test("turtle mode script filter") {
    val files = List("test.kojo", "test.mw.kojo", "test.st.kojo", "test.tw.kojo")
    Utils.modeFilter(files, TwMode) should be (List("test.kojo", "test.tw.kojo"))
  }

  test("staging mode script filter") {
    val files = List("test.kojo", "test.mw.kojo", "test.st.kojo", "test.tw.kojo")
    Utils.modeFilter(files, StagingMode) should be (List("test.kojo", "test.st.kojo"))
  }

  test("mathworld mode script filter") {
    val files = List("test.kojo", "test.mw.kojo", "test.st.kojo", "test.tw.kojo")
    Utils.modeFilter(files, MwMode) should be (List("test.kojo", "test.mw.kojo"))
  }
}
