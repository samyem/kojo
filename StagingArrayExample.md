# Array #

This example renders mathematical functions that yield numbers in the
range 0.0 to 1.0 as grayscale pseudo-gradients.

Note that the y-axis is flipped to grow downwards.

[![](http://photos-e.ak.fbcdn.net/hphotos-ak-snc4/hs081.snc4/35412_133172723376222_100000504857742_292376_7894836_s.jpg)](http://www.facebook.com/photo.php?pid=292376&id=100000504857742)

# Script #

```
import Staging._

clear
val cm = grayColors(255)
screenSize(180, -100)
strokeWidth(1)
val stripHeight = screenHeight / 4d

val halfwave = Seq.tabulate(screenWidth){ i => math.toRadians(i) }

def render (level: Int)(fn: Double => Double) {
  val y1 = stripHeight * level
  val y2 = y1 + stripHeight
  (halfwave map fn zipWithIndex) foreach { case(e, i) =>
    val x = i.toDouble
    stroke(cm(e))
    line((x, y1), (x, y2))
  }
}

val f = { x: Double => 1 - x }

render (0) { sin }
render (1) { sin _ andThen f }
render (2) { cos _ andThen math.abs }
render (3) { cos _ andThen math.abs andThen f }
```