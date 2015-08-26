# Hue - Saturation - Brightness #

Based on three Processing examples.

**Hue** is the "kind" of color (red, yellow, green, etc).

**Saturation** is the "purity" of the color.  Less saturation means closer to gray.

**Brightness** is the lightness of the color.

Choose which of the classes to instantiate, and move the mouse up and down to change the selected quality within a strip.  Move across to change other strips (changes the hue in `SFiller` / `BFiller`).

[![](http://photos-a.ak.fbcdn.net/hphotos-ak-snc4/hs101.snc4/35412_133172756709552_100000504857742_292381_6433823_s.jpg)](http://www.facebook.com/photo.php?pid=292381&id=100000504857742)

# Script #

```
import Staging._

val barWidth =  5

reset
screenSize(360, 360)
background(black)
val cm = hsbColors(screenHeight, screenHeight, screenHeight)
noStroke

trait BarFiller {
  def draw(x: Double, y: Double) {
    val mx = constrain(x, 0, screenWidth - barWidth).toInt
    val my = constrain(y, 0, screenHeight).toInt
    val barX = (mx / barWidth) * barWidth
    fill(calcColor(barX, my))
    rectangle((barX, 0), barWidth, screenHeight)
  }
  def calcColor(barX: Int, my: Int): Color
}

class HFiller extends BarFiller {
  def calcColor(barX: Int, my: Int) = cm(my, 280, barX)
}

class SFiller extends BarFiller {
  def calcColor(barX: Int, my: Int) = cm(barX, my, 280)
}

class BFiller extends BarFiller {
  def calcColor(barX: Int, my: Int) = cm(barX, 280, my)
}

loop {
  (new HFiller).draw(mouseX, mouseY)
}
```