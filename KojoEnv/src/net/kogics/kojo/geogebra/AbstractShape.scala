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

import geogebra.kernel.GeoElement
import geogebra.plugin.GgbAPI

import java.util.logging._
import net.kogics.kojo.util.Throttler

abstract class AbstractShape(ggbApi: GgbAPI) extends net.kogics.kojo.core.Shape {
  
  Throttler.throttle()
  protected def geogebraElement: GeoElement

  protected def ctorDone() {
    ggbApi.getApplication.storeUndoInfo()
    repaint()
  }

  def repaint() {
//    geogebraElement.updateRepaint()
    geogebraElement.updateCascade()
    ggbApi.getKernel.notifyRepaint()
//    need to think about app.storeUndoInfo();
  }

  def hide() {
    geogebraElement.setEuclidianVisible(false)
    repaint()
  }

  def show() {
    geogebraElement.setEuclidianVisible(true)
    repaint()
  }

  def setColor(color: java.awt.Color) {
    geogebraElement.setObjColor(color)
    repaint()
  }

  override def showNameInLabel() {
    geogebraElement.setLabelMode(GeoElement.LABEL_NAME)
    repaint()
  }

  override def showNameValueInLabel() {
    geogebraElement.setLabelMode(GeoElement.LABEL_NAME_VALUE)
    repaint()
  }

  override def showValueInLabel() {
    geogebraElement.setLabelMode(GeoElement.LABEL_VALUE)
    repaint()
  }
}
