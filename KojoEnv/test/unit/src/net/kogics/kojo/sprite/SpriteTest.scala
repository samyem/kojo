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
package net.kogics.kojo.sprite

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._

import org.scalacheck.{Test => SCTest}
import org.scalacheck.Prop.forAll

import edu.umd.cs.piccolo.PCanvas

import net.kogics.kojo.util.Utils._

class SpriteTest {

  val sprite = new Geometer(SpriteCanvas.instance, "/images/turtle32.png")

  @Before
  def setUp: Unit = {
    sprite.init()
    val latch = listenToSprite
    sprite.setAnimationDelay(0)
    latch.await
  }

  @After
  def tearDown: Unit = {
  }

  @Test
  def testForward1 {
    val latch = listenToSprite
    sprite.forward(100)
    latch.await
    val (x,y) = sprite.position

    assertEquals(0, x, 0.001)
    assertEquals(100, y, 0.001)
  }

  @Test
  def testForward2 {
    var latch = listenToSprite
    sprite.turn(-90)
    latch.await
    latch = listenToSprite
    sprite.forward(100)
    latch.await
    val (x,y) = sprite.position

    assertEquals(100, x, 0.001)
    assertEquals(0, y, 0.001)
  }

  @Test
  def testMotion1 {
    var latch = listenToSprite
    sprite.turn(45)
    latch.await
    latch = listenToSprite
    sprite.forward(100)
    latch.await
    val (x,y) = sprite.position

    val l = Math.sqrt(100 * 100 / 2)

    assertEquals(-l, x, 0.001)
    assertEquals(l, y, 0.001)
  }

  @Test
  def testMotion2 {
    var latch = listenToSprite
    sprite.moveTo(-100, -100)
    latch.await

    latch = listenToSprite
    sprite.turn(-45)
    latch.await

    latch = listenToSprite
    sprite.forward(150)
    latch.await
    val (x,y) = sprite.position

    assertEquals(-250, x, 0.001)
    assertEquals(-100, y, 0.001)
  }

  @Test
  def testMotion3 {
    var latch = listenToSprite
    sprite.moveTo(-100, -100)
    latch.await
    sprite.resetRotation()

    latch = listenToSprite
    sprite.turn(-45)
    latch.await

    latch = listenToSprite
    sprite.forward(Math.sqrt(2 * 100 * 100))
    latch.await
    val (x,y) = sprite.position

    assertEquals(0, x, 0.001)
    assertEquals(0, y, 0.001)
  }

  @Test
  def testTowards1 {
    val latch = listenToSprite
    sprite.towards(100, 100)
    latch.await
    assertEquals(45, sprite.thetaDegrees, 0.001)
  }

  @Test
  def testTowards2 {
    val latch = listenToSprite
    sprite.towards(-100, 100)
    latch.await
    assertEquals(135, sprite.thetaDegrees, 0.001)
  }

  @Test
  def testTowards3 {
    val latch = listenToSprite
    sprite.towards(-100, -100)
    latch.await
    assertEquals(225, sprite.thetaDegrees, 0.001)
  }

  @Test
  def testTowards4 {
    val latch = listenToSprite
    sprite.towards(100, -100)
    latch.await
    assertEquals(315, sprite.thetaDegrees, 0.001)
  }

  def listenToSprite: java.util.concurrent.CountDownLatch = {
    val latch = new java.util.concurrent.CountDownLatch(1)

    sprite.setSpriteListener(new AbstractSpriteListener {
        override def commandDone(cmd: Command) {
          latch.countDown
        }

      })
    latch
  }

  @Test
  def testTurn {
    val latch = listenToSprite
    val theta0 = sprite.heading
    val turnSize = 30
    sprite.turn(turnSize)
    latch.await
    val theta1 = sprite.heading
    doublesEqual(theta1, theta0 + turnSize, 0.001)
  }

  /*
   @Test
   def testManyForwards {
   val propForward = forAll { stepSize: Int =>
   val pos0 = sprite.position
   val latch = listenToSprite
   sprite.forward(stepSize)
   latch.await
   val pos1 = sprite.position
   (doublesEqual(pos0._1, pos1._1, 0.001)
   && doublesEqual(pos0._2 + stepSize, pos1._2, 0.001))
   }
   assertTrue(SCTest.check(propForward).passed)
   }

   @Test
   def testManyTurns {
   val propTurn = forAll { turnSize: Int =>
   val theta0 = sprite.heading
   val latch = listenToSprite
   sprite.turn(turnSize)
   latch.await
   val theta1 = sprite.heading
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
   val latch = listenToSprite
   val x = Math.abs(n)
   val y = x+10
   sprite.towards(x, y)
   latch.await
   doublesEqual(Math.atan(y/x), sprite.thetaRadians, 0.001)
   }
   assertTrue(SCTest.check(propTowards).passed)
   }

   @Test
   def testManyTowardsQ2 {
   val propTowards = forAll { n: Double =>
   val latch = listenToSprite
   val x = -Math.abs(n)
   val y = Math.abs(n+20)
   sprite.towards(x, y)
   latch.await
   doublesEqual(Math.Pi + Math.atan(y/x), sprite.thetaRadians, 0.001)
   }
   assertTrue(SCTest.check(propTowards).passed)
   }

   @Test
   def testManyTowardsQ3 {
   val propTowards = forAll { n: Double =>
   val latch = listenToSprite
   val x = -Math.abs(n) - 1
   val y = x - 30
   sprite.towards(x, y)
   latch.await
   doublesEqual(Math.Pi + Math.atan(y/x), sprite.thetaRadians, 0.001)
   }
   assertTrue(SCTest.check(propTowards).passed)
   }

   @Test
   def testManyTowardsQ4 {
   val propTowards = forAll { n: Double =>
   val latch = listenToSprite
   val x = Math.abs(n)
   val y = -x - 10
   sprite.towards(x, y)
   latch.await
   doublesEqual(2*Math.Pi + Math.atan(y/x), sprite.thetaRadians, 0.001)
   }
   assertTrue(SCTest.check(propTowards).passed)
   }

   @Test
   def testManyMoveTos {
   val propMoveTo = forAll { n: Double =>
   val x = n
   val y = n+15
   val latch = listenToSprite
   sprite.moveTo(x, y)
   latch.await
   val pos1 = sprite.position
   (doublesEqual(x, pos1._1, 0.001)
   && doublesEqual(y, pos1._2, 0.001))
   }
   assertTrue(SCTest.check(propMoveTo).passed)
   }
   */
}
