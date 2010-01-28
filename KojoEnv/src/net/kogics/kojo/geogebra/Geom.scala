/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.kogics.kojo.geogebra
import geogebra.plugin.GgbAPI

class Geom(ggbApi: GgbAPI) {
  def gPoint(x: Double, y: Double) = new Point(ggbApi, x, y)
  def gLine(p: Point, q: Point) = new Line(ggbApi, p, q)

}
