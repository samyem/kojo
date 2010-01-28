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

object SampleCode {

  def get(e: java.awt.event.ActionEvent): String = {
    e.getActionCommand match {
      // TODO: need to read the case strings from Bundle.properties
      case "Square" => Square
      case "Circle" => Circle
      case "Turtle Mania" => TurtleMania
      case "Turning Squares" => TurningSquares
      case "Another Square Pattern" => DecmoSquares
      case "Inward Eyes" => InwardEyes
      case "Orange Flower" => Flower1
      case "Green Flower" => Flower2
      case "Red Fan" => Fan
      case "Ferris Wheel" => FerrisWheel
      case "Plant" => Plant
      case "Sun, Fence, and Flower" => SunFenceFlower
      case "Rangoli" => Rangoli
      case "Koch Snowflake" => Snowflake
      case "Tree" => Tree
      case "Polygon" => Polygon
      case "Parallelogram" => Parallelogram
    }
  }

  val Square = """
clear
repeat(4) {
    forward(100)
    right
}
"""

  val Circle = """
clear
setAnimationDelay(10)
setPenThickness(2)
setPenColor(green)
setFillColor(orange)
repeat(360) {
    forward(1)
    turn(1)
}
"""

  val TurtleMania = """
clear()

def runPattern() {

    import collection.mutable.ArrayBuffer
    import java.util.Random

    def pattern(turtle: Turtle, n: Int): Unit = {
        if (n < 2) return
        turtle.forward(n)
        turtle.right
        turtle.forward(n)
        turtle.right
        pattern(turtle, n-5)
    }

    turn(60)
    setAnimationDelay(2000)
    forward(400)

    val turtles = new ArrayBuffer[Turtle]

    val rand = new Random

    for (i <- 0 until 5) {
        for (j <- 0 until 5) {
            val turtle = newTurtle(-400 + j*200, 400 - i*200)
            turtles += turtle
            turtle.setAnimationDelay(500 + rand.nextInt(500))
            turtle.left
            pattern(turtle, 100-5*i)
        }
    }
}

// run the function that we just defined
runPattern()
"""

  val TurningSquares = """
clear()
setPenColor(blue)
  
def squareTurn(n:Int){
    repeat(4) {
        forward(n)
        right
    }
    turn(10)
}

def pattern(n: Int) {
    repeat(36) {
        squareTurn(n)
    }
}

pattern(100)
"""

  val DecmoSquares = """
clear()

def pattern(n:Int) {
    if (n <= 2) return
    forward(n+100)
    right
    forward(n)
    right
    forward(n)
    right
    pattern(n-2)
}

pattern(50)
"""

  val InwardEyes = """
clear
setAnimationDelay(10)

setPenColor(black)

def drawCircle(col:Color, step:Double, angle:Int){
    setFillColor(col)
    repeat(360) {
        forward(step)
        turn(angle)
    }
}
drawCircle(orange, 3, 1)
drawCircle(orange, 3, -1)
drawCircle(blue, 2, 1)
drawCircle(blue, 2, -1)
drawCircle(yellow, 1, 1)
drawCircle(yellow, 1, -1)
drawCircle(green, .50, 1)
drawCircle(green, .50, -1)
drawCircle(red, .25, 1)
drawCircle(red, .25, -1)
"""

  val Flower1 = """
clear()
jumpTo(0,100)
setAnimationDelay(100)
setPenColor(black)
setFillColor(orange)
repeat(4){
    right
    repeat(90){
        turn(-2)
        forward(3)
    }
}
"""

  val Flower2 = """
clear()
jumpTo(0,100)
setAnimationDelay(20)
setPenColor(black)
setFillColor(green)
repeat(6){
    turn(-120)
    repeat(90){
        turn(-2)
        forward(3)
    }
}
"""

  val Fan = """
clear()
setFillColor(red)
repeat(4){
    turn(-30)
    forward(100)
    repeat(2){
        turn(-120)
        forward(100)
    }
}
"""

  val FerrisWheel = """
clear()
def flag(t: Turtle, c: Color, a: Double){
    t.setPenColor(c)
    t.setFillColor(c)
    t.turn(a)
    t.forward(150)
    repeat(3){
        t.right
        t.forward(50)
    }
    t.left
    t.forward(100)
}
val t1 = newTurtle(0,0)
val t2 = newTurtle(0,0)
val t3 = newTurtle(0,0)
val t4 = newTurtle(0,0)
val t5 = newTurtle(0,0)
val t6 = newTurtle(0,0)
val t7 = newTurtle(0,0)
val t8 = newTurtle(0,0)
val t9 = newTurtle(0,0)
val t10 = newTurtle(0,0)
val t11 = newTurtle(0,0)

flag(turtle0, red,0)
flag(t1, yellow,30)
flag(t2, blue,60)
flag(t3, green,90)
flag(t4, orange,120)
flag(t5, purple,150)
flag(t6, red,180)
flag(t7, yellow,210)
flag(t8, blue,240)
flag(t9, green,270)
flag(t10, orange,300)
flag(t11, purple,330)
"""

