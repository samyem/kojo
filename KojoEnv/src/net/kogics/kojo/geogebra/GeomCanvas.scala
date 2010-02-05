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

package net.kogics.kojo.geogebra
import geogebra.plugin.GgbAPI

class GeomCanvas(ggbApi: GgbAPI) extends net.kogics.kojo.core.GeomCanvas {
  type P = Point
  type L = Line
  type LS = LineSegment

  def clear() {
    ggbApi.getApplication.setSaved()
    ggbApi.getApplication.fileNew()
  }

  def showAxes() {
    ggbApi.setAxesVisible(true, true)
  }

  def hideAxes() {
    ggbApi.setAxesVisible(false, false)
  }

  def point(label: String, x: Double, y: Double) = Point(ggbApi, label, x, y)
  def point(label: String, on: L, x: Double, y: Double) = Point(ggbApi, label, on, x, y)

  def line(label: String, p1: P, p2: P) = Line(ggbApi, label, p1, p2)

  def lineSegment(label: String, p1: P, p2: P) = LineSegment(ggbApi, label, p1, p2)

  def intersect(label: String, l1: L, l2: L): P = {
    Point(ggbApi, label, l1, l2)
  }

  def angle(label: String, p1: P, p2: P, p3: P): Angle = {
    Angle(ggbApi, label, p1, p2, p3)
  }

  def text(content: String, x: Double, y: Double): Text = {
    val txt = Text(ggbApi, content, x, y)
    txt
  }
}
