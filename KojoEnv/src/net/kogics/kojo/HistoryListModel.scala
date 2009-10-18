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
import java.awt._

class HistoryListModel(myList: JList) extends AbstractListModel {
  val commandHistory = CommandHistory.instance

  myList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
  def getSize = commandHistory.size + 1

  def getElementAt(idx: Int) = {
    if (idx == commandHistory.size) ""
    else commandHistory(idx)
  }
  
  commandHistory.setListener(new HistoryListener {
      def itemAdded {
        fireIntervalAdded(HistoryListModel.this,commandHistory.size-1, commandHistory.size-1)
        myList.setSelectedIndex(getSize-1)
        myList.ensureIndexIsVisible(getSize-1)
      }

      def selectionChanged(n: Int) {
        val currSelIndex = myList.getSelectedIndex
        if (currSelIndex != n) {
          myList.setSelectedIndex(n)
          myList.ensureIndexIsVisible(n)
        }
      }
    })
}