  val Plant = """
clear()
setAnimationDelay(20)
setPenThickness(4)

// Flower
setFillColor(red)
repeat(4){
    turn(90)
    repeat(45){
        turn(-4)
        forward(4)}
}

// Upper part of stem
turn(135)
setFillColor(null)
setPenColor(green)
repeat(30){
    turn(1)
    forward(10)
}

// First leaf
setFillColor(green)
repeat(2){
    turn(120)
    repeat(30){
        turn(2)
        forward(6)
    }
}

// Second leaf
repeat(2){
    turn(-120)
    repeat(30){
        turn(-2)
        forward(6)
    }
}

// Lower part of stem
setFillColor(null)
setPenColor(green)
repeat(20){
    turn(1)
    forward(10)
}

// Pot
setPenColor(brown)
setFillColor(brown)
turn(85)
forward(75)
turn(-110)
forward(150)
turn(-70)
forward(50)
turn(-70)
forward(150)
turn(-110)
forward(75)
"""
  val SunFenceFlower = """
clear()

// Sun
jumpTo(-400,200)
setAnimationDelay(20)
setPenColor(yellow)
setFillColor(yellow)
repeat(18) {
    right
    forward(75)
    turn(180)
    forward(75)
    right
    repeat(10) {
        turn(2)
        forward(2)}
}

// Flower Pot
jumpTo(200,100)
setPenColor(red)
setPenThickness(4)

// Flower
setFillColor(red)
repeat(4){
    turn(90)
    repeat(45){
        turn(-4)
        forward(1)}
}

// Upper part of stem
turn(135)
setFillColor(null)
setPenColor(green)
repeat(30){
    turn(1)
    forward(3)
}

// First leaf
setFillColor(green)
repeat(2){
    turn(120)
    repeat(30){
        turn(2)
        forward(2)
    }
}

// Second leaf
repeat(2){
    turn(-120)
    repeat(30){
        turn(-2)
        forward(2)
    }
}

// Lower part of stem
setFillColor(null)
setPenColor(green)
repeat(20){
    turn(1)
    forward(3)
}

// Pot
setPenColor(brown)
setFillColor(brown)
turn(85)
forward(25)
turn(-110)
forward(50)
turn(-70)
forward(15)
turn(-70)
forward(50)
turn(-110)
forward(25)

// Fence
def post(t: Turtle){
    t.setPenColor(black)
    t.setFillColor(white)
    t.setPenThickness(4)
    t.forward(150)
    t.turn(-30)
    t.forward(25)
    t.turn(-120)
    t.forward(25)
    t.turn(-30)
    t.forward(150)
    t.right
    t.forward(25)
}

val t0= newTurtle (0,-100)
val t1 = newTurtle(100,-100)
val t2 = newTurtle(200,-100)
val t3 = newTurtle(300,-100)
val t4 = newTurtle(400,-100)
val t5 = newTurtle(500,-100)
val t6 = newTurtle(600,-100)
val t7 = newTurtle(-100,-100)
val t8 = newTurtle(-200,-100)
val t9 = newTurtle(-300,-100)
val t10 = newTurtle(-400,-100)
val t11 = newTurtle(-500,-100)
val t12 = newTurtle(-550,-70)
val t13 = newTurtle(-550,-10)

post(t0)
post(t1)
post(t2)
post(t3)
post(t4)
post(t5)
post(t6)
post(t7)
post(t8)
post(t9)
post(t10)
post(t11)

def rail(tt: Turtle)
{
    tt.setPenColor(black)
    tt.setFillColor(white)
    tt.setPenThickness(4)
    tt.right()
    tt.forward(1200)
    tt.turn(90)
    tt.forward(25)
    tt.turn(90)
    tt.forward(1200)
    tt.turn(90)
    tt.forward(25)
}

rail(t12)
rail(t13)
"""

