/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.kogics.kojo.geogebra

import geogebra.kernel._
import geogebra.plugin.GgbAPI

object PointLabel {
  var ctr = 0
  var chr = 'A'
  def next(): String = {
    val res = if (ctr == 0)
      chr + ""
    else
      chr + ctr.toString

    chr = (chr + 1).toChar
    if (chr > 'Z') {
      chr = 'A'
      ctr += 1
    }
    res
  }
}

class Point(ggbApi: GgbAPI, x: Double, y: Double) {
  val gPoint = ggbApi.getKernel.Point(PointLabel.next(), x, y)
  def moveTo(x: Double, y: Double) {
    gPoint.setCoords(x, y, 1)
    gPoint.updateCascade()
    ggbApi.getKernel.notifyRepaint()
  }
}
