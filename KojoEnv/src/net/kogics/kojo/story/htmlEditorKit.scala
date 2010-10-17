/*
 * Copyright (C) 2010 Lalit Pant <pant.lalit@gmail.com>
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
package story

import java.awt._
import javax.swing._
import java.awt.image._
import javax.swing.text._
import javax.swing.text.html._
import org.scilab.forge.jlatexmath._

object CustomHtmlEditorKit {
  val latexPrefix = "latex://"
}

class CustomHtmlEditorKit extends HTMLEditorKit {
  override def getViewFactory() = new CustomHtmlFactory()
  
//  override def createDefaultDocument() = {
//    val doc = super.createDefaultDocument().asInstanceOf[HTMLDocument]
//    val baseDir = CodeEditorTopComponent.findInstance().getLastLoadStoreDir() + "/"
//    doc.setBase(new java.net.URL("file:///" + baseDir))
//    println("Doc base is %s: " format(new java.net.URL("file:///" + baseDir).toString))
//    doc
//  }
}

class CustomHtmlFactory extends HTMLEditorKit.HTMLFactory {
  
  override def create(elem: Element) = {
    def hasLatexAttr(e: Element) = e.getAttributes().getAttribute(HTML.Attribute.SRC) match {
      case src: String if (src.startsWith(CustomHtmlEditorKit.latexPrefix)) => true
      case _ => false
    }

    val o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute)
    val src = elem.getAttributes().getAttribute(HTML.Attribute.SRC)
    o match {
      case kind: HTML.Tag if(kind == HTML.Tag.IMG && hasLatexAttr(elem)) => new LatexView(elem)
      case _ => super.create(elem)
    }
  }
}

class LatexView(elem: Element) extends View(elem) {
  val srcAttr = elem.getAttributes().getAttribute(HTML.Attribute.SRC).asInstanceOf[String]
  val latex = srcAttr.substring(CustomHtmlEditorKit.latexPrefix.length, srcAttr.length) // strip off latex prefix
  val formula = new TeXFormula(latex)
  val icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 18)
  icon.setInsets(new Insets(2, 2, 2, 2))
  val jl = new JLabel();
  jl.setForeground(new Color(30, 30, 30));
  
//  override def getImage() = {
//    val image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB)
//    val g2 = image.createGraphics();
//    g2.setColor(Color.white);
//    g2.fillRect(0,0,icon.getIconWidth(),icon.getIconHeight());
//    val jl = new JLabel();
//    jl.setForeground(new Color(0, 0, 0));
//    icon.paintIcon(jl, g2, 0, 0);
//    image
//  }

  override def getPreferredSpan(axis: Int) = {
    axis match {
      case View.X_AXIS => icon.getIconWidth
      case View.Y_AXIS => icon.getIconHeight
    }
  }

  override def getMinimumSpan(axis: Int) = getPreferredSpan(axis)
  override def getMaximumSpan(axis: Int) = getPreferredSpan(axis)

  override def viewToModel(x: Float, y: Float, a: Shape, biasReturn: Array[Position.Bias]): Int = {
//    println("viewToModel(%f, %f, %s, %s)" format(x, y, a.toString, biasReturn.toString))
    val alloc = a.asInstanceOf[Rectangle];
    if (x < alloc.x + alloc.width) {
      biasReturn(0) = Position.Bias.Forward
      getStartOffset()
    }
    else {
      biasReturn(0) = Position.Bias.Backward
      getEndOffset()
    }
  }

  override def modelToView(pos: Int, a: Shape, b: Position.Bias): Shape = {
//    println("modelToView(%d, %s, %s)" format(pos, a.toString, b.toString))
    new Rectangle(0, 0, icon.getIconWidth, icon.getIconHeight)
  }

  override def paint(g: Graphics, a: Shape) {
    icon.paintIcon(jl, g, a.getBounds.x, a.getBounds.y-3);
  }
}
