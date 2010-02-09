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

class TurtleTest extends KojoTestBase {

  val turtle = new Turtle(SpriteCanvas.instance, "/images/turtle32.png")

  @Before
  def setUp: Unit = {
    turtle.init()
    val latch = listenToTurtle
    turtle.setAnimationDelay(0)
    latch.await
  }

  @After
  def tearDown: Unit = {
  }

  @Test
  def testForward1 {
    val latch = listenToTurtle
    turtle.forward(100)
    latch.await
    val p = turtle.position

    assertEquals(0, p.x, 0.001)
    assertEquals(100, p.y, 0.001)
  }

  @Test
  def testForward2 {
    var latch = listenToTurtle
    turtle.turn(-90)
    latch.await
    latch = listenToTurtle
    turtle.forward(100)
    latch.await
    val p = turtle.position

    assertEquals(100, p.x, 0.001)
    assertEquals(0, p.y, 0.001)
  }

  @Test
  def testMotion1 {
    var latch = listenToTurtle
    turtle.turn(45)
    latch.await
    latch = listenToTurtle
    turtle.forward(100)
    latch.await
    val p = turtle.position

    val l = Math.sqrt(100 * 100 / 2)

    assertEquals(-l, p.x, 0.001)
    assertEquals(l, p.y, 0.001)
  }

  @Test
  def testMotion2 {
    var latch = listenToTurtle
    turtle.moveTo(-100, -100)
    latch.await

    latch = listenToTurtle
    turtle.turn(-45)
    latch.await

    latch = listenToTurtle
    turtle.forward(150)
    latch.await
    val p = turtle.position

    assertEquals(-250, p.x, 0.001)
    assertEquals(-100, p.y, 0.001)
  }

  @Test
  def testMotion3 {
    var latch = listenToTurtle
    turtle.moveTo(-100, -100)
    latch.await
    turtle.resetRotation()

    latch = listenToTurtle
    turtle.turn(-45)
    latch.await

    latch = listenToTurtle
    turtle.forward(Math.sqrt(2 * 100 * 100))
    latch.await
    val p = turtle.position

    assertEquals(0, p.x, 0.001)
    assertEquals(0, p.y, 0.001)
  }

  @Test
  def testTowards1 {
    val latch = listenToTurtle
    turtle.towards(100, 100)
    latch.await
    assertEquals(45, turtle.thetaDegrees, 0.001)
  }

  @Test
  def testTowards2 {
    val latch = listenToTurtle
    turtle.towards(-100, 100)
    latch.await
    assertEquals(135, turtle.thetaDegrees, 0.001)
  }

  @Test
  def testTowards3 {
    val latch = listenToTurtle
    turtle.towards(-100, -100)
    latch.await
    assertEquals(225, turtle.thetaDegrees, 0.001)
  }

  @Test
  def testTowards4 {
    val latch = listenToTurtle
    turtle.towards(100, -100)
    latch.await
    assertEquals(315, turtle.thetaDegrees, 0.001)
  }

  @Test
  def testTowardsRightLeft {
    var latch = listenToTurtle
    turtle.jumpTo(100, 0)
    latch.await
    latch = listenToTurtle
    turtle.towards(0, 0)
    latch.await
    assertEquals(180, turtle.thetaDegrees, 0.001)
  }

  @Test
  def testTowardsRightRight {
    var latch = listenToTurtle
    turtle.jumpTo(100, 0)
    latch.await
    latch = listenToTurtle
    turtle.towards(200, 0)
    latch.await
    assertEquals(0, turtle.thetaDegrees, 0.001)
  }

  @Test
  def testTowardsLeftRight {
    var latch = listenToTurtle
    turtle.jumpTo(-100, 0)
    latch.await
    latch = listenToTurtle
    turtle.towards(0, 0)
    latch.await
    assertEquals(0, turtle.thetaDegrees, 0.001)
  }

  @Test
  def testTowardsLeftLeft {
    var latch = listenToTurtle
    turtle.jumpTo(-100, 0)
    latch.await
    latch = listenToTurtle
    turtle.towards(-200, 0)
    latch.await
    assertEquals(180, turtle.thetaDegrees, 0.001)
  }

  @Test
  def testTowardsTopBottom {
    var latch = listenToTurtle
    turtle.jumpTo(0, 100)
    latch.await
    latch = listenToTurtle
    turtle.towards(0, 0)
    latch.await
    assertEquals(270, turtle.thetaDegrees, 0.001)
  }

  @Test
  def testTowardsTopTop {
    var latch = listenToTurtle
    turtle.jumpTo(0, 100)
    latch.await
    latch = listenToTurtle
    turtle.towards(0, 200)
    latch.await
    assertEquals(90, turtle.thetaDegrees, 0.001)
  }

