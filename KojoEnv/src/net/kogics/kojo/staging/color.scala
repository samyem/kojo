/*
 * Copyright (C) 2010 Peter Lewerin <peter.lewerin@tele2.se>
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

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.nodes._
import edu.umd.cs.piccolo.util._
import edu.umd.cs.piccolo.event._

//import net.kogics.kojo.util.Utils

import javax.swing._

import core._

object ColorMode {
  type Color = java.awt.Color
  var mode: API.ColorModes = API.RGB(255, 255, 255)

  def apply(cm: API.ColorModes) { mode = cm }

  def color(v: Int) = {
    require(mode.isInstanceOf[API.GRAY] ||
            mode.isInstanceOf[API.RGB],
            "Color mode isn't GRAY or RGB")
    if (mode.isInstanceOf[API.GRAY]) {
      val vv = API.norm(v, 0, mode.asInstanceOf[API.GRAY].v).toFloat
      new Color(vv, vv, vv)
    } else {
      new Color(v)
    }
  }
  def color(v: Double) = {
    require(mode.isInstanceOf[API.GRAY], "Color mode isn't GRAY")
    val vv = v.toFloat
    new Color(vv, vv, vv)
  }

  def color(v: Int, a: Int) = {
    require(mode.isInstanceOf[API.GRAYA] ||
            mode.isInstanceOf[API.RGBA],
            "Color mode isn't GRAYA (gray with alpha) or RGBA")
    if (mode.isInstanceOf[API.GRAYA]) {
      val vv = API.norm(v, 0, mode.asInstanceOf[API.GRAYA].v).toFloat
      val aa = API.norm(a, 0, mode.asInstanceOf[API.GRAYA].a).toFloat
      new Color(vv, vv, vv, aa)
    } else {
      val aa = API.norm(a, 0, mode.asInstanceOf[API.RGBA].a).toFloat
      new Color(v | Math.lerp(0, 255, aa).toInt << 12, true)
    }
  }
  def color(v: Double, a: Double) = {
    require(v >= 0 && v <= 1, "Grayscale value off range")
    require(a >= 0 && a <= 1, "Alpha value off range")
    val vv = v.toFloat
    new Color(vv, vv, vv, a.toFloat)
  }

  def color(v1: Int, v2: Int, v3: Int) = {
    require(mode.isInstanceOf[API.RGB] ||
            mode.isInstanceOf[API.HSB],
            "Color mode isn't RGB or HSB")
    if (mode.isInstanceOf[API.RGB]) {
      val r = API.norm(v1, 0, mode.asInstanceOf[API.RGB].r).toFloat
      val g = API.norm(v2, 0, mode.asInstanceOf[API.RGB].g).toFloat
      val b = API.norm(v3, 0, mode.asInstanceOf[API.RGB].b).toFloat
      new Color(r, g, b)
    } else {
      val h = API.norm(v1, 0, mode.asInstanceOf[API.HSB].h).toFloat
      val s = API.norm(v2, 0, mode.asInstanceOf[API.HSB].s).toFloat
      val b = API.norm(v3, 0, mode.asInstanceOf[API.HSB].b).toFloat
      java.awt.Color.getHSBColor(h, s, b)
    }
  }
  def color(v1: Int, v2: Int, v3: Int, a: Int) = {
    require(mode.isInstanceOf[API.RGBA] ||
            mode.isInstanceOf[API.HSBA],
            "Color mode isn't RGBA or HSBA")
    if (mode.isInstanceOf[API.RGBA]) {
      val r = API.norm(v1, 0, mode.asInstanceOf[API.RGBA].r).toFloat
      val g = API.norm(v2, 0, mode.asInstanceOf[API.RGBA].g).toFloat
      val b = API.norm(v3, 0, mode.asInstanceOf[API.RGBA].b).toFloat
      val aa = API.norm(a, 0, mode.asInstanceOf[API.RGBA].a).toFloat
      new Color(r, g, b, aa)
    } else {
      //TODO transparency not working
      val h = API.norm(v1, 0, mode.asInstanceOf[API.HSBA].h).toFloat
      val s = API.norm(v2, 0, mode.asInstanceOf[API.HSBA].s).toFloat
      val b = API.norm(v3, 0, mode.asInstanceOf[API.HSBA].b).toFloat
      val aa = API.norm(a, 0, mode.asInstanceOf[API.HSBA].a).toFloat
      val c = java.awt.Color.getHSBColor(h, s, b)
      new Color(c.getRGB | Math.lerp(0, 255, aa).toInt << 12, true)
    }
  }

  def color(v1: Double, v2: Double, v3: Double) = {
    require(mode.isInstanceOf[API.RGB] ||
            mode.isInstanceOf[API.HSB],
            "Color mode isn't RGB or HSB")
    if (mode.isInstanceOf[API.RGB]) {
      val r = v1.toFloat
      val g = v2.toFloat
      val b = v3.toFloat
      new Color(r, g, b)
    } else {
      val h = v1.toFloat
      val s = v2.toFloat
      val b = v3.toFloat
      java.awt.Color.getHSBColor(h, s, b)
    }
  }
  def color(v1: Double, v2: Double, v3: Double, a: Double) = {
    require(mode.isInstanceOf[API.RGBA] ||
            mode.isInstanceOf[API.HSBA],
            "Color mode isn't RGBA or HSBA")
    if (mode.isInstanceOf[API.RGBA]) {
      val r = v1.toFloat
      val g = v2.toFloat
      val b = v3.toFloat
      val aa = a.toFloat
      new Color(r, g, b, aa)
    } else {
      val h = v1.toFloat
      val s = v2.toFloat
      val b = v3.toFloat
      val c = java.awt.Color.getHSBColor(h, s, b)
      new Color(c.getRGB | Math.lerp(0, 255, a).toInt << 12, true)
    }
  }
  def color(s: String): Color = s match {
    case ColorName(cc) => cc
    case "none"        => null
    case z             => java.awt.Color.decode(s)
  }
}

class RichColor (val c: java.awt.Color) {
  type Color = java.awt.Color
  def alpha = c.getAlpha
  def red = c.getRed
  def blue = c.getBlue
  def green = c.getGreen
  private def hsb =
    java.awt.Color.RGBtoHSB(c.getRed, c.getBlue, c.getGreen, null)
  def hue = {
    val h = math.floor(255 * (1 - this.hsb(0))) + 1
    if (h > 255) 0 else h.toInt
  }
  def saturation = (this.hsb(1) * 255).toInt
  def brightness = (this.hsb(2) * 255).toInt
  // TODO blendColor
}
object RichColor {
  def apply(c: java.awt.Color) = new RichColor(c)

  def lerpColor(from: RichColor, to: RichColor, amt: Double) = {
    require(amt >= 0d && amt <= 1d)
    new java.awt.Color(
      Math.lerp(from.red, to.red, amt).round.toInt,
      Math.lerp(from.green, to.green, amt).round.toInt,
      Math.lerp(from.blue, to.blue, amt).round.toInt
    )
  }
}
