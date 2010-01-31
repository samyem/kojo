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

class Point(ggbApi: GgbAPI, val x: Double, val y: Double) extends net.kogics.kojo.core.Point {
  val gPoint = ggbApi.getKernel.Point(PointLabel.next(), x, y)
  
  override def cx = gPoint.x
  override def cy = gPoint.y

  def moveTo(x: Double, y: Double) {
    gPoint.setCoords(x, y, 1)
    gPoint.updateCascade()
    ggbApi.getKernel.notifyRepaint()
  }
}
