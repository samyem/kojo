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

package net.kogics.kojo
package language

import java.io.File
import javax.swing.Action
import javax.swing.JOptionPane

import util.Utils
import util.RichFile._

object LangChanger {
  var actions: List[Action] = Nil
  implicit lazy val klass = getClass
  lazy val cf = new File(new File("%s/../etc/kojo.conf" format(Utils.installDir)).getAbsolutePath)
  lazy val regex = """-J-Duser.language=(\w+)""".r
  def currLang: Option[String] = {
    regex.findFirstMatchIn(cf.readAsString).map {_ group(1)}
  }
  lazy val initLang = currLang
}

trait LangChanger {
  import LangChanger._

  def updateMenus(curr: Action) {
    actions.foreach { e => if (e == curr) e.setEnabled(false) else e.setEnabled(true) }
  }
  
  def langDisplay(lang: Option[String]) = lang match {
    case Some(l) if (l == "en") => "English"
    case Some(l) if (l == "sv") => "Swedish"
    case None => "Default"
  }
  
  def displayNoChange(lang: Option[String]) {
    JOptionPane.showMessageDialog(null, 
                                  Utils.loadString("S_NOCHANGE") format(langDisplay(lang)), 
                                  Utils.loadString("S_NOCHANGE_T"), 
                                  JOptionPane.INFORMATION_MESSAGE)
  }
  
  def confirmChange(lang: Option[String]) {
    val curlang = currLang
    if (curlang == lang) {
      JOptionPane.showMessageDialog(null, 
                                    Utils.loadString("S_CHANGED") format(langDisplay(lang)), 
                                    Utils.loadString("S_CHANGED_T"), 
                                    JOptionPane.INFORMATION_MESSAGE)
//      Auto restart does not pick up the change to the conf file.
//      Utils.schedule(5) {
//        LifecycleManager.getDefault().markForRestart()
//        LifecycleManager.getDefault().exit()
//      }
    }
    else if (curlang == None) {
      println("Unable to change your language to %s by modifying the kojo.conf file." format(langDisplay(lang)))
      println("Make sure that your kojo.conf file has the '-J-Duser.defLanguage=true' flag within default_options.")
      showSiteLink()
    }
    else {
      println("Unable to change your language to %s by modifying the kojo.conf file." format(langDisplay(lang)))
      showSiteLink()
    }
  }
  
  def setLang(lang: String): Boolean = currLang match {
    case Some(cl) if cl == lang => false
    case Some(_) => 
      setLangHelper { _ replaceAll("""(-J-Duser.language)=\w+""", "$1=%s" format(lang)) }
      true
    case None => 
      setLangHelper { _ replaceAll("""-J-Duser.defLanguage=true""", "-J-Duser.language=%s" format(lang)) }
      true
  }
  
  def setDefLang(): Boolean = currLang match {
    case Some(lang) => 
      setLangHelper { _ replaceAll("""-J-Duser.language=\w+""", "-J-Duser.defLanguage=true") }
      true
    case None => false
  }
  
  def setLangHelper(mod: String => String) {
    val confFile = cf.readAsString
    val newCf = mod(confFile)
    cf.write(newCf)
  }
  
  def showSiteLink() {
    println("\nMore information on changing the Language used by Kojo is available on this page: " + "http://wiki.kogics.net/sf:lang-support")
  }
}
