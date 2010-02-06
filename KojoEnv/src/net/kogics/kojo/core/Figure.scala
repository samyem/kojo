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

package net.kogics.kojo.core

import edu.umd.cs.piccolo.nodes._


trait Figure {
  def clear(): Unit
  def fgClear(): Unit
  def setPenColor(color: java.awt.Color): Unit
  def setPenThickness(t: Double): Unit
  def setFillColor(color: java.awt.Color): Unit

  type P <: Point

  def point(x: Double, y: Double): P
  def line(p1: P, p2: P): Line
  def line(x0: Double, y0: Double, x1: Double, y1: Double): Line
  def ellipse(center: P, w: Double, h: Double): Ellipse
  def ellipse(cx: Double, cy: Double, w: Double, h: Double): Ellipse
  def circle(cx: Double, cy: Double, radius: Double) = ellipse(cx, cy, 2*radius, 2*radius)
  def text(content: String, x: Double, y: Double): Text
  def refresh(fn: => Unit)
}
