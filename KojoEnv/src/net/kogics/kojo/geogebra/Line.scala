/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.kogics.kojo.geogebra
import geogebra.plugin.GgbAPI

class Line(ggbApi: GgbAPI, val p1: Point, val p2: Point) extends net.kogics.kojo.core.Line {
  val gLine = ggbApi.getKernel.Line("a", p1.gPoint, p2.gPoint)
}
