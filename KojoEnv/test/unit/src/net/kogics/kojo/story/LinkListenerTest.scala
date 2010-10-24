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

class LinkListenerTest extends KojoTestBase {

  val ll = new LinkListener(StoryTeller.instance)
  
  @Test
  def test1 {
    assertEquals((2,1), ll.location("http://localpage/2"))
  }

  @Test
  def test2 {
    assertEquals((2,3), ll.location("http://LOCALPAGE/2#3"))
  }

  @Test
  def test3 {
    assertEquals((12,11), ll.location("http://localpage/12#11"))
  }

  @Test
  def test4 {
    try {
      ll.location("http://localpage/a#11")
      fail("Invalid location not detected")
    }
    catch {
      case ex: IllegalArgumentException =>
        assertTrue(true)
    }
  }

  @Test
  def test5 {
    try {
      ll.location("http://localpage/5#x")
      fail("Invalid location not detected")
    }
    catch {
      case ex: IllegalArgumentException =>
        assertTrue(true)
    }
  }
}
