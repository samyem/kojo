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
package net.kogics.kojo

import javax.swing._
import javax.swing.event._
import java.awt.{List => _, _}
import java.awt.event._
import java.util.logging._

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.nodes._
import edu.umd.cs.piccolo.util.PPaintContext
import edu.umd.cs.piccolo.event._

import net.kogics.kojo.core.SCanvas
import net.kogics.kojo.util.Utils

import org.openide.awt.StatusDisplayer

import scala.collection._
import scala.{math => Math}

import figure.Figure
import turtle.Turtle
import core.SpriteListener
import net.kogics.kojo.core.DelegatingSpriteListener

abstract class UnitLen
case object Pixel extends UnitLen
case object Cm extends UnitLen
case object Inch extends UnitLen

object SpriteCanvas extends InitedSingleton[SpriteCanvas] {
  def initedInstance(kojoCtx: KojoCtx) = synchronized {
    instanceInit()
    val ret = instance()
    ret.kojoCtx = kojoCtx
    ret
  }

  protected def newInstance = new SpriteCanvas
}

class SpriteCanvas private extends PCanvas with SCanvas {
  val Log = Logger.getLogger(getClass.getName);
  @volatile var kojoCtx: core.KojoCtx = _

  val defLayer = getLayer
  val AxesColor = new Color(100, 100, 100)
  val GridColor = new Color(200, 200, 200)
  val TickColor = new Color(150, 150, 150)
  val TickLabelColor = new Color(50, 50, 50)
  val TickIntegerLabelColor = Color.blue

  val Dpi = Toolkit.getDefaultToolkit.getScreenResolution
  @volatile var unitLen: UnitLen = Pixel

  def setUnitLength(ul: UnitLen) {
    throw new UnsupportedOperationException("Use clearWithUL(unit) instead of setUnitLength(unit) and clear().")
  }
  
  private def realSetUnitLength(ul: UnitLen) {
    unitLen = ul
  }
  
  var outputFn: String => Unit = { msg =>
    Log.info(msg)
  }

  setBackground(Color.white)
  setPreferredSize(new Dimension(200, 400))
  setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING)
  setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING)
  setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING)

