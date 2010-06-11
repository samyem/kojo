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
package staging

import org.junit.Test
import org.junit.Assert._

import net.kogics.kojo.util._

// cargo coding off CodePaneTest
class ColorTest extends StagingTestBase {

  @Test
  // lalit sez: if we have more than five tests, we run out of heap space - maybe
  // a leak in the Scala interpreter/compiler subsystem. So we run (mostly)
  // everything in one test
  def test1 = {
  //W
  //W==Color==
  //W
  //WA color value can be created by calling the method
  //W
  //W{{{
  //Wcolor( ... )
    Tester(
      "import Staging._ ; println(color(22, 13, 75))",
      Some("java.awt.Color[r=22,g=13,b=75]import Staging._")
    )

  //W}}}
  //W
  //Wwhich yields an instance of `java.awt.Color`.  The method accepts different
  //Wsets of arguments depending on color mode.
  //W
  //W{{{
  //WcolorMode(GRAY(lim))
  //Wcolor(arg)
    Tester(
      "import Staging._ ; colorMode(GRAY(255)) ; println(color(22))",
      Some("java.awt.Color[r=22,g=22,b=22]import Staging._")
    )
    Tester(
      "import Staging._ ; colorMode(GRAY(255)) ; println(color(.1))",
      Some("java.awt.Color[r=26,g=26,b=26]import Staging._")
    )

  //W}}}
  //W
  //Wcreates a grayscale color with a whiteness value of `arg`.  If `arg` is an
  //W`Double` value, it is expected to be within the range 0.0 (black) -- 1.0
  //W(white).  If `arg` is an integer value, it is as if the method were called
  //Wwith
  //W
  //W{{{
  //Wcolor(norm(arg, 0, lim))
    Tester(
      "import Staging._ ; colorMode(GRAY(255)) ; println(color(norm(22, 0, 255)))",
      Some("java.awt.Color[r=22,g=22,b=22]import Staging._")
    )

  //W}}}
  //W
  //W{{{
  //WcolorMode(RGB(lim1, lim2, lim3))
  //Wcolor(intVal)
    Tester(
      "import Staging._ ; colorMode(RGB(255, 255, 255)) ; println(color(22))",
      Some("java.awt.Color[r=0,g=0,b=22]import Staging._")
    )

  //W}}}
  //W
  //Won the other hand creates a color with this absolute code.
  //W
  //W{{{
  //WcolorMode(GRAYA(lim1, lim2)) // GRAY + ALPHA
  //Wcolor(arg1, arg2)
    Tester(
      "import Staging._ ; colorMode(GRAYA(255, 255)) ; println(color(22, 22))",
      Some("java.awt.Color[r=22,g=22,b=22]import Staging._")
    )
    Tester(
      "import Staging._ ; colorMode(GRAYA(255, 255)) ; println(color(.1, .1))",
      Some("java.awt.Color[r=26,g=26,b=26]import Staging._")
    )

  //W}}}
  //W
  //Wcreates a grayscale color with a whiteness value of `arg1` and an opaqueness
  //Wof `arg2`.
  //W
  //W{{{
  //WcolorMode(RGB(lim1, lim2, lim3)) // RED/GREEN/BLUE
  //Wcolor(arg1, arg2, arg3)
    Tester(
      "import Staging._ ; colorMode(RGB(255, 255, 255)) ; println(color(22, 22, 22))",
      Some("java.awt.Color[r=22,g=22,b=22]import Staging._")
    )
    Tester(
      "import Staging._ ; colorMode(RGB(255, 255, 255)) ; println(color(.1, .1, .1))",
      Some("java.awt.Color[r=26,g=26,b=26]import Staging._")
    )

  //W}}}
  //W
  //Wcreates a color with red, green, blue components `arg1`, `arg2`, `arg3`.
  //W
  //W{{{
  //WcolorMode(RGBA(lim1, lim2, lim3, lim4)) // RED/GREEN/BLUE + ALPHA
  //Wcolor(arg1, arg2, arg3, arg4)
    Tester(
      "import Staging._ ; colorMode(RGBA(255, 255, 255, 255)) ; println(color(22, 22, 22, 22))",
      Some("java.awt.Color[r=22,g=22,b=22]import Staging._")
    )
    Tester(
      "import Staging._ ; colorMode(RGBA(255, 255, 255, 255)) ; println(color(.1, .1, .1, .1))",
      Some("java.awt.Color[r=26,g=26,b=26]import Staging._")
    )

  //W}}}
  //W
  //Wcreates a color with red, green, blue components `arg1`, `arg2`, `arg3` and
  //Walpha value `arg4`.
  //W
  //W{{{
  //WcolorMode(HSB(lim1, lim2, lim3)) // HUE/SATURATION/BRIGHTNESS
  //Wcolor(arg1, arg2, arg3)
    Tester(
      "import Staging._ ; colorMode(HSB(255, 255, 255)) ; println(color(22, 22, 22))",
      Some("java.awt.Color[r=22,g=21,b=20]import Staging._")
    )
    Tester(
      "import Staging._ ; colorMode(HSB(255, 255, 255)) ; println(color(.1, .1, .1))",
      Some("java.awt.Color[r=26,g=24,b=23]import Staging._")
    )

  //W}}}
  //W
  //Wcreates a color with hue, saturation, brightness components `arg1`, `arg2`,
  //W`arg3`.
  //W
  //W{{{
  //WcolorMode(HSBA(lim1, lim2, lim3, lim4)) // HUE/SATURATION/BRIGHTNESS + ALPHA
  //Wcolor(arg1, arg2, arg3, arg4)
    Tester(
      "import Staging._ ; colorMode(HSBA(255, 255, 255, 255)) ; println(color(22, 22, 22, 22))",
      Some("java.awt.Color[r=23,g=117,b=20]import Staging._")
    )
    Tester(
      "import Staging._ ; colorMode(HSBA(255, 255, 255, 255)) ; println(color(.1, .1, .1, .1))",
      Some("java.awt.Color[r=27,g=152,b=23]import Staging._")
    )

  //W}}}
  //W
  //Wcreates a color with hue, saturation, brightness components `arg1`, `arg2`,
  //W`arg3` and alpha value `arg4`.
  //W
  //WFinally,
  //W
  //W{{{
  //Wcolor(s)
    Tester(
      """import Staging._ ; println(color("#99ccDD"))""",
      Some("java.awt.Color[r=153,g=204,b=221]import Staging._")
    )

    Tester(
      """import Staging._ ; println(color("aliceblue"))""",
      Some("java.awt.Color[r=240,g=248,b=255]import Staging._")
    )

  //W}}}
  //W
  //Wwhere s is either
  //W
  //W    * "none",
  //W    * one of the names in this list: http://www.w3.org/TR/SVG/types.html#ColorKeywords, or
  //W    * a string with the format "#rrggbb" (in hexadecimal)
  //W
  //Wreturns the described color regardless of color mode.
  //W
  //WWhen drawing figures, the _fill_ color, which is used for the insides, and
  //Wthe _stroke_ color, which is used for the edges, can be set and unset.
  //W
  //WTo set the fill color, call `fill`.
  //W
  //W{{{
  //Wfill(color)
    Tester(
      """import Staging._ ; fill(color("#99ccDD"))""",
      Some("import Staging._")
    )
    assertEquals("java.awt.Color[r=153,g=204,b=221]", SpriteCanvas.instance.figure0.fillColor.toString)

  //W}}}
  //W
  //WTo unset the fill color, call `noFill`, or `fill` with a `null` argument.
  //W
  //W{{{
  //WnoFill
  //Wfill(null)
    Tester("import Staging._ ; fill(null)", Some("import Staging._"))
    assertNull(SpriteCanvas.instance.figure0.fillColor)

    Tester(
      """import Staging._ ; fill(color("#99ccDD")) ; noFill""",
      Some("import Staging._")
    )
    assertNull(SpriteCanvas.instance.figure0.fillColor)

  //W}}}
  }

