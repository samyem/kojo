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
package turtle

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._

import org.scalacheck.{Test => SCTest}
import org.scalacheck.Prop.forAll

import edu.umd.cs.piccolo.PCanvas

import net.kogics.kojo.util.Utils._

class TurtleUndoTest extends KojoTestBase {

  val turtle = new Turtle(SpriteCanvas.instance, "/images/turtle32.png")

  @Before
  def setUp: Unit = {
    turtle.init()
    turtle.setAnimationDelay(0)
  }

  @After
  def tearDown: Unit = {
  }

  @Test
  def testForwardUndo1 {
    val s0 = turtle.state
    turtle.forward(100)
    turtle.undo()
    val s1 = turtle.state

    assertEquals(s0, s1)
  }

  @Test
  def testManyForwardsUndo {
    val propForwardUndo = forAll { stepSize: Int =>
      val s0 = turtle.state
      turtle.forward(stepSize)
      turtle.undo()
      val s1 = turtle.state

      s0 == s1
    }
    assertTrue(SCTest.check(propForwardUndo).passed)
  }

  @Test
  def testTurnUndo1 {
    val s0 = turtle.state
    turtle.turn(40)
    turtle.undo()
    val s1 = turtle.state

    assertEquals(s0, s1)
  }

  @Test
  def testManyTurnsUndo {
    val propTurn = forAll { turnSize: Int =>
      val s0 = turtle.state
      turtle.turn(turnSize)
      turtle.undo()
      val s1 = turtle.state

      s0 == s1
    }
    assertTrue(SCTest.check(propTurn).passed)
  }

  @Test
  def testColorfulCircleUndo {
    val random = new java.util.Random
    def randomColor = new java.awt.Color(random.nextInt(255), random.nextInt(255), random.nextInt(255))

    var states: List[SpriteState] = Nil
    states = turtle.state :: states

    for (idx <- 1 to 360) {
      turtle.setPenColor(randomColor)
      states = turtle.state :: states
      turtle.setPenThickness(idx)
      states = turtle.state :: states
      turtle.setFillColor(randomColor)
      states = turtle.state :: states
      turtle.forward(1)
      states = turtle.state :: states
      turtle.turn(1)
      states = turtle.state :: states
    }

    states = states.tail
    for (idx <- 1 to 360*5) {
      turtle.undo()
      assertEquals(states.head, turtle.state)
      states = states.tail
    }
  }

  @Test
  def testMoveToUndo1 {
    val s0 = turtle.state
    turtle.moveTo(100, 100)
    turtle.undo()
    val s1 = turtle.state

    assertEquals(s0, s1)
  }

  @Test
  def testManyMoveToUndo {
    val propForwardUndo = forAll { stepSize: Int =>
      val s0 = turtle.state
      turtle.moveTo(100, 100)
      turtle.undo()
      val s1 = turtle.state

      s0 == s1
    }
    assertTrue(SCTest.check(propForwardUndo).passed)
  }
}
