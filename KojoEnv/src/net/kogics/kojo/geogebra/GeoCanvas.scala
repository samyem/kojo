/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.kogics.kojo.geogebra

import net.kogics.kojo.Singleton

import geogebra.GeoGebraPanel;
import geogebra.plugin.GgbAPI

object GeoCanvas extends Singleton[GeoCanvas] {
  protected def newInstance = new GeoCanvas
}

class GeoCanvas extends GeoGebraPanel {
  setMaxIconSize(24)

  setShowAlgebraInput(true)
  setShowMenubar(false)
  setShowToolbar(true)

  buildGUI()

  def api: GgbAPI = this.getGeoGebraAPI
  val geom = new Geom(api)
}
