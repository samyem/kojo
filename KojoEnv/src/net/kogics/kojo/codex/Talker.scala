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

package net.kogics.kojo.codex

import net.kogics.swill.Conversation
import org.openide.util.NbBundle

trait TalkListener {
  def onEvent(msg: String)
}

class Talker(email: String, password: String, listener: TalkListener) {
  val server = "http://localhost"

  def upload(title: String, code: String, file: java.io.File) {
    val conv = new Conversation
    listener.onEvent(NbBundle.getMessage(classOf[Talker], "Talker.login"))
    conv.go(server + "/login")
    conv.formField("email", email)
    conv.formField("password", password)
    conv.formSubmit()
    try {
      conv.find("Login Succeeded")
      listener.onEvent(NbBundle.getMessage(classOf[Talker], "Talker.login.success"))
    }
    catch {
      case ex: RuntimeException => listener.onEvent(NbBundle.getMessage(classOf[Talker], "Talker.login.error")); return
      case t: Throwable => listener.onEvent(t.getMessage); return
    }

    listener.onEvent(NbBundle.getMessage(classOf[Talker], "Talker.upload.init"))
    try {
      conv.go(server + "/codeupload")
    }
    catch {
      case t: Throwable => listener.onEvent(t.getMessage); return
    }
    conv.formField("title", title)
    conv.formField("code", code)
    conv.formField("image", file)
    listener.onEvent(NbBundle.getMessage(classOf[Talker], "Talker.upload.start"))
    conv.formSubmit()
    try {
      conv.find("Code Exchange")
      listener.onEvent(NbBundle.getMessage(classOf[Talker], "Talker.upload.success"))
    }
    catch {
      case ex: RuntimeException => listener.onEvent(NbBundle.getMessage(classOf[Talker], "Talker.upload.error")); return
      case t: Throwable => listener.onEvent(t.getMessage); return
    }
  }
}
