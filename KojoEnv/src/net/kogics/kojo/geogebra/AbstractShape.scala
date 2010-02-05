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
import net.kogics.kojo.util.Utils

import java.util.logging._

abstract class AbstractShape(ggbApi: GgbAPI) extends net.kogics.kojo.core.Shape {
  
  protected def geogebraElement: GeoElement

  protected def ctorDone() {
    ggbApi.getApplication.storeUndoInfo()
    repaint()
  }

  def repaint() {
//    geogebraElement.updateRepaint()
    geogebraElement.updateCascade()
    ggbApi.getKernel.notifyRepaint()
  }

  def hide() {
    Utils.runInSwingThread {
      geogebraElement.setEuclidianVisible(false)
      repaint()
    }
  }

  def show() {
    Utils.runInSwingThread {
      geogebraElement.setEuclidianVisible(true)
      repaint()
    }
  }

  def setColor(color: java.awt.Color) {
    Utils.runInSwingThread {
      geogebraElement.setObjColor(color)
      repaint()
    }
  }

  override def showNameInLabel() {
    Utils.runInSwingThread {
      geogebraElement.setLabelMode(GeoElement.LABEL_NAME)
      repaint()
    }
  }

  override def showNameValueInLabel() {
    Utils.runInSwingThread {
      geogebraElement.setLabelMode(GeoElement.LABEL_NAME_VALUE)
      repaint()
    }
  }

  override def showValueInLabel() {
    Utils.runInSwingThread {
      geogebraElement.setLabelMode(GeoElement.LABEL_VALUE)
      repaint()
    }
  }

  override def hideLabel() {
    Utils.runInSwingThread {
      geogebraElement.setLabelVisible(false)
      repaint()
    }
  }

  override def showLabel() {
    Utils.runInSwingThread {
      geogebraElement.setLabelVisible(true)
      repaint()
    }
  }
}
