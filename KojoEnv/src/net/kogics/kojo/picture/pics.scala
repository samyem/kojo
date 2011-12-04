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

import java.awt.geom.AffineTransform

import util.Utils
import edu.umd.cs.piccolo.PLayer
import edu.umd.cs.piccolo.util.PAffineTransform
import edu.umd.cs.piccolo.util.PBounds 

object Impl {
  val canvas = SpriteCanvas.instance
//  val turtle0 = canvas.turtle0
//  val figure0 = canvas.figure0
}

trait Picture {
  def decorateWith(painter: Painter): Unit
  def show(): Unit
  def bounds: PBounds
  def rotate(angle: Double)
  def scale(factor: Double)
  def translate(x: Double, y: Double)
  def transformBy(trans: AffineTransform)
  def dumpInfo(): Unit
  def clear(): Unit
  def copy: Picture
  def tnode: PLayer
}

trait BoundsCacher {
  @volatile var cachedBounds: PBounds = _
  def bounds = {
    if (cachedBounds == null) {
      cachedBounds = boundsHelper
    }
    cachedBounds
  }
  def boundsHelper: PBounds
}

case class Pic(painter: Painter) extends Picture with BoundsCacher {
  @volatile var _t: turtle.Turtle = _
  @volatile var penWidth = 0.0
  var _srtransform = new PAffineTransform
  
  def t = Utils.runInSwingThreadAndWait {
    if (_t == null) {
      _t = Impl.canvas.newTurtle(0, 0)
    }
    _t
  }
  
  def tnode = t.tlayer

  def decorateWith(painter: Painter) = painter(t)
  def show() = {
    painter(t)
    t.waitFor()
    penWidth = _t.pen.getThickness
  }
  
  def offset = Utils.runInSwingThreadAndWait {
    t.tlayer.getOffset
  }  
  
  def boundsHelper = Utils.runInSwingThreadAndWait {
    val cb = t.tlayer.getUnionOfChildrenBounds(null)
    new PBounds(_srtransform.transform(
        new PBounds(cb.x + penWidth, cb.y + penWidth, cb.width - 2*penWidth, cb.height - 2 * penWidth), 
        null
      ))
  }
  
  def translate(x: Double, y: Double) = Utils.runInSwingThread {
    _srtransform.translate(x, y)
    t.tlayer.translate(x, y)
    t.tlayer.repaint()
  }
  
  def rotate(angle: Double) = Utils.runInSwingThread {
    val rt = AffineTransform.getRotateInstance(angle.toRadians)
    _srtransform.concatenate(rt)
    transformBy(rt)
  }
  
  def scale(factor: Double) = Utils.runInSwingThread {
    val st = AffineTransform.getScaleInstance(factor, factor)
    _srtransform.concatenate(st)
    transformBy(st)
  }
  
  def transformBy(trans: AffineTransform) = Utils.runInSwingThread {
    t.tlayer.transformBy(trans)
    t.tlayer.invalidatePaint();
    t.tlayer.invalidateFullBounds();
    t.tlayer.repaint()
  }
    
  def copy = Pic(painter)
  def clear() {
    cachedBounds = null
    Utils.runInSwingThread {
      t.tlayer.setOffset(0, 0)
      t.tlayer.setRotation(0)
      t.tlayer.setScale(1)
      _srtransform = new PAffineTransform
    }
    t.clear()
  }
    
  def dumpInfo() = Utils.runInSwingThreadAndWait {
    println(">>> Pic Start - " +  System.identityHashCode(this))
    println("Bounds: " + bounds)
    println("Offset: " + t.tlayer.getOffset)
    println("<<< Pic End\n")
  }
}

abstract class BasePicList(pics: Picture *) extends Picture with BoundsCacher {
  @volatile var padding = 0.0
  var ptransform = new PAffineTransform
  var _srtransform = new PAffineTransform
  
  def srtransform = Utils.runInSwingThreadAndWait {
    _srtransform
  }
  
  def rotate(angle: Double) = Utils.runInSwingThread {
    ptransform.rotate(angle.toRadians)
    srtransform.rotate(angle.toRadians)
  }

  def scale(factor: Double) = Utils.runInSwingThread {
    ptransform.scale(factor, factor)
    srtransform.scale(factor, factor)
  }
  
  def transformBy(trans: AffineTransform) = Utils.runInSwingThread {
    ptransform.concatenate(trans)
  }
  
  def translate(x: Double, y: Double) = Utils.runInSwingThread {
    ptransform.translate(x, y)
    srtransform.translate(x, y)
  }

  def decorateWith(painter: Painter) {
    pics.foreach { pic =>
      pic.decorateWith(painter)
    }
  }
  
  def show() = Utils.runInSwingThread {
    pics.foreach { pic =>
      pic.transformBy(ptransform)
    }
  }
  
  def clear() {
    cachedBounds = null
    Utils.runInSwingThread {
      ptransform = new PAffineTransform
      _srtransform = new PAffineTransform
    }
    pics.foreach { pic =>
      pic.clear()
    }
  }
  
  def withGap(n: Double): Picture = {
    padding = n
    this
  }
  
  protected def picsCopy: List[Picture] = pics.map {_ copy}.toList
  
  def tnode = throw new UnsupportedOperationException
  
  def dumpInfo() {
    println("--- ")
    println("Pic List Bounds: " + bounds)
    println("--- ")
    
    pics.foreach { pic =>
      pic.dumpInfo
    }
  }
}

object HPics {
  def apply(pics: List[Picture]):HPics = HPics(pics:_*)
}

case class HPics(pics: Picture *) extends BasePicList(pics:_*) {

  def boundsHelper: PBounds = {
    var width = 0.0
    var height = 0.0
    pics.foreach { pic =>
      val nbounds = pic.bounds
      width = nbounds.getMinX + nbounds.getWidth + padding
      val h = nbounds.getMinY + nbounds.getHeight
      height = if (h > height) h else height
    }
    new PBounds(srtransform.transform(new PBounds(0,0, width, height), null))
  }
  
  override def show() {
    super.show()
    var ox = 0.0
    pics.foreach { pic =>
      pic.translate(ox, 0)
      pic.show()
      val nbounds = pic.bounds
//      println("Pic %d tr bounds: %s" format(System.identityHashCode(pic), nbounds))
//      println("Pic %d tr bounds min x: %f, width: %f" format(System.identityHashCode(pic), nbounds.getMinX, nbounds.getWidth))
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
  def apply(pics: List[Picture]):VPics = VPics(pics:_*)
}

case class VPics(pics: Picture *) extends BasePicList(pics:_*) {

  def boundsHelper: PBounds = {
    var width = 0.0
    var height = 0.0
    pics.foreach { pic =>
      val nbounds = pic.bounds
      height = nbounds.getMinY + nbounds.getHeight + padding
      val w = nbounds.getMinX + nbounds.getWidth
      width = if (w > width) w else width
    }
    new PBounds(srtransform.transform(new PBounds(0,0, width, height), null))
  }

  override def show() {
    super.show()
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
  def apply(pics: List[Picture]):GPics = GPics(pics:_*)
}

case class GPics(pics: Picture *) extends BasePicList(pics:_*) {

  def boundsHelper: PBounds = {
    var width = 0.0
    var height = 0.0
    pics.foreach { pic =>
      val nbounds = pic.bounds
      val h = nbounds.getMinY + nbounds.getHeight
      height = if (h > height) h else height
      val w = nbounds.getMinX + nbounds.getWidth
      width = if (w > width) w else width
    }
    new PBounds(srtransform.transform(new PBounds(0,0, width, height), null))
  }

  override def show() {
    super.show()
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
