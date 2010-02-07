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

trait GeomCanvas {
  type GPoint <: Point with Labelled with MoveablePoint
  type GLine <: Line with Labelled
  type GSegment <: LineSegment with Labelled
  type GAngle <: Angle with Labelled
  type GText <: Text with Labelled

  def clear(): Unit
  def showAxes(): Unit
  def hideAxes(): Unit

  def point(label: String, x: Double, y: Double): GPoint
  def point(label: String, on: GLine, x: Double, y: Double): GPoint
  def line(label: String, p1: GPoint, p2: GPoint): GLine
  def lineSegment(label: String, p1: GPoint, p2: GPoint): GSegment
  def intersect(label: String, l1: GLine, l2: GLine): GPoint
  def angle(label: String, p1: GPoint, p2: GPoint, p3: GPoint): GAngle
  def text(content: String, x: Double, y: Double): GText
}
