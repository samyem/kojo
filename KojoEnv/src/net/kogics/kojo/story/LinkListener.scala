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

import java.awt.Desktop
import javax.swing._
import javax.swing.event._

class LinkListener(st: StoryTeller) extends HyperlinkListener {
  val linkRegex = """(?i)http://localpage/(\d+)#?(\d*)""".r

  def location(url: String): (Int, Int) = {
    url match {
      case linkRegex(page, para) =>
        (page.toInt, if (para=="") 1 else para.toInt)
      case _ =>
        throw new IllegalArgumentException()
    }
  }

  def hyperlinkUpdate(e: HyperlinkEvent) {
    if (e.getEventType == HyperlinkEvent.EventType.ACTIVATED) {
      val url = e.getURL
      if (url.getProtocol == "http") {
        if (url.getHost.toLowerCase == "localpage") {
          try {
            val loc = location(url.toString)
            st.viewPage(loc._1, loc._2)
          }
          catch {
            case ex: IllegalArgumentException =>
              st.showStatusError("Invalid page/view in Link - " + url.toString)
          }
        }
        else {
          Desktop.getDesktop().browse(url.toURI)
        }
      }
      else {
        st.showStatusError("Trying to use link with unsupported protocol - " + url.getProtocol)
      }
    }
    else if (e.getEventType == HyperlinkEvent.EventType.ENTERED) {
      st.showStatusMsg(e.getURL.toString, false)
    }
    else if (e.getEventType == HyperlinkEvent.EventType.EXITED) {
      st.clearStatusBar()
    }
  }
}
