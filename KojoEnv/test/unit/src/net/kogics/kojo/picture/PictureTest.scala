package net.kogics.kojo
package picture

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.junit.ShouldMatchersForJUnit._
import util.Utils._

@RunWith(classOf[JUnitRunner])
class PictureTest extends KojoTestBase with FunSuite with xscala.RepeatCommands {
  
  val size = 50
  val w = 51
  val h = 50
  val bx = -1
  
  def testPic = Pic { t =>
    import t._
    invisible()
    setAnimationDelay(0)
    repeat(4) {
      forward(size)
      right
    }
  }
  
  def testHpic3(a1: Double, a2: Double) = HPics(
    testPic, 
    testPic, 
    rot(a1) -> HPics(
      testPic, 
      testPic,
      rot(a2) -> HPics(testPic, testPic)
    ),
    testPic
  )

  test("picture bounds") {
    val p = testPic
    p.show()
    val b = p.bounds
    b.x should equal(bx)
    b.y should equal(0)
    b.width should equal(w)
    b.height should equal(h)
  }  
  
  test("picture translation") {
    val p = trans(50, 0) -> testPic
    p.show()
    val b = p.bounds
    b.x should equal(50 + bx)
  }  

  test("picture scaling") {
    val p = scale(2) -> testPic
    p.show()
    val b = p.bounds
    b.x should equal(bx * 2)
    b.width should equal(w * 2)
    b.height should equal(h * 2)
  }  

  test("picture scaling after translation") {
    val p = trans(50, 0) * scale(2) -> testPic
    p.show()
    val b = p.bounds
    b.x should equal(50 + 2 * bx)
    b.width should equal(w * 2)
    b.height should equal(h * 2)
  }

  test("picture translation after scaling") {
    val p = scale(2) * trans(50, 0) -> testPic
    p.show()
    val b = p.bounds
    b.x should equal(50*2 + 2 * bx)
    b.width should equal(w * 2)
    b.height should equal(h * 2)
  }
  
  test("3-hpics hp3 bounds") {
    val a1 = 30.0
    val a2 = 40.0
    val p = testHpic3(a1, a2)
    p.show()

    val hp2 = p.pics(2).asInstanceOf[Rot].tpic.asInstanceOf[HPics]
    val hp3 = hp2.pics(2).asInstanceOf[Rot].tpic.asInstanceOf[HPics]

    val b3 = hp3.bounds
    doublesEqual(b3.x, size * 2 - math.cos((90-a2).toRadians) * h, 0.0001) should be (true)
    doublesEqual(b3.width, math.cos((90-a2).toRadians) * size + math.cos(a2.toRadians) * size*2, 0.0001) should be (true)
    doublesEqual(b3.height, math.cos(a2.toRadians) * size + math.sin(a2.toRadians) * size*2, 0.0001) should be (true)
  }

  test("3-hpics hp2 bounds") {
    val a1 = 30.0
    val a2 = 40.0
    val p = testHpic3(a1, a2)
    p.show()

    val hp2 = p.pics(2).asInstanceOf[Rot].tpic.asInstanceOf[HPics]
    val hp3 = hp2.pics(2).asInstanceOf[Rot].tpic.asInstanceOf[HPics]

    val b3 = hp3.bounds
    val b2 = hp2.bounds
    doublesEqual(b2.x, 100 - b3.height * math.cos((90-a1).toRadians), 0.0001) should be (true)
    doublesEqual(b2.width, (b3.width - math.cos((90-a2).toRadians) * size + size*2) * math.cos(a1.toRadians) + b3.height * math.cos((90-a1).toRadians), 0.0001) should be (true)
    doublesEqual(b2.height, b3.height * math.sin((90-a1).toRadians) + (b3.width - math.cos((90-a2).toRadians) * size + size*2) * math.cos((90-a1).toRadians), 0.0001) should be (true)
  }
  
  test("3-hpics hp3 bounds #2") {
    val a1 = -120.0
    val a2 = 60.0
    val p = testHpic3(a1, a2)
    p.show()

    val hp2 = p.pics(2).asInstanceOf[Rot].tpic.asInstanceOf[HPics]
    val hp3 = hp2.pics(2).asInstanceOf[Rot].tpic.asInstanceOf[HPics]

    val b3 = hp3.bounds
    doublesEqual(b3.x, size * 2 - math.cos((90-a2).toRadians) * h, 0.0001) should be (true)
    doublesEqual(b3.width, math.cos((90-a2).toRadians) * size + math.cos(a2.toRadians) * size*2, 0.0001) should be (true)
    doublesEqual(b3.height, math.cos(a2.toRadians) * size + math.sin(a2.toRadians) * size*2, 0.0001) should be (true)
  }

  test("3-hpics hp2 bounds #2") {
    val a1 = -120.0
    val a2 = 60.0
    val p = testHpic3(a1, a2)
    p.show()

    val hp2 = p.pics(2).asInstanceOf[Rot].tpic.asInstanceOf[HPics]
    val hp3 = hp2.pics(2).asInstanceOf[Rot].tpic.asInstanceOf[HPics]

    val b3 = hp3.bounds
    val b2 = hp2.bounds
//    doublesEqual(b2.x, 100 - b3.height * math.cos((90-a1).toRadians), 0.0001) should be (true)
//    doublesEqual(b2.width, (b3.width - math.cos((90-a2).toRadians) * size + size*2) * math.cos(a1.toRadians) + b3.height * math.cos((90-a1).toRadians), 0.0001) should be (true)
//    doublesEqual(b2.height, b3.height * math.sin((90-a1).toRadians) + (b3.width - math.cos((90-a2).toRadians) * size + size*2) * math.cos((90-a1).toRadians), 0.0001) should be (true)
  }
  
}
