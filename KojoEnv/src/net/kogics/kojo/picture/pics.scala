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
import edu.umd.cs.piccolo.PNode
import edu.umd.cs.piccolo.util.PBounds 

object Impl {
  val canvas = SpriteCanvas.instance
  val camera = canvas.getCamera
  val picLayer = canvas.pictures
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
  def copy: Picture
  def tnode: PNode
}

trait RSTImpl {self: Picture =>
  def rotate(angle: Double) {
    transformBy(AffineTransform.getRotateInstance(angle.toRadians))
  }

  def scale(factor: Double) {
    transformBy(AffineTransform.getScaleInstance(factor, factor))
  }
  
  def translate(x: Double, y: Double) {
    transformBy(AffineTransform.getTranslateInstance(x, y))
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

case class Pic(painter: Painter) extends Picture with RSTImpl with TNodeCacher {
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
  def show() = {
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
  
  def transformBy(trans: AffineTransform) = Utils.runInSwingThread {
    tnode.transformBy(trans)
    tnode.repaint()
  }
  
  def copy = Pic(painter)
    
  def dumpInfo() = Utils.runInSwingThreadAndWait {
    println(">>> Pic Start - " +  System.identityHashCode(this))
    println("Bounds: " + bounds)
    println("Tnode: " + System.identityHashCode(tnode))
    println("<<< Pic End\n")
  }
}

abstract class BasePicList(pics: Picture *) extends Picture with RSTImpl with TNodeCacher {
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
  
  def transformBy(trans: AffineTransform) = Utils.runInSwingThread {
    tnode.transformBy(trans)
    tnode.repaint()
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
  
  protected def picsCopy: List[Picture] = pics.map {_ copy}.toList
  
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
  def apply(pics: List[Picture]):HPics = HPics(pics:_*)
}

case class HPics(pics: Picture *) extends BasePicList(pics:_*) {
  def show() {
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
  def apply(pics: List[Picture]):VPics = VPics(pics:_*)
}

case class VPics(pics: Picture *) extends BasePicList(pics:_*) {
  def show() {
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

  def show() {
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