  @Test
  def testTowardsBottomTop {
    var latch = listenToTurtle
    turtle.jumpTo(0, -100)
    latch.await
    latch = listenToTurtle
    turtle.towards(0, 0)
    latch.await
    assertEquals(90, turtle.thetaDegrees, 0.001)
  }

  @Test
  def testTowardsBottomBottom {
    var latch = listenToTurtle
    turtle.jumpTo(0, -100)
    latch.await
    latch = listenToTurtle
    turtle.towards(0, -200)
    latch.await
    assertEquals(270, turtle.thetaDegrees, 0.001)
  }

  def listenToTurtle: java.util.concurrent.CountDownLatch = {
    val latch = new java.util.concurrent.CountDownLatch(1)

    turtle.setTurtleListener(new AbstractTurtleListener {
        override def commandDone(cmd: Command) {
          latch.countDown
        }

      })
    latch
  }

  @Test
  def testTurn {
    val latch = listenToTurtle
    val theta0 = turtle.heading
    val turnSize = 30
    turtle.turn(turnSize)
    latch.await
    val theta1 = turtle.heading
    doublesEqual(theta1, theta0 + turnSize, 0.001)
  }

  @Test
  def testTurn2 {
    val latch = listenToTurtle
    val theta0 = turtle.heading
    // failing test from earlier scalacheck run. but works here??
    val turnSize = 2147483647
    turtle.turn(turnSize)
    latch.await
    val theta1 = turtle.heading
    val e0 = theta0 + turnSize
    val expected = {
      if (e0 < 0) 360 + e0 % 360
      else if (e0 > 360)  e0 % 360
      else e0
    }
    doublesEqual(expected, theta1, 0.001)
  }

  @Test
  def testManyForwards {
    val propForward = forAll { stepSize: Int =>
      val pos0 = turtle.position
      val latch = listenToTurtle
      turtle.forward(stepSize)
      latch.await
      val pos1 = turtle.position
      (doublesEqual(pos0.x, pos1.x, 0.001)
       && doublesEqual(pos0.y + stepSize, pos1.y, 0.001))
    }
    assertTrue(SCTest.check(propForward).passed)
  }

  @Test
  def testManyTurns {
    val propTurn = forAll { turnSize: Int =>
      val theta0 = turtle.heading
      val latch = listenToTurtle
      turtle.turn(turnSize)
      latch.await
      val theta1 = turtle.heading
      val e0 = theta0 + turnSize
      val expected = {
        if (e0 < 0) 360 + e0 % 360
        else if (e0 > 360)  e0 % 360
        else e0
      }
      doublesEqual(expected, theta1, 0.001)
    }
    assertTrue(SCTest.check(propTurn).passed)
  }

  @Test
  def testManyTowardsQ1 {
    val propTowards = forAll { n: Double =>
      val latch = listenToTurtle
      val x = Math.abs(n)
      val y = x+10
      turtle.towards(x, y)
      latch.await
      doublesEqual(Math.atan(y/x), turtle.thetaRadians, 0.001)
    }
    assertTrue(SCTest.check(propTowards).passed)
  }

  @Test
  def testManyTowardsQ2 {
    val propTowards = forAll { n: Double =>
      val latch = listenToTurtle
      val x = -Math.abs(n)
      val y = Math.abs(n+20)
      turtle.towards(x, y)
      latch.await
      doublesEqual(Math.Pi + Math.atan(y/x), turtle.thetaRadians, 0.001)
    }
    assertTrue(SCTest.check(propTowards).passed)
  }

  @Test
  def testManyTowardsQ3 {
    val propTowards = forAll { n: Double =>
      val latch = listenToTurtle
      val x = -Math.abs(n) - 1
      val y = x - 30
      turtle.towards(x, y)
      latch.await
      doublesEqual(Math.Pi + Math.atan(y/x), turtle.thetaRadians, 0.001)
    }
    assertTrue(SCTest.check(propTowards).passed)
  }

  @Test
  def testManyTowardsQ4 {
    val propTowards = forAll { n: Double =>
      val latch = listenToTurtle
      val x = Math.abs(n)
      val y = -x - 10
      turtle.towards(x, y)
      latch.await
      doublesEqual(2*Math.Pi + Math.atan(y/x), turtle.thetaRadians, 0.001)
    }
    assertTrue(SCTest.check(propTowards).passed)
  }

  @Test
  def testManyMoveTos {
    val propMoveTo = forAll { n: Double =>
      val x = n
      val y = n+15
      val latch = listenToTurtle
      turtle.moveTo(x, y)
      latch.await
      val pos1 = turtle.position
      (doublesEqual(x, pos1.x, 0.001)
       && doublesEqual(y, pos1.y, 0.001))
    }
    assertTrue(SCTest.check(propMoveTo).passed)
  }
}
