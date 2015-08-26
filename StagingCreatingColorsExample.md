# Creating Colors #

Based on a Processing example called _Creating Colors (Homage to Albers)_.

[![](http://photos-f.ak.fbcdn.net/hphotos-ak-snc4/hs081.snc4/35412_133172746709553_100000504857742_292379_597795_s.jpg)](http://www.facebook.com/photo.php?pid=292379&id=100000504857742)

# Script #

```
import Staging._

reset
screenSize(200, -200)
val cm = rgbColors(255, 255, 255)
noStroke
val inside = cm(204, 102, 0) // or: val inside = namedColor("#CC6600")
val middle = cm(204, 153, 0) // or: val middle = namedColor("#CC9900")
val outside = cm(153, 51, 0) // or: val outside = namedColor("#993300")

fill(outside)
rectangle(O, screenExt)
fill(middle)
rectangle((40, 60), 120, 120)
fill(inside)
rectangle((60, 90), 80, 80)
```