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
  def hyperlinkUpdate(e: HyperlinkEvent) {
    if (e.getEventType == HyperlinkEvent.EventType.ACTIVATED) {
      val url = e.getURL
      if (url.getHost == "localpage") {
        st.viewPage(url.getPath.substring(1), url.getRef)
      }
      else {
        Desktop.getDesktop().browse(url.toURI)
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
