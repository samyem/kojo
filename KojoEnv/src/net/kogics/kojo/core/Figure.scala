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
  def stopRefresh(): Unit
  def setPenColor(color: java.awt.Color): Unit
  def setPenThickness(t: Double): Unit
  def setFillColor(color: java.awt.Color): Unit

  type FPoint <: Point with VisualElement
  type FLine <: Line with VisualElement
  type FEllipse <: Ellipse with VisualElement
  type FArc <: Arc with VisualElement
  type FText <: Text with VisualElement
  type FRectangle <: Rectangle with VisualElement
  type FRRectangle <: RoundRectangle with VisualElement
  type FPath <: Path with VisualElement

  def point(x: Double, y: Double): FPoint
  def line(p1: Point, p2: Point): FLine
  def line(x0: Double, y0: Double, x1: Double, y1: Double): FLine
  def ellipse(center: Point, w: Double, h: Double): FEllipse
  def ellipse(cx: Double, cy: Double, w: Double, h: Double): FEllipse
  def arc(onEll: Ellipse, start: Double, extent: Double): FArc
  def arc(cx: Double, cy: Double, w: Double, h: Double, start: Double, extent: Double): FArc
  def arc(cp: Point, r: Double, start: Double, extent: Double): FArc
  def arc(cx: Double, cy: Double, r: Double, start: Double, extent: Double): FArc
  def circle(cp: Point, radius: Double): Ellipse
  def circle(cx: Double, cy: Double, radius: Double): Ellipse
  def rectangle(bLeft: Point, tRight: Point): FRectangle
  def rectangle(x0: Double, y0: Double, w: Double, h: Double): FRectangle
  def roundRectangle(p1: Point, p2: Point, rx: Double, ry: Double): FRRectangle
  def text(content: String, p: Point): FText
  def text(content: String, x: Double, y: Double): FText
  def polyLine(path: net.kogics.kojo.kgeom.PolyLine): net.kogics.kojo.kgeom.PolyLine
  def path(descr: String): FPath
  def ppath(path: PPath): Unit
  def refresh(fn: => Unit): Unit
}
