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

import geogebra.kernel.GeoText
import geogebra.plugin.GgbAPI
import net.kogics.kojo.util.Utils

object Text {
  def apply(ggbApi: GgbAPI, content: String, x: Double, y: Double): Text = {
    net.kogics.kojo.util.Throttler.throttle()
    val text = Utils.runInSwingThreadAndWait {
      if (content.indexOf('"') < 0) {
        val gText = ggbApi.getKernel.Text("T", content)
        new Text(ggbApi, gText, x, y)
      }
      else {
        val ret = ggbApi.getAlgebraProcessor.processAlgebraCommand(content, false)
        if (ret != null && ret(0).isTextValue()) {
          val gText = ret(0).asInstanceOf[GeoText]
          new Text(ggbApi, gText, x, y)
        }
        else {
          null
        }
      }
    }
    text
  }
}

class Text(ggbApi: GgbAPI, val gText: GeoText, x: Double, y: Double) extends AbstractShape(ggbApi) with net.kogics.kojo.core.Text {
  gText.setRealWorldLoc(x, y)
  ctorDone()

  protected def geogebraElement = gText
}
