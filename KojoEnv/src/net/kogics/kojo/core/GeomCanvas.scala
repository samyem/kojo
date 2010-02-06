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
  type P <: Point with Labelled
  type L <: Line  with Labelled
  type LS <: L  with Labelled
  type A <: Angle  with Labelled

  def clear(): Unit
  def showAxes(): Unit
  def hideAxes(): Unit
  def point(label: String, x: Double, y: Double): P
  def point(label: String, on: L, x: Double, y: Double): P
  def line(label: String, p1: P, p2: P): L
  def lineSegment(label: String, p1: P, p2: P): LS
  def intersect(label: String, l1: L, l2: L): P
  def angle(label: String, p1: P, p2: P, p3: P): A
  def text(content: String, x: Double, y: Double): Text
}
