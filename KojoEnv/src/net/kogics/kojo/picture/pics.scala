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
import java.awt.geom.Point2D

import util.Utils
import edu.umd.cs.piccolo.util.PBounds 

object Impl {
  val canvas = SpriteCanvas.instance
//  val turtle0 = canvas.turtle0
//  val figure0 = canvas.figure0
}

trait Picture {
  def parent: Picture
  def parent_=(p: Picture): Unit
  def decorateWith(painter: Painter): Unit
  def show(): Unit
  def offset: Point2D
  def bounds: PBounds
  def rotate(angle: Double)
  def rotateWithParent(angle: Double, px: Double, py: Double)
  def scale(factor: Double)
  def scaleWithParent(factor: Double, px: Double, py: Double)
  def flipp(): Unit
  def translate(x: Double, y: Double)
  def transformBy(trans: AffineTransform)
  def transformByWithParent(trans: AffineTransform, px: Double, py: Double)
  def dumpInfo(): Unit
  def clear(): Unit
  def copy: Picture
}

case class Pic(painter: Painter) extends Picture {
  @volatile var _t: turtle.Turtle = _
  @volatile var parent: Picture = _
  
  def t = Utils.runInSwingThreadAndWait {
    if (_t == null) {
      _t = Impl.canvas.newTurtle(0, 0)
    }
    _t
  }

  def decorateWith(painter: Painter) = painter(t)
  def show() = {
    painter(t)
    t.waitFor()
  }
  
  def offset = Utils.runInSwingThreadAndWait {
    t.tlayer.getOffset
  }  
  
  def bounds = Utils.runInSwingThreadAndWait {
    t.tlayer.getFullBounds
  }
  
  private def relativeOffset(px: Double, py: Double) = {
    val o = offset
    (o.getX - px, o.getY - py)
  }
  
  def srTran = {
    val ct = t.tlayer.getTransform()
    ct.invert()
    new AffineTransform(ct.getScaleX, ct.getShearY, ct.getShearX, ct.getScaleY, 0, 0)
  }
  
  def translate(x: Double, y: Double) = Utils.runInSwingThread {
    val newt = srTran.transform(new Point2D.Double(x, y), null)
    t.tlayer.translate(newt.getX, newt.getY)
    t.tlayer.repaint()
  }
  
  def rotate(angle: Double) {
    transformBy(AffineTransform.getRotateInstance(angle.toRadians))
  }
  
  def rotateWithParent(angle: Double, px: Double, py: Double) {
    transformByWithParent(AffineTransform.getRotateInstance(angle.toRadians), px, py)
  }

  def scale(factor: Double)  {
    transformBy(AffineTransform.getScaleInstance(factor, factor))
  }
  
  def scaleWithParent(factor: Double, px: Double, py: Double)  {
    transformByWithParent(AffineTransform.getScaleInstance(factor, factor), px, py)    
  }
  
  def flipp() {
    val trans = AffineTransform.getScaleInstance(-1, 1)
    val ct = t.tlayer.getTransform()
    ct.invert()
    t.tlayer.transformBy(ct)
    t.tlayer.transformBy(trans)
    ct.invert()
    t.tlayer.transformBy(ct)
    t.tlayer.invalidatePaint();
    t.tlayer.invalidateFullBounds();
    t.tlayer.repaint()
  }
  
  def transformBy(trans: AffineTransform) = Utils.runInSwingThread {
    t.tlayer.transformBy(trans)
    t.tlayer.invalidatePaint();
    t.tlayer.invalidateFullBounds();
    t.tlayer.repaint()
  }
    
  def transformByWithParent(trans: AffineTransform, px: Double, py: Double) = Utils.runInSwingThread {
    val (x,y) = relativeOffset(px,py)
    val newO = srTran.transform(new Point2D.Double(-x, -y), null)
    t.tlayer.translate(newO.getX, newO.getY)
    t.tlayer.transformBy(trans)
    t.tlayer.translate(-newO.getX, -newO.getY)
    t.tlayer.invalidatePaint();
    t.tlayer.invalidateFullBounds();
    t.tlayer.repaint()
  }
    
  def copy = Pic(painter)
  def clear() {
    Utils.runInSwingThread {
      t.tlayer.setOffset(0, 0)
      t.tlayer.setRotation(0)
      t.tlayer.setScale(1)
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

abstract class BasePicList(pics: Picture *) extends Picture {
  var _offsetX, _offsetY = 0.0
  @volatile var padding = 0.0
  var shown = false
  @volatile var parent: Picture = _

  pics.foreach { pic =>
    pic.parent = this
  }

  def offset = Utils.runInSwingThreadAndWait { new Point2D.Double(_offsetX, _offsetY) }
  def offsetX = Utils.runInSwingThreadAndWait { _offsetX }
  def offsetY = Utils.runInSwingThreadAndWait { _offsetY }

  def bounds(): PBounds = Utils.runInSwingThreadAndWait {
    val b = pics(0).bounds
    pics.tail.foreach { pic =>
      b.add(pic.bounds)
    }        
    b
  }
  
  def rotate(angle: Double) = Utils.runInSwingThread {
    pics.foreach { pic =>
      pic.rotateWithParent(angle, _offsetX, _offsetY)
    }
  }

  def rotateWithParent(angle: Double, px: Double, py: Double) {
    pics.foreach { pic =>
      pic.rotateWithParent(angle, px, py)
    }
  }
  
  def scale(factor: Double) = Utils.runInSwingThread {
    pics.foreach { pic =>
      pic.scaleWithParent(factor, _offsetX, _offsetY)
    }
  }
  
  def scaleWithParent(factor: Double, px: Double, py: Double) {
    pics.foreach { pic =>
      pic.scaleWithParent(factor, px, py)
    }
  }
  
  def transformBy(trans: AffineTransform) = Utils.runInSwingThread {
    pics.foreach { pic =>
      pic.transformByWithParent(trans, _offsetX, _offsetY)
    }
  }
  
  def transformByWithParent(trans: AffineTransform, px: Double, py: Double) {
    pics.foreach { pic =>
      pic.transformByWithParent(trans, px, py)
    }
  }
  
  def flipp() {
    pics.foreach { pic =>
      pic.flipp()
    }
  }
  
  def translate(x: Double, y: Double) = Utils.runInSwingThread {
    _offsetX += x
    _offsetY += y
    
    if (shown) {
      pics.foreach { pic =>
        pic.translate(x, y)
      }
    }
  }

  def decorateWith(painter: Painter) {
    pics.foreach { pic =>
      pic.decorateWith(painter)
    }
  }
  
  def show() = Utils.runInSwingThread {
    shown = true
  }
  
  def clear() {
    Utils.runInSwingThread {
      _offsetX = 0
      _offsetY = 0
      shown = false
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
  
  def dumpInfo() {
    println("--- ")
    println("Pic List Bounds: " + bounds)
    println("Pic List Offset: (%f, %f)" format(offsetX, offsetY) )
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
  override def show() {
    super.show()
    var ox = offsetX
    pics.foreach { pic =>
      pic.translate(ox, offsetY)
      pic.show()
      ox = pic.bounds.x + pic.bounds.width + padding
//      ox = pic.offset.getX + pic.bounds.width + padding
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
  override def show() {
    super.show()
    var oy = offsetY
    pics.foreach { pic =>
      pic.translate(offsetX, oy)
      pic.show()
      oy = pic.bounds.y + pic.bounds.height + padding
//      oy = pic.offset.getY + pic.bounds.height + padding
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
  override def show() {
    super.show()
    pics.foreach { pic =>
      pic.translate(offsetX, offsetY)
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
