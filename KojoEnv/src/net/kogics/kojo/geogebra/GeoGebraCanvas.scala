/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.kogics.kojo.geogebra

import net.kogics.kojo.InitedSingleton

import geogebra.GeoGebraPanel;
import geogebra.gui.menubar.GeoGebraMenuBar
import net.kogics.kojo.core.KojoCtx

object GeoGebraCanvas extends InitedSingleton[GeoGebraCanvas] {
  def initedInstance(kojoCtx: KojoCtx) = synchronized {
    instanceInit()
    val ret = instance()
    ret.geomCanvas.kojoCtx = kojoCtx
    ret
  }

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
