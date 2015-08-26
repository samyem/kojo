# Color Wheel #

Draws a tinted or shaded color wheel.  Based on a Processing example by Rusty Robison.

The primaries are red, yellow, and blue. The
secondaries are green, purple, and orange. The
tertiaries are  yellow-orange, red-orange, red-purple,
blue-purple, blue-green, and yellow-green.

Create a shade or tint of the
subtractive color wheel using
the `shade` or `tint` function.

[![](http://photos-d.ak.fbcdn.net/hphotos-ak-snc4/hs101.snc4/35412_133172740042887_100000504857742_292378_1059508_s.jpg)](http://www.facebook.com/photo.php?pid=292378&id=100000504857742)

# Script #

```
import Staging._

reset
screenSize(200, 200)
background(grayColors(100)(50))
val cm = rgbColors(255, 255, 255)
noStroke

val segs      = 12
val steps     = 6
val radius    = 95.0
val segWidth  = radius / steps
val interval  = 360d / segs

val a =      1d / steps
val b = (1/1.5) / steps
val c = (1/2.0) / steps
val d = (1/2.5) / steps

val colors = Array.tabulate(steps+1){ step =>
    Array(
        cm(a*step, a*step, 0d), 
        cm(a*step, b*step, 0d), 
        cm(a*step, c*step, 0d), 
        cm(a*step, d*step, 0d), 
        cm(a*step, 0d,     0d), 
        cm(a*step, 0d,     c*step), 
        cm(a*step, 0d,     a*step), 
        cm(c*step, 0d,     a*step), 
        cm(0d,     0d,     a*step),
        cm(0d,     a*step, d*step), 
        cm(0d,     a*step, 0d), 
        cm(c*step, a*step, 0d)
    )
}

def tint (i: Int, p: Point) = {
    for (j <- steps until (0, -1)) {
        fill(colors(j)(i))
        val r = radius - segWidth*(steps-j)
        arc(p, r, r, interval*i, interval)
    }
}

def shade (i: Int, p: Point) = {
    for (j <- 0 until steps) {
        fill(colors(j)(i))
        val r = radius - segWidth*j
        arc(p, r, r, interval*i, interval)
    }
}

for (i <- 0 until segs)
    tint(i, screenMid) // or shade(i, screenMid)
```