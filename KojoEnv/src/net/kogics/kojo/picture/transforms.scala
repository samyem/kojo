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

import java.awt.geom.AffineTransform

trait Transformer {
  val tpic: Picture
  def offset = tpic.offset
  def bounds = tpic.bounds
  def dumpInfo() = tpic.dumpInfo()
  def rotate(angle: Double, x: Double, y: Double) = tpic.rotate(angle, x, y)
  def scale(factor: Double, x: Double, y: Double) = tpic.scale(factor, x, y)
  def transformBy(trans: AffineTransform) = tpic.transformBy(trans)
  def translate(x: Double, y: Double) = tpic.translate(x, y)
  def decorateWith(painter: Painter) = tpic.decorateWith(painter)
  def clear() = tpic.clear()
}

abstract class Transform(pic: Picture) extends Picture with Transformer {
  val tpic = pic
}

case class Rot(angle: Double)(pic: Picture) extends Transform(pic) {
  def show() {
    pic.show()
    val o = pic.offset
    pic.rotate(angle, - o.getX, - o.getY)
  }
  def copy = Rot(angle)(pic.copy)
}

case class Scale(factor: Double)(pic: Picture) extends Transform(pic) {
  def show() {
    pic.show()
    val o = pic.offset
    pic.scale(factor, - o.getX, - o.getY)
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

case class Flip(pic: Picture) extends Transform(pic) {
  def show() {
    pic.show()
    val transform = AffineTransform.getScaleInstance(-1, 1)
    pic.transformBy(transform)
    pic.translate(pic.bounds.width, 0)
  }
  def copy = Flip(pic.copy)
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

case class StrokeWidth(w: Double)(pic: Picture) extends Deco(pic)({ t =>
    t.setPenThickness(w)
  }) {
  override def copy = StrokeWidth(w)(pic.copy)
}

abstract class ComposableTransformer extends Function1[Picture,Picture] {outer =>
  def apply(p: Picture): Picture
  def ->(p: Picture) = apply(p)
  def * (other: ComposableTransformer) = new ComposableTransformer {
    def apply(p: Picture): Picture = {
      outer.apply(other.apply(p))
    }
  }
}

case class Rotc(angle: Double) extends ComposableTransformer {
  def apply(p: Picture) = Rot(angle)(p)
}

case class Scalec(factor: Double) extends ComposableTransformer {
  def apply(p: Picture) = Scale(factor)(p)
}

case class Transc(x: Double, y: Double) extends ComposableTransformer {
  def apply(p: Picture) = Trans(x, y)(p)
}

case object Flipc extends ComposableTransformer {
  def apply(p: Picture) = Flip(p)
}

case class Fillc(color: Color) extends ComposableTransformer {
  def apply(p: Picture) = Fill(color)(p)
}

case class Strokec(color: Color) extends ComposableTransformer {
  def apply(p: Picture) = Stroke(color)(p)
}

case class StrokeWidthc(w: Double) extends ComposableTransformer {
  def apply(p: Picture) = StrokeWidth(w)(p)
}

case class Decoc(painter: Painter) extends ComposableTransformer {
  def apply(p: Picture) = Deco(p)(painter)
}

