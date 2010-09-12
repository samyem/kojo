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
import net.kogics.kojo.util.Utils

trait TalkListener {
  def onStart()
  def onEvent(msg: String)
  def onFinish(success: Boolean)
}

object Talker {
//  val server = "http://localhost"
  val server = "http://www.kogics.net"
}

class Talker(email: String, password: String, listener: TalkListener) {

  def fireEvent(msg: String) {
    Utils.runInSwingThread {
      listener.onEvent(msg)
    }
  }

  def fireStart() {
    Utils.runInSwingThread {
      listener.onStart()
    }
  }

  def fireFinish(success: Boolean) {
    Utils.runInSwingThread {
      listener.onFinish(success)
    }
  }

  def fireProblem(msg: String) {
    fireEvent(msg);
    fireFinish(false)
  }

  def upload(title: String, code: String, file: java.io.File) {
    val uploadRunner = new Runnable {
      def run {
        fireStart()
        Conversation.server = Talker.server
        val conv = new Conversation
        fireEvent(NbBundle.getMessage(classOf[Talker], "Talker.login"))
        try {
          conv.go("/login")
        }
        catch {
          case t: Throwable => fireProblem(t.getMessage); return
        }
        conv.formField("email", email)
        conv.formField("password", password)
        try {
          conv.formSubmit()
          // redirects to code exchange page after login
          conv.title("Code Exchange")
          fireEvent(NbBundle.getMessage(classOf[Talker], "Talker.login.success"))
        }
        catch {
          case ex: RuntimeException => fireProblem(NbBundle.getMessage(classOf[Talker], "Talker.login.error")); return
          case t: Throwable => fireProblem(t.getMessage); return
        }

        fireEvent(NbBundle.getMessage(classOf[Talker], "Talker.upload.init"))
        try {
          conv.go("/codeupload")
        }
        catch {
          case t: Throwable => fireProblem(t.getMessage); return
        }
        conv.formField("title", title)
        conv.formField("code", code)
        conv.formField("image", file)
        fireEvent(NbBundle.getMessage(classOf[Talker], "Talker.upload.start"))
        try {
          conv.formSubmit()
          conv.title("Code Exchange")
          fireEvent(NbBundle.getMessage(classOf[Talker], "Talker.upload.success"))
          fireFinish(true)
        }
        catch {
          case ex: RuntimeException => fireProblem(NbBundle.getMessage(classOf[Talker], "Talker.upload.error")); return
          case t: Throwable => fireProblem(t.getMessage); return
        }
      }
    }
    new Thread(uploadRunner).start()
  }
}