  val Rangoli = """
clear()

val t1=newTurtle(-600,-150)
val t2=newTurtle(-600, 150)

def border(t: Turtle, a: Double) {
    t.setAnimationDelay(100)
    t.setPenColor(black)
    t.right
    t.forward(1200)
    repeat(15){
        t.setFillColor(red)
        t.turn(a)
        t.forward(40)
        t.turn(a)
        t.forward(40)
        t.turn(a)

        t.setFillColor(blue)
        t.turn(a)
        t.forward(40)
        t.turn(a)
        t.forward(40)
        t.turn(a)
    }
}

border(t1,120)
border(t2,-120)


jumpTo(-50,100)
setAnimationDelay(20)
setPenColor(black)
setFillColor(green)
repeat(6){
    turn(-120)
    repeat(90){
        turn(-2)
        forward(2)
    }
}

val t3=newTurtle(-300,100)
val t4=newTurtle(-400,0)
val t5=newTurtle(-500,100)
val t6=newTurtle(-600,0)

val t7=newTurtle(200,100)
val t8=newTurtle(300,0)
val t9=newTurtle(400,100)
val t10=newTurtle(500,0)

def flower(tt:Turtle, c:Color) {
    tt.setAnimationDelay(20)
    tt.setPenColor(black)
    tt.setFillColor(c)
    repeat(4){
        tt.right
        repeat(90){
            tt.turn(-2)
            tt.forward(2)
        }
    }
}

flower(t3, orange)
flower(t4, yellow)
flower(t5, red)
flower(t6, purple)

flower(t7, orange)
flower(t8, yellow)
flower(t9, red)
flower(t10, purple)

turtle0.invisible
t1.invisible
t2.invisible
t3.invisible
t4.invisible
t5.invisible
t6.invisible
t7.invisible
t8.invisible
t9.invisible
t10.invisible
"""

  val Snowflake = """
def line(count: Int, length: Int) {
    if (count == 1) forward(length)
    else {
        line(count-1, length)
        left(60)
        line(count-1, length)
        right(120)
        line(count-1, length)
        left(60)
        line(count-1, length)
    }
}

def koch(count: Int, length: Int) {
    right(30)
    line(count, length)
    right(120)
    line(count, length)
    right(120)
    line(count, length)
}

clear()
invisible()
setPenThickness(1)
setPenColor(new Color(128, 128, 128))
setFillColor(new Color(0xC9C0BB))
setAnimationDelay(10)
penUp
back(100)
left
forward(150)
right
penDown
koch(5, 5)
  """

  val Tree = """
def tree(distance: Double) {
    if (distance > 4) {
        setPenThickness(distance/7)
        setPenColor(new Color(distance.toInt, Math.abs(255-distance*3).toInt, 125))
        forward(distance)
        right(25)
        tree(distance*0.8-2)
        left(45)
        tree(distance-10)
        right(20)
        back(distance)
    }
}

clear()
invisible
setAnimationDelay(10)
penUp
back(200)
penDown
tree(90)
  """

  val Polygon = """
clear()
def regularPoly(n: Int, size: Int) {
    repeat(n) {
        forward(size)
        left(360.0/n)
    }
}

regularPoly(5, 100)
invisible
val p = pathToPolygon()
p.showAngles()
jumpTo(100, 75)
write("Drag the vertices\naround to play\nwith the Shape.")
  """

  val Parallelogram = """
clear()
repeat(4) {
    forward(200)
    right()
}
invisible
val p = pathToParallelogram()
p.showAngles()
p.showLengths()
jumpTo(-200, 130)
write("Drag the vertices\naround to play\nwith the Shape.")
  """

val sinThetaAnimation = """
def d2r(a: Double) = a * Math.Pi/180

def sineFn(offset: Int, scale: Double) {
    for (i <- 0 to 359) {
        Shape.point(offset+i, scale * Math.sin(d2r(i)))
    }
}

val radius = 50
var theta = 0
clear()
Shape.setPenColor(green)
Shape.setPenThickness(2)
Shape.circle(0,0,radius)
sineFn(2*radius, radius)

Shape.setPenColor(new Color(64,64,64))
Shape.setPenThickness(1)
Shape.line(0,0,radius, 0)

Shape.animationStep {
    Shape.aclear()
    Shape.setPenColor(new Color(64,64,64))
    Shape.setPenThickness(1)
    Shape.line(0,0,radius * Math.cos(d2r(theta)), radius * Math.sin(d2r(theta)))

    Shape.setPenColor(red)
    Shape.setPenThickness(5)
    Shape.point(radius * Math.cos(d2r(theta)), radius * Math.sin(d2r(theta)))
    Shape.point(2*radius + (theta%360), radius * Math.sin(d2r(theta)))

    theta += 1
}

"""
}
