/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.kogics.kojo.core

import edu.umd.cs.piccolo.nodes._


trait Figure {
  def clear(): Unit
  def aclear(): Unit
  def setPenColor(color: java.awt.Color): Unit
  def setPenThickness(t: Double): Unit
  def setFillColor(color: java.awt.Color): Unit
  def point(x: Double, y: Double) = line(x, y, x, y)
  def line(x0: Double, y0: Double, x1: Double, y1: Double): PPath
  def ellipse(left: Double, top: Double, w: Double, h: Double): PPath
  def circle(x: Double, y: Double, radius: Double) = ellipse(x-radius, y-radius, 2*radius, 2*radius)
  def animationStep(fn: => Unit)
}
