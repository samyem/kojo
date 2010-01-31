/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.kogics.kojo.geogebra

import net.kogics.kojo.Singleton

import geogebra.GeoGebraPanel;
import geogebra.gui.menubar.GeoGebraMenuBar

object GeoGebraCanvas extends Singleton[GeoGebraCanvas] {
  protected def newInstance = new GeoGebraCanvas
}

class GeoGebraCanvas extends GeoGebraPanel {
  setMaxIconSize(24)

  setShowAlgebraInput(true)
  setShowMenubar(false)
  setShowToolbar(true)

  buildGUI()
  app.getGuiManager().initMenubar()


  val ggbApi = getGeoGebraAPI
  val geomCanvas = new GeomCanvas(ggbApi)

  def selectAllAction = app.getGuiManager().getMenuBar().asInstanceOf[GeoGebraMenuBar].getSelectAllAction
}
