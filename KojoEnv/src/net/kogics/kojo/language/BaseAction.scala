/*
 * Copyright (C) 2012 Lalit Pant <pant.lalit@gmail.com>
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

package net.kogics.kojo.language

import java.awt.event.ActionEvent
import java.io.FileNotFoundException
import javax.swing.AbstractAction

abstract class BaseAction(thisLang: Option[String]) extends AbstractAction with LangChanger {
  import LangChanger._
  actions = this :: actions

  if (initLang == thisLang) {
    setEnabled(false)
  }
  
  def actionPerformed(e: ActionEvent) {
    try {
      thisLang match {
        case Some(l) => setLang(l)
        case None => setDefLang()
      }
      
      confirmChange(thisLang)
      // displayNoChange(...) // if ret false
      updateMenus(this)
    }
    catch {
      case fnf: FileNotFoundException => 
        println("Kojo is unable to update your kojo.conf file. This might be because you don't have sufficient permissions to modify the file. Try running Kojo as an administrator, and then change your Language setting.")
        println("\nMore details about the problem:\n%s" format (fnf.getMessage))
        showSiteLink()
      case t: Throwable => 
        println("Problem updating kojo.conf file:\n%s" format (t.getMessage))
        showSiteLink()
    }
  }
}
