# Hello Kojo #

This example creates a limited "font" where letters are defined by a series of strokes,
and prints a message by drawing those strokes at suitable offsets.

[![](http://photos-g.ak.fbcdn.net/hphotos-ak-ash2/hs041.ash2/35412_133172753376219_100000504857742_292380_6004414_s.jpg)](http://www.facebook.com/photo.php?pid=292380&id=100000504857742)

# Script #

```
import Staging._

reset
screenSize(100, 100)

stroke(color(255, 126, 33))

object Printer {
  val E: Seq[Point] = Seq((0, 0), (0, 20), (0, 20), (10, 20),
    (0, 10), (7, 10), (0, 0), (10, 0))
  val H: Seq[Point] = Seq((0, 0), (0, 20), (0, 10), (10, 10),
    (10, 0), (10, 20))
  val J: Seq[Point] = Seq((0, 5), (5, 0), (5, 0), (10, 5),
    (10, 5), (10, 20))
  val K: Seq[Point] = Seq((0, 0), (0, 20), (0, 10), (10, 20),
    (0, 10), (10, 0))
  val L: Seq[Point] = Seq((0, 0), (0, 20), (0, 0), (10, 0))
    val O: Seq[Point] = Seq(
    (5, 0), (0, 5),
    (0, 5), (0, 15),
    (0, 15), (5, 20),
    (5, 20), (10, 15),
    (10, 15), (10, 5),
    (10, 5), (5, 0)
  )

  var insert: Point = _

  def print1 (letter: Seq[Point]) {
    linesShape(letter map (insert + _))
    insert += point(16, 0)
  }

  def print (ins: Point, string: Seq[Point]*) {
    insert = ins
    string foreach print1
  }

  def run {
    print(screenMid + (-40, 20), H, E, L, L, O)
    print(screenMid + (-32, -10), K, O, J, O)
  }
}

Printer.run
```