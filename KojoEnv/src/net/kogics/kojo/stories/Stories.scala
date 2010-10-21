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
package stories

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io._

class Stories extends ActionListener {
  def actionPerformed(e: ActionEvent) {
    val ces = CodeExecutionSupport.instance()
    ces.codePane.setText(getCode(e).trim())
    ces.codePane.setCaretPosition(0)
    CodeEditorTopComponent.findInstance().requestActive()
  }

  def getCode(e: ActionEvent) = {
    e.getActionCommand match {
      case "Creating Stories" => util.Utils.readFile(storyStream("creating-stories.kojo"))
    }
  }

  def storyStream(fname: String) = {
    val installDir = System.getProperty("user.dir")
//    println("***** user dir: " + installDir)
    val base = installDir + File.separator + "stories"
    CodeEditorTopComponent.findInstance().setLastLoadStoreDir(base)
    new FileInputStream(base + File.separator + fname)
  }
}
