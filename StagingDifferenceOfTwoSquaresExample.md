# Difference of two squares #

Shows two proofs that the difference of two squares a² - b² can be factorized as (a + b)(a - b).

[![](http://photos-e.ak.fbcdn.net/hphotos-ak-snc4/hs122.snc4/36445_134292503264244_100000504857742_298914_4412570_s.jpg)](http://www.facebook.com/photo.php?pid=298914&id=100000504857742)

# Script #

```
import Staging._

reset
val a = 280
val b = 150
screenSize(a + b + 30, a + 30)

def drawAxes {
  fill(black)
  stroke(black)
  strokeWidth(1.5)
  vector(O, (screenWidth, 0), 10)
  vector(O, (0, screenHeight), 10)

  val labelWidth = 40
  val markWidth =  8
  noFill
  strokeWidth(1)
  text("a", (-labelWidth, a))
  line((-markWidth/2, a), (markWidth/2, a))
  text("a - b", (-labelWidth, a - b))
  line((-markWidth/2, a - b), (markWidth/2, a - b))

  val labelHeight = 10
  val markHeight =  8
  text("a + b", (a + b, -labelHeight))
  line((a + b, -markHeight/2), (a + b, markHeight/2))
  text("a", (a, -labelHeight))
  line((a, -markHeight/2), (a, markHeight/2))
  text("b", (b, -labelHeight))
  line((b, -markHeight/2), (b, markHeight/2))
}

val leading = 40

drawAxes
noFill
stroke(black)
strokeWidth(1)
var column  = -300
var topline =    a

def glide(s: String) {
  var t = text(s, column, leading)
  repeat ((topline - leading) * 5) {
    t.translate(point(0, .2))
  }
  topline -= leading
}

glide("Is (a + b)(a - b) = a² - b²?")

val r0 = rectangle(O, (a + b, a - b))
glide("Draw a rectangle (a + b) × (a - b).")

quad(O, (a, 0), (b, a - b), (0, a - b))
glide("""Cut the rectangle into two parts, 
        |each of which has one side = a 
        |and one side = b""".stripMargin)
topline -= leading

glide("Flip one of parts over like this.")

r0.hide
interpolatePolygon(
  List((a, 0), (a + b, 0), (a + b, a - b), (b, a - b)),
  List((b, a - b), (b, a), (a, a), (a, 0)),
  20
)
fill(namedColor("silver"))
square((0, a - b), b)
glide("""The resulting figure is the
        |difference between two squares
        |a × a and b × b.
        |\u2234 (a + b)(a - b) = a² - b²""".stripMargin)



repeat(1000) { /* */ }


      
column  = screenWidth + 30
topline =                a

reset
stroke(black)
drawAxes
noFill
strokeWidth(1)

glide("Now, is a² - b² = (a + b)(a - b)?")

glide("(Well, obviously, but still.)")

val s0 = square(O, a)
glide("Draw a square a × a.")

rectangle(O, (a, a - b))
glide("Cut off a strip a × b.")

var r1 = rectangle((0, a - b), (a, a))
s0.hide
repeat (100) {
  r1.rotate(.9)
}
repeat (100) {
  r1.translate(2.15, -.65)
}
glide("Move it to this position...")

fill(namedColor("silver"))
square((a, a - b), b)
glide("Cut off a square b × b.")

glide("\u2234 a² - b² = (a + b)(a - b)")
```