//  edu.umd.cs.piccolo.util.PDebug.debugBounds = true
//  edu.umd.cs.piccolo.util.PDebug.debugFullBounds = true
//  edu.umd.cs.piccolo.util.PDebug.debugPaintCalls = true

  @volatile var turtles: List[Turtle] = Nil
  var puzzlers: List[Turtle] = Nil
  var figures: List[Figure] = Nil
  var eventListeners: List[PInputEventListener] = Nil

  var showAxes = false
  var showGrid = false
  var showProt = false

  val grid = new PNode()
  val axes = new PNode()
  getCamera.addChild(grid)
  getCamera.addChild(axes)

  initCamera()

  addComponentListener(new ComponentAdapter {
      override def componentResized(e: ComponentEvent) = initCamera()
    })

  val megaListener = new DelegatingSpriteListener
  val figure = newFigure()
  @volatile var turtle = newTurtle()
  val pictures = new PLayer
  getCamera.addLayer(pictures)
  
  def turtle0 = turtle
  val figure0 = figure
  val origTurtle = turtle
  
  def setDefTurtle(t: Turtle) = Utils.runInSwingThreadAndWait {
    turtle = t
  }

  def restoreDefTurtle() = Utils.runInSwingThreadAndWait {
    turtle = origTurtle
  }

  val panHandler = new PPanEventHandler() {
//    setAutopan(false)
    override def pan(event: PInputEvent) {
      super.pan(event)
      Utils.schedule(0.05) {
        updateAxesAndGrid()
      }
    }
    
    override def dragActivityStep(event: PInputEvent) {
      super.dragActivityStep(event)
      Utils.schedule(0.05) {
        updateAxesAndGrid()
      }
    }
  }

  val zoomHandler = new PZoomEventHandler {
    override def dragActivityStep(event: PInputEvent) {
      if (event.isHandled) {
        return
      }
      
      super.dragActivityStep(event)
      event.setHandled(true)
      Utils.schedule(0.05) {
        updateAxesAndGrid()
      }
    }
  }

  panHandler.getEventFilter.setNotMask(InputEvent.SHIFT_MASK)
  zoomHandler.setEventFilter(new PInputEventFilter(InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK))

  setPanEventHandler(panHandler)
  setZoomEventHandler(zoomHandler)

  addInputEventListener(new PBasicInputEventHandler {
      val popup = new Popup()
      
      def showPopup(e: PInputEvent) {
        if(e.isPopupTrigger) {
          val pos = e.getCanvasPosition
          popup.show(SpriteCanvas.this, pos.getX.toInt, pos.getY.toInt);
        }
      }
      
      override def mousePressed(e: PInputEvent) = showPopup(e)
      override def mouseReleased(e: PInputEvent) = showPopup(e)
      
      override def mouseMoved(e: PInputEvent) {
        val pos = e.getPosition
        val prec0 = Math.round(getCamera.getViewTransformReference.getScale/camScale) - 1
        val prec = {
          if (prec0 < 0) 0
          else if (prec0 > 18) 18
          else prec0
        }
        val statusStr = "Mouse Position: (%%.%df, %%.%df)" format(prec, prec)
        StatusDisplayer.getDefault().setStatusText(statusStr format(pos.getX, pos.getY));
      }
    })

  def camScale = unitLen match {
    case Pixel => 1
    case Inch => Dpi
    case Cm => Dpi / 2.54
  }
  
  private def initCamera() {
    val size = getSize(null)
    val scale = camScale
    getCamera.getViewTransformReference.setToScale(scale, -scale)
    getCamera.setViewOffset(size.getWidth/2f, size.getHeight/2f)
    updateAxesAndGrid()
  }

  def gridOn() {
    Utils.runInSwingThreadAndWait {
      if (!showGrid) {
        showGrid = true
        updateAxesAndGrid()
        repaint()
      }
    }
  }

  def gridOff() {
    Utils.runInSwingThreadAndWait {
      if (showGrid) {
        showGrid = false
        grid.removeAllChildren()
        repaint()
      }
    }
  }

  def axesOn() {
    Utils.runInSwingThreadAndWait {
      if (!showAxes) {
        showAxes = true
        updateAxesAndGrid()
        repaint()
      }
    }
  }

  def axesOff() {
    Utils.runInSwingThreadAndWait {
      if (showAxes) {
        showAxes = false
        axes.removeAllChildren()
        repaint()
      }
    }
  }

  def updateAxesAndGrid() {

//    def isInteger(d: Double) = Utils.doublesEqual(d.floor, d, 0.0000000001)
    def isInteger(label: String) = {
      val d = label.toDouble
      Utils.doublesEqual(d.floor, d, 0.0000000001)
    }
    
    val seedDelta = unitLen match {
      case Pixel => 50
      case Inch => Dpi
      case Cm => Dpi/2.54
    }

    if (!(showGrid || showAxes))
      return
    
    val scale = getCamera.getViewScale 
    val MaxPrec = 10
    val prec0 = Math.round(scale)
    val prec = prec0 match {
      case p if p < 10 => 0
      case p if p < 50 => 2
      case p if p < 100 => 4
      case p if p < 150 => 6
      case p if p < 200 => 8
      case _ => MaxPrec
    }

    val labelPrec = if (scale % seedDelta == 0) math.log10(scale).round else prec

    val labelText = "%%.%df" format(labelPrec)
    val deltaFinder = "%%.%df" format(if (prec == 0) prec else prec-1)
    
    val delta = {
      val d = seedDelta
      val d0 = d/scale
      if (d0 > 10) {
        math.round(d0/10) * 10
      }
      else {
        val d2 = deltaFinder.format(d0).toDouble
        if (d2.compare(0) != 0) d2 else 0.0000000005 // MaxPrec-1 zeroes
      }
    }

    val viewBounds = getCamera.getViewBounds()
    val width = viewBounds.width.toFloat
    val height = viewBounds.height.toFloat
    val vbx = viewBounds.x.toFloat
    val vby = viewBounds.y.toFloat

    import java.awt.geom._
    val screenCenter = new Point2D.Double(vbx + width/2, vby + height/2)

    val deltap = new Point2D.Double(delta, delta)
    val numxTicks = Math.ceil(width / deltap.getY).toInt + 4
    val numyTicks = Math.ceil(height / deltap.getX).toInt + 4
    val tickSize = 3
    
    val xStart = {
      val x = viewBounds.x
      if (x < 0) Math.floor(x/deltap.getX) * deltap.getX
      else Math.ceil(x/deltap.getX) * deltap.getX
    } - 2*deltap.getX

    val yStart = {
      val y = viewBounds.y
      if (y < 0) Math.floor(y/deltap.getY) * deltap.getY
      else Math.ceil(y/deltap.getY) * deltap.getY
    } - 2*deltap.getY

    grid.removeAllChildren()
    axes.removeAllChildren()

    val xmin = xStart - deltap.getX
    val xmax = xStart + (numxTicks+1) * deltap.getX
    
    val ymin = yStart - deltap.getY
    val ymax = yStart + (numyTicks+1) * deltap.getY

    if (showAxes) {
      val xa1 = getCamera.viewToLocal(new Point2D.Double(xmin, 0))
      var xa2 = getCamera.viewToLocal(new Point2D.Double(xmax, 0))
      val xAxis = PPath.createLine(xa1.getX.toFloat, xa1.getY.toFloat, xa2.getX.toFloat, xa2.getY.toFloat)
      xAxis.setStrokePaint(AxesColor)
      axes.addChild(xAxis)

      val ya1 = getCamera.viewToLocal(new Point2D.Double(0, ymin))
      val ya2 = getCamera.viewToLocal(new Point2D.Double(0, ymax))
      val yAxis = PPath.createLine(ya1.getX.toFloat, ya1.getY.toFloat, ya2.getX.toFloat, ya2.getY.toFloat)
      yAxis.setStrokePaint(AxesColor)
      axes.addChild(yAxis)
    }
    
    // gridlines and ticks on y axis
    for (i <- 0 until numyTicks) {
      val ycoord = yStart + i * deltap.getY
      if (showGrid) {
        // gridOn
        val pt1 = getCamera.viewToLocal(new Point2D.Double(xmin, ycoord))
        val pt2 = getCamera.viewToLocal(new Point2D.Double(xmax, ycoord))
        val gridline = PPath.createLine(pt1.getX.toFloat, pt1.getY.toFloat, pt2.getX.toFloat, pt2.getY.toFloat)
        gridline.setStrokePaint(GridColor)
        grid.addChild(gridline)
      }
      if (showAxes) {
        val pt1 = getCamera.viewToLocal(new Point2D.Double(-tickSize/scale, ycoord))
        val pt2 = getCamera.viewToLocal(new Point2D.Double(tickSize/scale, ycoord))
        val tick = PPath.createLine(pt1.getX.toFloat, pt1.getY.toFloat, pt2.getX.toFloat, pt2.getY.toFloat)
        tick.setStrokePaint(TickColor)
        axes.addChild(tick)
        
        if (!Utils.doublesEqual(ycoord, 0, 1/math.pow(10, prec+1))) {
          val label = new PText(labelText format(ycoord))
          label.setOffset(pt2.getX.toFloat, pt2.getY.toFloat)
          if (isInteger(label.getText)) {
            label.setText("%.0f" format(ycoord))
            label.setTextPaint(TickIntegerLabelColor)
          }
          else {
            label.setTextPaint(TickLabelColor)
          }
          axes.addChild(label)
        }
      }
    }

    // gridlines and ticks on x axis
    for (i <- 0 until numxTicks) {
      val xcoord = xStart + i * deltap.getX
      if (showGrid) {
        val pt1 = getCamera.viewToLocal(new Point2D.Double(xcoord, ymax))
        val pt2 = getCamera.viewToLocal(new Point2D.Double(xcoord, ymin))
        val gridline = PPath.createLine(pt1.getX.toFloat, pt1.getY.toFloat, pt2.getX.toFloat, pt2.getY.toFloat)
        gridline.setStrokePaint(GridColor)
        grid.addChild(gridline)
      }
      if (showAxes) {
        val pt1 = getCamera.viewToLocal(new Point2D.Double(xcoord, tickSize/scale))
        val pt2 = getCamera.viewToLocal(new Point2D.Double(xcoord, -tickSize/scale))
        val tick = PPath.createLine(pt1.getX.toFloat, pt1.getY.toFloat, pt2.getX.toFloat, pt2.getY.toFloat)
        tick.setStrokePaint(TickColor)
        axes.addChild(tick)

        if (Utils.doublesEqual(xcoord, 0, 1/math.pow(10, prec+1))) {
          val label = new PText("0")
          label.setOffset(pt2.getX.toFloat+2, pt2.getY.toFloat)
          label.setTextPaint(TickIntegerLabelColor)
          axes.addChild(label)
        }
        else {
          val label = new PText(labelText format(xcoord))
          label.setOffset(pt2.getX.toFloat, pt2.getY.toFloat)
          if (isInteger(label.getText)) {
            label.setText("%.0f" format(xcoord))
            label.setTextPaint(TickIntegerLabelColor)
          }
          else {
            label.setTextPaint(TickLabelColor)
          }
          if (label.getText.length > 5) {
            label.rotateInPlace(45.toRadians)
          }
          axes.addChild(label)
        }
      }
    }

//    outputFn("\nScale: %f\n" format(scale))
//    outputFn("Deltap: %s\n" format(deltap.toString))
  }

  def zoom(factor0: Double, cx: Double, cy: Double) {
    Utils.runInSwingThreadAndWait {
      val size = getSize(null)
      val factor = factor0 * camScale
      getCamera.getViewTransformReference.setToScale(factor, -factor)
      getCamera.getViewTransformReference.setOffset(size.getWidth/2d - cx*factor, size.getHeight/2d + cy*factor)
      updateAxesAndGrid()
      repaint()
    }
  }

  def zoomXY(xfactor0: Double, yfactor0: Double, cx: Double, cy: Double) {
    Utils.runInSwingThreadAndWait {
      val xfactor = xfactor0 * camScale
      val yfactor = yfactor0 * camScale
      val size = getSize(null)
      getCamera.getViewTransformReference.setToScale(xfactor, -yfactor)
      getCamera.getViewTransformReference.setOffset(
        size.getWidth / 2d - cx * xfactor.abs,
        size.getHeight / 2d + cy * yfactor.abs
      )
      updateAxesAndGrid()
      repaint()
    }
  }

  import java.io.File
  private def exportImageHelper(filePrefix: String, width: Int, height: Int): java.io.File = {
    val outfile = File.createTempFile(filePrefix + "-", ".png")
    exportImageToFile(outfile, width, height)
    outfile
  }

  private def exportImageToFile(outfile: File, width: Int, height: Int) {
    val image = getCamera.toImage(width, height, java.awt.Color.white)
    javax.imageio.ImageIO.write(image.asInstanceOf[java.awt.image.BufferedImage], "png", outfile)
  }
  
  def exportImage(filePrefix: String): File = {
    exportImageHelper(filePrefix, getWidth, getHeight)
  }

  def exportThumbnail(filePrefix: String, height: Int): File = {
    exportImageHelper(filePrefix, (getWidth.toFloat/getHeight * height).toInt, height)
  }

  def afterClear() = {
    // initCamera()
  }

  def forceClear() {
    stop()
    clearHelper()
  }
  
  def makeStagingVisible() {
    kojoCtx.makeStagingVisible()
  }
  
  def clearStaging() {
    realSetUnitLength(Pixel)
    realClearStaging()
  }
  
  def clearStagingWul(ul: UnitLen) {
    realSetUnitLength(ul)
    realClearStaging()
  }
  
  def clear() {
    realSetUnitLength(Pixel)
    realClear()
  }
  
  def clearWithUL(ul: UnitLen) {
    realSetUnitLength(ul)
    realClear()
  }
  
  def realClearStaging() {
    makeStagingVisible()
    clearHelper0()
  }

  def realClear() {
    kojoCtx.makeTurtleWorldVisible()
    clearHelper0()
  }
  
  private def clearHelper0() {
    clearHelper()
    // turtles.foreach {t => t.waitFor()}
  }
  
  private def clearHelper() {
    gridOff()
    axesOff()
    Utils.runInSwingThreadAndWait {
      showProt = false
      if (getPanEventHandler == null) {
        // clobbered by drag handling
        setPanEventHandler(panHandler)
      }

      turtles.foreach {t => if (t == origTurtle) t.clear() else t.remove()}
      turtles = List(turtles.last)

      figures.foreach {f => if (f == figure) f.clear() else f.remove()}
      figures = List(figures.last)
      
      eventListeners.foreach {el => removeInputEventListener(el)}
      eventListeners = Nil
      staging.Inputs.removeKeyHandler()
      getRoot.getDefaultInputManager.setKeyboardFocus(null)
      
      pictures.removeAllChildren()
    }
    zoom(1, 0, 0)
  }

  def clearPuzzlers() {
    stop()
    Utils.runInSwingThreadAndWait {
      puzzlers.foreach {t => t.remove()}
      puzzlers = Nil
    }
  }

  def stop() = {
    Utils.runInSwingThreadAndWait {
      puzzlers.foreach {t => t.stop}
      turtles.foreach {t => t.stop}
      figures.foreach {f => f.stop}
      megaListener.pendingCommandsDone()
      Utils.schedule(0.5) {
        megaListener.pendingCommandsDone()
      }
    }
  }

  def newFigure(x: Int = 0, y: Int = 0) = {
    val fig = Utils.runInSwingThreadAndWait {
      val f = Figure(this, x, y)
      f.setSpriteListener(megaListener)
      figures = f :: figures
      f
    }
    this.repaint()
    fig
  }

  def newTurtle(x: Int = 0, y: Int = 0) = {
    val ttl = Utils.runInSwingThreadAndWait {
      val t = new Turtle(this, "/images/turtle32.png", x, y)
//      t.setTurtleListener(megaListener)
      turtles = t :: turtles
      t
    }
    this.repaint()
    ttl
  }

  def newInvisibleTurtle(x: Int = 0, y: Int = 0) = {
    val ttl = Utils.runInSwingThreadAndWait {
      val t = new Turtle(this, "/images/turtle32.png", x, y, true)
//      t.setTurtleListener(megaListener)
      turtles = t :: turtles
      t
    }
    this.repaint()
    ttl
  }

  def newPuzzler(x: Int = 0, y: Int = 0) = {
    val pzl = Utils.runInSwingThreadAndWait {
      val t = new Turtle(this, "/images/puzzler32.png", x, y, false, true)
//      t.setTurtleListener(megaListener)
      t.setPenThickness(1)
      t.setPenColor(Color.blue)
      t.setAnimationDelay(10)
      puzzlers = t :: puzzlers
      t
    }
    this.repaint()
    pzl
  }

  def setTurtleListener(l: SpriteListener) {
    megaListener.setRealListener(l)
  }
  
  def onKeyPress(fn: Int => Unit) = Utils.runInSwingThread {
    staging.Inputs.setKeyHandler {e =>
      Utils.runAsyncQueued {
        fn(e.getKeyCode)
      }
    }
  }
  
  def onMouseClick(fn: (Double, Double) => Unit) = Utils.runInSwingThread {
    val eh = new PBasicInputEventHandler {
      override def mousePressed(event: PInputEvent) {
        val pos = event.getPosition
        Utils.runAsyncQueued {
          fn(pos.getX, pos.getY)
        }
      }
    }
    eventListeners = eh :: eventListeners
    addInputEventListener(eh)
  }
  
  var globalEl: PInputEventListener = _
  def addGlobalEventListener(l: PInputEventListener) {
    globalEl = l
    addInputEventListener(l)
  }
  
  def activate() {
    def grabFocus() {
      SCanvasTopComponent.findInstance().requestActive()
      getRoot.getDefaultInputManager.setKeyboardFocus(globalEl)
    }
    Utils.schedule(0) {
      // do it right away
      grabFocus()
    }
    Utils.schedule(0.3) {
      // and also a little later, in case the history mechanism gives 
      // the focus to the script editor
      grabFocus()
    }
  }
  
  def cbounds = getCamera.getViewBounds()

  class Popup() extends JPopupMenu {

    val axesItem = new JCheckBoxMenuItem("Show Axes")
    axesItem.addActionListener(new ActionListener {
        override def actionPerformed(e: ActionEvent) {
          if (axesItem.isSelected) {
            axesOn()
          }
          else {
            axesOff()
          }
        }
      })
    add(axesItem)

    val gridItem = new JCheckBoxMenuItem("Show Grid")
    gridItem.addActionListener(new ActionListener {
        override def actionPerformed(e: ActionEvent) {
          if (gridItem.isSelected) {
            gridOn()
          }
          else {
            gridOff()
          }
        }
      })
    add(gridItem)
    
    addSeparator()

    val protItem = new JCheckBoxMenuItem("Show Protractor")
    protItem.addActionListener(new ActionListener {
        @volatile var prot: picture.Picture = _
        def protOn() {
          showProt = true
          prot = picture.protractor(camScale)
          // can draw from GUI thread because anim delay is zero, and latch will not be used
          prot.draw()
        }
        
        def protOff() {
          showProt = false
          prot.invisible()
        }
        
        override def actionPerformed(e: ActionEvent) {
          if (protItem.isSelected) {
            protOn()
          }
          else {
            protOff()
          }
        }
      })
    add(protItem)
    
    val saveAsImage = new JMenuItem("Save as Image")
    saveAsImage.addActionListener(new ActionListener {
        val saveAs = new SaveAs()
        override def actionPerformed(e: ActionEvent) {
          val file = saveAs.chooseFile("PNG Image File", "png")
          if (file != null) {
            exportImageToFile(file, SpriteCanvas.this.getWidth, SpriteCanvas.this.getHeight)
          }
        }
      })
    add(saveAsImage)

    addSeparator()

    val resetPanZoomItem = new JMenuItem("Reset Pan and Zoom")
    resetPanZoomItem.addActionListener(new ActionListener {
        override def actionPerformed(e: ActionEvent) {
          initCamera()
        }
      })
    add(resetPanZoomItem)
    val clearItem = new JMenuItem("Clear + Stop Animation")
    clearItem.addActionListener(new ActionListener {
        override def actionPerformed(e: ActionEvent) {
          forceClear()
        }
      })
    add(clearItem)

    addSeparator()

    add("<html><em>Mouse Actions: Drag to Pan; Shift-Drag to Zoom</em></html>")
    addPopupMenuListener(new PopupMenuListener {
        def popupMenuWillBecomeVisible(e: PopupMenuEvent) {
          axesItem.setState(showAxes)
          gridItem.setState(showGrid)
          protItem.setState(showProt)
        }
        def popupMenuWillBecomeInvisible(e: PopupMenuEvent) {}
        def popupMenuCanceled(e: PopupMenuEvent) {}
      })
  }
}


