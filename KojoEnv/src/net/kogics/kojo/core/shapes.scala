/*
 * Copyright (C) 2010 Lalit Pant <pant.lalit@gmail.com>
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

package net.kogics.kojo.core

import scala.collection._

trait Shape {
  def hide()
  def show()
  def setColor(color: java.awt.Color)
  def showNameInLabel() {}
  def showNameValueInLabel() {}
  def showValueInLabel() {}
  def hideLabel() {}
  def showLabel() {}
}

trait Point extends Shape {
  val x: Double
  val y: Double

  def cx: Double = x
  def cy: Double = y
}

trait Line extends Shape {
  val p1: Point
  val p2: Point
}

trait LineSegment extends Line {
}

trait Ellipse extends Shape {
  val left: Double
  val top: Double
  val w: Double
  val h: Double
}

trait Circle extends Shape {
  val center: Point
  val radius: Double
}

trait Polygon extends Shape {
  val points: mutable.ArrayBuffer[Point]
}

trait Angle extends Shape 
trait Text extends Shape 
