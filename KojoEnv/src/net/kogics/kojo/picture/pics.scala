/*
 * Copyright (C) 2011 Lalit Pant <pant.lalit@gmail.com>
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
package picture

import java.awt.BasicStroke
import java.awt.geom.AffineTransform

import scala.collection.mutable.ArrayBuffer
import util.Utils
import util.InputAware
import util.Vector2D
import edu.umd.cs.piccolo.PNode
import edu.umd.cs.piccolo.nodes.PPath
import edu.umd.cs.piccolo.util.PBounds 

import com.vividsolutions.jts.geom._
import com.vividsolutions.jts.geom.util.AffineTransformation

object Impl {
  val canvas = SpriteCanvas.instance
  val camera = canvas.getCamera
  val picLayer = canvas.pictures
  val Gf = new GeometryFactory
}

trait Picture extends InputAware {
  def myCanvas = Impl.canvas
  def myNode = tnode
  def decorateWith(painter: Painter): Unit
  def show(): Unit
  def bounds: PBounds
  def rotate(angle: Double)
  def rotateAboutPoint(angle: Double, x: Double, y: Double)
  def scale(factor: Double)
  def translate(x: Double, y: Double)
  def transv(v: Vector2D) = translate(v.x, v.y)
  def offset(x: Double, y: Double)
  def flipX(): Unit
  def flipY(): Unit
  def transformBy(trans: AffineTransform)
  def dumpInfo(): Unit
  def copy: Picture
  def tnode: PNode
  def axesOn(): Unit
  def axesOff(): Unit
  def visible(): Unit
  def invisible(): Unit
  def toggleV(): Unit
  def intersects(other: Picture): Boolean
  def collidesWith(other: Picture) = intersects(other)
  def distanceTo(other: Picture): Double
  def area: Double
  def perimeter: Double
  def picGeom: Geometry
  
  def setPosition(x: Double, y: Double)
  def position: core.Point
  def act(fn: Picture => Unit)
}

trait CorePicOps { self: Picture with ReshowStopper =>
  var axes: PNode = _
  var _picGeom: Geometry = _
  var pgTransform = new AffineTransformation
  
  def realShow(): Unit

  def show() {
    realShow()
    showDone
  }
  
  def showDone() = Utils.runInSwingThread {
    _picGeom = initGeom()
  }

  def t2t(t: AffineTransform): AffineTransformation = {
    val ms = Array.fill(6)(0.0)
    val ms2 = Array.fill(6)(0.0)
    t.getMatrix(ms)
    ms2(0) = ms(0) // m00
    ms2(1) = ms(2) // m01
    ms2(2) = ms(4) // m02
    ms2(3) = ms(1) // m10
    ms2(4) = ms(3) // m11
    ms2(5) = ms(5) // m12
    new AffineTransformation(ms2)
  }

  def transformBy(trans: AffineTransform) = Utils.runInSwingThread {
    tnode.transformBy(trans)
//    pgTransform.composeBefore(t2t(trans))
    pgTransform = t2t(tnode.getTransformReference(true))
    tnode.repaint()
  }

  def rotateAboutPoint(angle: Double, x: Double, y: Double) {
    translate(x, y)
    rotate(angle)
    translate(-x, -y)
  }

  def rotate(angle: Double) {
    transformBy(AffineTransform.getRotateInstance(angle.toRadians))
  }
  
  def scale(factor: Double) {
    transformBy(AffineTransform.getScaleInstance(factor, factor))
  }
  
  def translate(x: Double, y: Double) {
    transformBy(AffineTransform.getTranslateInstance(x, y))
  }

  def offset(x: Double, y: Double) = Utils.runInSwingThread {
    tnode.offset(x, y)
    pgTransform = t2t(tnode.getTransformReference(true))
    tnode.repaint()
  }
  
  def position = Utils.runInSwingThreadAndWait {
    val o = tnode.getOffset
    new core.Point(o.getX, o.getY)
  }  
  
  def setPosition(x: Double, y: Double) = Utils.runInSwingThread {
    tnode.setOffset(x, y)
    pgTransform = t2t(tnode.getTransformReference(true))
    tnode.repaint()
  }
  
  def flipX() {
    transformBy(AffineTransform.getScaleInstance(1, -1))
  }
  
  def flipY() {
    transformBy(AffineTransform.getScaleInstance(-1, 1))
  }
  
  def axesOn() = Utils.runInSwingThread {
    if (axes == null) {
      val (size, delta, num, bigt) = Impl.canvas.unitLen match {
        case Pixel => (200.0f, 20.0f, 10, 5)
        case Inch => (4.0f, 0.25f, 16, 4)
        case Cm => (10f, .5f, 20, 2)
      }
      val camScale = Impl.canvas.camScale.toFloat
      val tickSize = 3/camScale
      val overrun = 5/camScale
      def line(x1: Float, y1: Float, x2: Float, y2: Float) = {
        val l = PPath.createLine(x1, y1, x2, y2)
        l.setStroke(new BasicStroke(2/camScale))
        l
      }
      def text(s: String, x: Double, y: Double) = {
        Utils.textNode(s, x, y, camScale)
      }
      axes = new PNode
      axes.addChild(line(-overrun, 0, size, 0))
      axes.addChild(line(0, -overrun, 0, size))
      for (i <- 1 to num) {
        val ts = if (i % bigt == 0) 2* tickSize else tickSize
        axes.addChild(line(i*delta, ts, i*delta, -ts))
        axes.addChild(line(-ts, i*delta, ts, i*delta))
      }
      axes.addChild(text("x", size - delta/2, delta))
      axes.addChild(text("y", delta/2, size))
      tnode.addChild(axes)
    } 
    else {
      axes.setVisible(true)
    }
    tnode.repaint()
  }

  def axesOff() = Utils.runInSwingThread {
    if (axes != null) {
      axes.setVisible(false)
      tnode.repaint()
    }
  }
  
  def visible() = Utils.runInSwingThread {
    if (!tnode.getVisible) {
      tnode.setVisible(true)
      tnode.repaint()
    }
  }
  
  def invisible() = Utils.runInSwingThread {
    if (tnode.getVisible) {
      tnode.setVisible(false)
      tnode.repaint()
    }
  }
  
  def toggleV() = Utils.runInSwingThread {
    if (tnode.getVisible) {
      tnode.setVisible(false)
    }
    else {
      tnode.setVisible(true)
    }
    tnode.repaint()
  }

  def initGeom(): Geometry
  def picGeom = {
    if (_picGeom == null) {
      throw new IllegalStateException("Access geometry after show is done")
    }
    else {
      pgTransform.transform(_picGeom)
    }
  }
    
  def intersects(other: Picture) = Utils.runInSwingThreadAndWait {
    if (tnode.getVisible && other.tnode.getVisible) {
      picGeom.intersects(other.picGeom)
    }
    else {
      false
    }
  }
  
  def distanceTo(other: Picture) = Utils.runInSwingThreadAndWait {
    picGeom.distance(other.picGeom)
  }
  
  def toPolygon(g: Geometry) = {
    val gc = g.getCoordinates
    val ab = new ArrayBuffer[Coordinate]
    ab ++= gc
    ab += gc(0)
    Impl.Gf.createPolygon(Impl.Gf.createLinearRing(ab.toArray), null)
  }
  
  def area = Utils.runInSwingThreadAndWait {
    toPolygon(picGeom).getArea
  }
  
  def perimeter = Utils.runInSwingThreadAndWait {
    picGeom.getLength
  }
  
  def act(fn: Picture => Unit) {
    if (!shown) {
      throw new IllegalStateException("Ask picture to act after you show it.")
    }
 
    staging.API.loop {
      fn(this)
    }
  }
}

trait ReshowStopper extends Picture {
  @volatile var shown = false
  abstract override def show() {
    if (shown) {
      throw new RuntimeException("You can't reshow a picture")
    }
    else {
      shown = true
      super.show()
    }
  }
}

trait TNodeCacher {
  def makeTnode: PNode
  @volatile var _tnode: PNode = _
  def tnode = {
    if (_tnode == null) {
      _tnode = makeTnode
    }
    _tnode
  }
}

object Pic {
  def apply(painter: Painter) = new Pic(painter) 
}

class Pic(painter: Painter) extends Picture with CorePicOps with TNodeCacher with ReshowStopper {
  @volatile var _t: turtle.Turtle = _
  def t = Utils.runInSwingThreadAndWait {
    if (_t == null) {
      _t = Impl.canvas.newTurtle(0, 0)
      val tl = _t.tlayer
      Impl.camera.removeLayer(tl)
      Impl.picLayer.addChild(tl)
      tl.repaint()
      Impl.picLayer.repaint
    }
    _t
  }
  
  def makeTnode = t.tlayer
  
  def decorateWith(painter: Painter) = painter(t)
  def realShow() {
    painter(t)
    t.waitFor()
    Utils.runInSwingThread {
      val tl = tnode
      tl.invalidateFullBounds()
      tl.repaint()
      Impl.picLayer.repaint
    }
  }
  
  def bounds = Utils.runInSwingThreadAndWait {
    tnode.getFullBounds
  }
  
  def initGeom() = {
    val cab = new ArrayBuffer[Coordinate]
    val pp = t.penPaths
    pp.foreach { pl =>
      pl.points.foreach {pt =>
        cab += new Coordinate(pt.x, pt.y)
      }
    }
    if (cab.size == 1) {
      cab += cab(0)
    }
    Impl.Gf.createLineString(cab.toArray)
  }

  def copy: Picture = Pic(painter)
    
  def dumpInfo() = Utils.runInSwingThreadAndWait {
    println(">>> Pic Start - " +  System.identityHashCode(this))
    println("Bounds: " + bounds)
    println("Tnode: " + System.identityHashCode(tnode))
    println("<<< Pic End\n")
  }
}

abstract class BasePicList(val pics: List[Picture]) 
extends Picture with CorePicOps with TNodeCacher with ReshowStopper {
  if (pics.size == 0) {
    throw new IllegalArgumentException("A Picture List needs to have at least one Picture.")
  }
  @volatile var padding = 0.0
  def makeTnode = Utils.runInSwingThreadAndWait {
    val tn = new PNode()
    pics.foreach { pic =>
      Impl.picLayer.removeChild(pic.tnode)
      tn.addChild(pic.tnode)
    }
    Impl.picLayer.addChild(tn)
    tn
  }

  def bounds = Utils.runInSwingThreadAndWait {
    tnode.getFullBounds
  }
  
  def decorateWith(painter: Painter) {
    pics.foreach { pic =>
      pic.decorateWith(painter)
    }
  }
  
  def withGap(n: Double): Picture = {
    padding = n
    this
  }
  
  def initGeom() = {
    var pg = pics(0).picGeom
    pics.tail.foreach { pic =>
      pg = pg union pic.picGeom
    }
    pg
  }

  protected def picsCopy: List[Picture] = pics.map {_ copy}
  
  def dumpInfo() {
    println("--- ")
    println("Pic List Bounds: " + bounds)
    println("Pic List Tnode: " + System.identityHashCode(tnode))
    println("--- ")
    
    pics.foreach { pic =>
      pic.dumpInfo
    }
  }
}

object HPics {
  def apply(pics: Picture *): HPics = new HPics(pics.toList) 
  def apply(pics: List[Picture]): HPics = new HPics(pics)
}

class HPics(pics: List[Picture]) extends BasePicList(pics) {
  def realShow() {
    var ox = 0.0
    pics.foreach { pic =>
      pic.translate(ox, 0)
      pic.show()
      val nbounds = pic.bounds
      ox = nbounds.getMinX + nbounds.getWidth + padding
    }
  }

  def copy = HPics(picsCopy).withGap(padding)

  override def dumpInfo() {
    println(">>> HPics Start - " + System.identityHashCode(this))
    super.dumpInfo()
    println("<<< HPics End\n\n")
  }
} 

object VPics {
  def apply(pics: Picture *): VPics = new VPics(pics.toList) 
  def apply(pics: List[Picture]): VPics = new VPics(pics) 
}

class VPics(pics: List[Picture]) extends BasePicList(pics) {
  def realShow() {
    var oy = 0.0
    pics.foreach { pic =>
      pic.translate(0, oy)
      pic.show()
      val nbounds = pic.bounds
      oy = nbounds.getMinY + nbounds.getHeight + padding
    }
  }

  def copy = VPics(picsCopy).withGap(padding)

  override def dumpInfo() {
    println(">>> VPics Start - " + System.identityHashCode(this))
    super.dumpInfo()
    println("<<< VPics End\n\n")
  }
}
  
object GPics {
  def apply(pics: Picture *): GPics = new GPics(pics.toList) 
  def apply(pics: List[Picture]): GPics = new GPics(pics) 
}

class GPics(pics: List[Picture]) extends BasePicList(pics) {
  def realShow() {
    pics.foreach { pic =>
      pic.show()
    }
  }

  def copy = GPics(picsCopy).withGap(padding)

  override def dumpInfo() {
    println(">>> GPics Start - " + System.identityHashCode(this))
    super.dumpInfo()
    println("<<< GPics End\n\n")
  }
} 
