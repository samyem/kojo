/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.kogics.kojo.geogebra
import geogebra.plugin.GgbAPI

class GeomCanvas(ggbApi: GgbAPI) extends net.kogics.kojo.core.GeomCanvas {
  type P = Point

  def point(x: Double, y: Double) = new Point(ggbApi, x, y)
  def line(p1: P, p2: P) = new Line(ggbApi, p1, p2)
}
