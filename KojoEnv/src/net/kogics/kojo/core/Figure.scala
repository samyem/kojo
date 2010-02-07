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

  type FPoint <: Point with Visible
  type FLine <: Line with Visible
  type FEllipse <: Ellipse with Visible
  type FArc <: Arc with Visible
  type FText <: Text with Visible

  def point(x: Double, y: Double): FPoint
  def line(p1: Point, p2: Point): FLine
  def line(x0: Double, y0: Double, x1: Double, y1: Double): FLine
  def ellipse(center: Point, w: Double, h: Double): FEllipse
  def ellipse(cx: Double, cy: Double, w: Double, h: Double): FEllipse
  def arc(onEll: Ellipse, start: Double, extent: Double): FArc
  def arc(cx: Double, cy: Double, w: Double, h: Double, start: Double, extent: Double): FArc
  def arc(cx: Double, cy: Double, r: Double, start: Double, extent: Double): FArc
  def circle(cx: Double, cy: Double, radius: Double) = ellipse(cx, cy, 2*radius, 2*radius)
  def text(content: String, x: Double, y: Double): FText
  def refresh(fn: => Unit)
}
