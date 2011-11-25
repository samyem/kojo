/*
 * Copyright (C) 2011 Lalit Pant <pant.lalit@gmail.com>
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

package net.kogics.kojo.picture

abstract class Transform(pic: Picture) extends Picture {
  def offset = pic.offset
  def bounds = pic.bounds
  def dumpInfo() = pic.dumpInfo()
  def rotate(angle: Double) = pic.rotate(angle)
  def scale(factor: Double) = pic.scale(factor)
  def translate(x: Double, y: Double) = pic.translate(x, y)
  def decorateWith(painter: Painter) = pic.decorateWith(painter)
  def clear() = pic.clear()
}

case class Rot(angle: Double)(pic: Picture) extends Transform(pic) {
  def show() {
    pic.show()
    pic.rotate(angle)
  }
  def copy = Rot(angle)(pic.copy)
}

case class Scale(factor: Double)(pic: Picture) extends Transform(pic) {
  def show() {
    pic.show()
    pic.scale(factor)
  }
  def copy = Scale(factor)(pic.copy)
}

case class Trans(x: Double, y: Double)(pic: Picture) extends Transform(pic) {
  def show() {
    pic.show()
    pic.translate(x, y)
  }
  def copy = Trans(x, y)(pic.copy)
}

object Deco {
  def apply(pic: Picture)(painter: Painter): Deco = Deco(pic)(painter)
}

class Deco(pic: Picture)(painter: Painter) extends Transform(pic) {
  def show() {
    pic.decorateWith(painter) 
    pic.show() 
  }
  def copy = Deco(pic.copy)(painter)
}

import java.awt.Color
case class Fill(color: Color)(pic: Picture) extends Deco(pic)({ t =>
    t.setFillColor(color)
  }) {
  override def copy = Fill(color)(pic.copy)
}

case class Stroke(color: Color)(pic: Picture) extends Deco(pic)({ t =>
    t.setPenColor(color)
  }) {
  override def copy = Stroke(color)(pic.copy)
}


abstract class ComposableTransform extends Function1[Picture,Picture] {outer =>
  def apply(p: Picture): Picture
  def ->(p: Picture) = apply(p)
  def * (other: ComposableTransform) = new ComposableTransform {
    def apply(p: Picture): Picture = {
      outer.apply(other.apply(p))
    }
  }
}

case class Rotc(angle: Double) extends ComposableTransform {
  def apply(p: Picture) = Rot(angle)(p)
}

case class Scalec(factor: Double) extends ComposableTransform {
  def apply(p: Picture) = Scale(factor)(p)
}

case class Transc(x: Double, y: Double) extends ComposableTransform {
  def apply(p: Picture) = Trans(x, y)(p)
}

case class Fillc(color: Color) extends ComposableTransform {
  def apply(p: Picture) = Fill(color)(p)
}

case class Strokec(color: Color) extends ComposableTransform {
  def apply(p: Picture) = Stroke(color)(p)
}

case class Decoc(painter: Painter) extends ComposableTransform {
  def apply(p: Picture) = Deco(p)(painter)
}