  @Test
  def test2 = {
  //W
  //WTo set the stroke color, call `stroke`.
  //W
  //W{{{
  //Wstroke(color)
    Tester(
      """import Staging._ ; stroke(color("#99ccDD"))""",
      Some("import Staging._")
    )
    assertEquals("java.awt.Color[r=153,g=204,b=221]", SpriteCanvas.instance.figure0.lineColor.toString)

  //W}}}
  //W
  //WTo unset the stroke color, call `noStroke`, or `stroke` with a `null` argument.
  //W
  //W{{{
  //WnoStroke
  //Wstroke(null)
    Tester("import Staging._ ; stroke(null)", Some("import Staging._"))
    assertNull(SpriteCanvas.instance.figure0.lineColor)

    Tester(
      """import Staging._ ; stroke(color("#99ccDD")) ; noStroke""",
      Some("import Staging._")
    )
    assertNull(SpriteCanvas.instance.figure0.lineColor)

  //W}}}
  //W
  //WTo set the stroke width, call `strokeWidth`.
  //W
  //W{{{
  //WstrokeWidth(value)
    Tester("""import Staging._ ; stroke(red) ; strokeWidth(2)""", Some("import Staging._"))
    Tester("""import Staging._ ; strokeWidth(2)""", Some("import Staging._"))
    assertEquals(2.0, SpriteCanvas.instance.figure0.lineStroke.asInstanceOf[java.awt.BasicStroke].getLineWidth, 0.01)
    Tester("""import Staging._ ; strokeWidth(2.0)""", Some("import Staging._"))
    assertEquals(2.0, SpriteCanvas.instance.figure0.lineStroke.asInstanceOf[java.awt.BasicStroke].getLineWidth, 0.01)
    Tester("""import Staging._ ; strokeWidth(.2)""", Some("import Staging._"))

  //W}}}
  //W
  //WTo set the fill, stroke, and stroke width just for the extent of some lines
  //Wof code, use `withStyle`.
  //W
  //W{{{
  //WwithStyle(fillColor, strokeColor, strokeWidth) { ...code... }
    Tester("""import Staging._
             |
             |fill(green)
             |stroke(black)
             |strokeWidth(1.0)
             |withStyle(red, blue, 4) {/**/}""".stripMargin, Some("import Staging._"))
    assertEquals("java.awt.Color[r=0,g=255,b=0]", SpriteCanvas.instance.figure0.fillColor.toString)
    assertEquals("java.awt.Color[r=0,g=0,b=0]", SpriteCanvas.instance.figure0.lineColor.toString)
    assertEquals(1.0, SpriteCanvas.instance.figure0.lineStroke.asInstanceOf[java.awt.BasicStroke].getLineWidth, 0.01)

  //W}}}
  //W
  //WThe fill, stroke, and stroke width can also be saved with `saveStyle` and
  //Wlater restored with `restoreStyle` (the latter quietly fails if no style
  //Whas been saved yet).
  //W
  //W{{{
  //WsaveStyle
  //WrestoreStyle
    Tester("""import Staging._
             |
             |fill(green)
             |stroke(black)
             |strokeWidth(1.0)
             |saveStyle
             |fill(red)
             |stroke(blue)
             |strokeWidth(4)
             |restoreStyle""".stripMargin, Some("import Staging._"))
    assertEquals("java.awt.Color[r=0,g=255,b=0]", SpriteCanvas.instance.figure0.fillColor.toString)
    assertEquals("java.awt.Color[r=0,g=0,b=0]", SpriteCanvas.instance.figure0.lineColor.toString)
    assertEquals(1.0, SpriteCanvas.instance.figure0.lineStroke.asInstanceOf[java.awt.BasicStroke].getLineWidth, 0.01)

  //W}}}
  //W
  }
}

