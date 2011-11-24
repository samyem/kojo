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

import util.Utils
import edu.umd.cs.piccolo.util.PBounds 
import net.kogics.kojo.SpriteCanvas


object Impl {
  val canvas = SpriteCanvas.instance
//  val turtle0 = canvas.turtle0
//  val figure0 = canvas.figure0
}

trait Picture {
  def decorateWith(painter: Painter): Unit
  def show()
  def offsetX: Double
  def offsetX_=(n: Double)
  def offsetY: Double
  def offsetY_=(n: Double)
  def bounds(): PBounds
  def rotate(angle: Double)
  def scale(angle: Double)
  def translate(x: Double, y: Double)
  def dumpInfo(): Unit
}

case class Pic(painter: Painter) extends Picture {
  val t = Impl.canvas.newTurtle(0, 0)
  def decorateWith(painter: Painter) = painter(t)
  def show() = {
    painter(t)
    t.waitFor()
//    Utils.runInSwingThread {
//      t.tlayer.setBounds(t.tlayer.computeFullBounds(null))
//    }
  }
  
  def offsetX = Utils.runInSwingThreadAndWait {
    t.tlayer.getOffset.getX
  }
  def offsetX_=(n: Double) = translate(n, 0)
  
  def offsetY = Utils.runInSwingThreadAndWait {
    t.tlayer.getOffset.getY
  }
  def offsetY_=(n: Double) = translate(0, n)
  
  def translate(x: Double, y: Double) = Utils.runInSwingThread {
    t.tlayer.offset(x, y)
    t.tlayer.repaint()
  }
  
  def bounds = Utils.runInSwingThreadAndWait {
    t.tlayer.getFullBounds
  }
  
  def rotate(angle: Double) = Utils.runInSwingThread {
    val savedOffset = t.tlayer.getOffset
    t.tlayer.rotateInPlace(angle.toRadians)
    t.tlayer.setOffset(savedOffset)
    t.tlayer.repaint()
  }
  
  def scale(factor: Double) = Utils.runInSwingThread {
    t.tlayer.scale(factor)
    t.tlayer.repaint()
  }
  
  def dumpInfo() = Utils.runInSwingThreadAndWait {
    println(">>> Pic Start - " +  System.identityHashCode(this))
    println("Bounds: " + bounds)
    println("Offset: " + t.tlayer.getOffset)
    println("<<< Pic End\n")
  }
}

abstract class Transform(pic: Picture) extends Picture {
  def offsetX = pic.offsetX
  def offsetX_=(n: Double) = pic.offsetX_=(n)
  def offsetY: Double = pic.offsetY
  def offsetY_=(n: Double) = pic.offsetY_=(n)
  def bounds(): PBounds = pic.bounds
  def dumpInfo() = pic.dumpInfo()
  def rotate(angle: Double) = pic.rotate(angle)
  def scale(factor: Double) = pic.scale(factor)
  def translate(x: Double, y: Double) = pic.translate(x, y)
  def decorateWith(painter: Painter) = pic.decorateWith(painter)
}

case class Rot(angle: Double)(pic: Picture) extends Transform(pic) {
  def show() {
    pic.show()
    pic.rotate(angle)
  }
}

case class Scale(factor: Double)(pic: Picture) extends Transform(pic) {
  def show() {
    pic.show()
    pic.scale(factor)
  }
}

case class Trans(x: Double, y: Double)(pic: Picture) extends Transform(pic) {
  def show() {
    pic.show()
    pic.translate(x, y)
  }
}

object Deco {
  def apply(pic: Picture)(painter: Painter): Deco = Deco(pic)(painter)
}

class Deco(pic: Picture)(painter: Painter) extends Transform(pic) {
  def show() {
    pic.decorateWith(painter) 
    pic.show() 
  }
}

import java.awt.Color
case class Fill(color: Color)(pic: Picture) extends Deco(pic)({ t =>
    t.setFillColor(color)
  })

case class Stroke(color: Color)(pic: Picture) extends Deco(pic)({ t =>
    t.setPenColor(color)
  })

abstract class BasePicList(pics: Picture *) extends Picture {
  def offsetX = pics(0).offsetX
  def offsetX_=(n: Double) {
    pics.foreach { pic =>
      pic.offsetX = n
    }
  }
    
  def offsetY = pics(0).offsetY
  def offsetY_=(n: Double) {
//    println("Picture List %d being offsety by %f" format(System.identityHashCode(this), n))
    pics.foreach { pic =>
      pic.offsetY = n
    }
  }

  def bounds(): PBounds = Utils.runInSwingThreadAndWait {
    var rect = pics(0).bounds.getBounds2D
    pics.tail.foreach { pic =>
      rect = pic.bounds.getBounds2D.createUnion(rect)
    }        
    new PBounds(rect)
  }
  
  def rotate(angle: Double) {
    pics.foreach { pic =>
      pic.rotate(angle)
    }
  }
  
  def scale(angle: Double) {
    pics.foreach { pic =>
      pic.scale(angle)
    }
  }
  
  def translate(x: Double, y: Double) {
    pics.foreach { pic =>
      pic.translate(x, y)
    }
  }

  def decorateWith(painter: Painter) {
    pics.foreach { pic =>
      pic.decorateWith(painter)
    }
  }
  
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
  def show() {
    var offset = 0.0
    pics.foreach { pic =>
      pic.offsetX = offset
      pic.show()
      offset += pic.bounds.width
    }
  }
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
    var offset = 0.0
    pics.foreach { pic =>
      pic.offsetY = offset
      pic.show()
      offset += pic.bounds.height
    }
  }
  override def dumpInfo() {
    println(">>> VPics Start - " + System.identityHashCode(this))
    super.dumpInfo()
    println("<<< VPics End\n\n")
  }
} 
