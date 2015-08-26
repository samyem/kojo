# Array 2D #

This example renders a binary mathematical function that yield numbers in the
range 0.0 to 1.0 as a grayscale polar pseudo-gradient.  The function in the
example is simply the distance from center as a fraction of maximum distance.

[![](http://photos-c.ak.fbcdn.net/hphotos-ak-snc4/hs081.snc4/35412_133172733376221_100000504857742_292377_5606316_s.jpg)](http://www.facebook.com/photo.php?pid=292377&id=100000504857742)

# Script #

```
import Staging._

reset
val cm = grayColors(255)
screenSize(200, 200)
background(black)

val maxDistance = dist(screenMid.x, screenMid.y, screenWidth, screenHeight)

val distances = Seq.tabulate(screenWidth, screenHeight){ case (x, y) =>
  dist(screenMid.x, screenMid.y, x, y) / maxDistance
}

for (x <- 0 until(screenWidth, 2) ; y <- 0 until(screenHeight, 2)) {
  stroke(cm(distances(x)(y)))
  dot(x+1, y+1)
}
```