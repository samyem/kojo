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
package net.kogics.kojo.story

trait Viewable {
  def hasNextView: Boolean
  def forward(): Unit
  def view: xml.Node
  def hasPrevView: Boolean
  def back(): Unit
}

case class StaticPage(body: xml.Node) extends Viewable {
  def hasNextView = false
  def hasPrevView = false
  def view = body
  def forward() = new IllegalStateException("Can't go forward on a Static page")
  def back() = new IllegalStateException("Can't go back on a Static page")
}

object Para {
  def apply(html: xml.Node, code: => Unit = {}) = new Para(html, code)
}
class Para(val html: xml.Node, code0: => Unit) {
  def code = code0
}
case class DynamicPage(style: String, body: Para*) extends Viewable {
  @volatile var currPara = 1
  def paras = body.size

  private def viewParas(n: Int) = {
    <div style={style}>
      {body.take(n).map {para => para.html}}
    </div>
  }
  
  private def runCode(n: Int) {
    body(n-1).code
  }

  def hasNextView = currPara < paras
  def hasPrevView = currPara > 1

  def forward() {
    currPara += 1
    if (currPara > paras) throw new IllegalStateException("Gone past view range")
  }

  def back() {
    currPara -= 1
    if (currPara < 1) throw new IllegalStateException("Gone past view range")
  }

  def view = {
    runCode(currPara)
    viewParas(currPara)
  }
}

case class Story(pages: Viewable*) extends Viewable {
  var currPage = 0

  def hasNextView: Boolean = {
    val b1 = pages(currPage).hasNextView
    if (b1) {
      true
    }
    else {
      if (currPage + 1 < pages.size) {
        true
      }
      else {
        false
      }
    }
  }

  def hasPrevView: Boolean = {
    val b1 = pages(currPage).hasPrevView
    if (b1) {
      true
    }
    else {
      if (currPage > 0) {
        true
      }
      else {
        false
      }
    }
  }

  def forward() {
    if (pages(currPage).hasNextView) {
      pages(currPage).forward()
    }
    else {
      currPage += 1
    }
  }

  def back() {
    if (pages(currPage).hasPrevView) {
      pages(currPage).back()
    }
    else {
      currPage -= 1
    }
  }

  def view = {
    pages(currPage).view
  }
}
