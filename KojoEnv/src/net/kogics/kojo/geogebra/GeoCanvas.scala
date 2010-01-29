/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.kogics.kojo.geogebra

import net.kogics.kojo.Singleton

import geogebra.GeoGebraPanel;
import geogebra.plugin.GgbAPI
import geogebra.gui.menubar.GeoGebraMenuBar

object GeoCanvas extends Singleton[GeoCanvas] {
  protected def newInstance = new GeoCanvas
}

class GeoCanvas extends GeoGebraPanel with net.kogics.kojo.core.GeoCanvas {
  setMaxIconSize(24)

  setShowAlgebraInput(true)
  setShowMenubar(false)
  setShowToolbar(true)

  buildGUI()
  app.getGuiManager().initMenubar()


  def api: GgbAPI = this.getGeoGebraAPI
  val geom = new Geom(api)

  def selectAllAction = app.getGuiManager().getMenuBar().asInstanceOf[GeoGebraMenuBar].getSelectAllAction
}
