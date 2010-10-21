/*
 * Copyright (C) 2010 Lalit Pant <pant.lalit@gmail.com>
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
package story

import org.junit.Test
import org.junit.Assert._

class StoryTest extends KojoTestBase {

  @Test
  def test1 {
    val html1 =
      <div style="color:green; font-size=18px">
        Page 1
      </div>
    val pg1 = Page(html1)

    val story = Story(pg1)
    assertFalse(story.hasNextView)
    assertFalse(story.hasPrevView)
    assertEquals(html1, story.view)
  }

  @Test
  def test2 {
    val para1 =
      <p>
        Para 1
      </p>

    val pg1 = IncrPage(
      style="",
      Para(para1)
    )

    val story = Story(pg1)

    val html1 =
      <div style="">
        {para1}
      </div>

    assertFalse(story.hasNextView)
    assertFalse(story.hasPrevView)

    val pp = new xml.PrettyPrinter(2, 2)
    assertEquals(pp.format(html1), pp.format(story.view))
  }

  @Test
  def test3 {
    val para1 =
      <p >
        Para 1
      </p>

    val para2 =
      <p >
        Para 2
      </p>

    val pg1 = IncrPage(
      style="color:green",
      Para(para1),
      Para(para2)
    )


    val story = Story(pg1)

    val html1 =
      <div style="color:green">
        {para1}
      </div>

    assertTrue(story.hasNextView)
    assertFalse(story.hasPrevView)

    val pp = new xml.PrettyPrinter(2, 2)
    assertEquals(pp.format(html1), pp.format(story.view))

    story.forward()

    val html2 =
      <div style="color:green">
        {para1}{para2}
      </div>

    assertFalse(story.hasNextView)
    assertTrue(story.hasPrevView)
    assertEquals(pp.format(html2), pp.format(story.view))

    story.back()
    assertTrue(story.hasNextView)
    assertFalse(story.hasPrevView)
    assertEquals(pp.format(html1), pp.format(story.view))
  }

  @Test
  def test4 {
    val para1 =
      <p >
        Para 1
      </p>

    val para2 =
      <p >
        Para 2
      </p>

    val pg1 = IncrPage(
      style="color:green",
      Para(para1),
      Para(para2)
    )

    val pgHhtml =
      <div style="color:green; font-size=18px">
        Page 1
      </div>
    val pg2 = Page(pgHhtml)


    val story = Story(pg1, pg2)

    val html1 =
      <div style="color:green">
        {para1}
      </div>

    assertTrue(story.hasNextView)
    assertFalse(story.hasPrevView)

    val pp = new xml.PrettyPrinter(2, 2)
    assertEquals(pp.format(html1), pp.format(story.view))

    story.forward()

    val html2 =
      <div style="color:green">
        {para1}{para2}
      </div>

    assertTrue(story.hasNextView)
    assertTrue(story.hasPrevView)
    assertEquals(pp.format(html2), pp.format(story.view))

    story.forward()
    assertFalse(story.hasNextView)
    assertTrue(story.hasPrevView)
    assertEquals(pp.format(pgHhtml), pp.format(story.view))

    story.back()
    assertTrue(story.hasNextView)
    assertTrue(story.hasPrevView)
    assertEquals(pp.format(html2), pp.format(story.view))

    story.back()
    assertTrue(story.hasNextView)
    assertFalse(story.hasPrevView)
    assertEquals(pp.format(html1), pp.format(story.view))
  }
}
