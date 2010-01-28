/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.kogics.kojo.geogebra
import geogebra.plugin.GgbAPI

class Line(ggbApi: GgbAPI, p: Point, q: Point) {
  val gLine = ggbApi.getKernel.Line("a", p.gPoint, q.gPoint)
}
