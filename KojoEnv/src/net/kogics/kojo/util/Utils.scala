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
package net.kogics.kojo.util

import java.awt._
import javax.swing._
import java.awt.event.{ActionListener, ActionEvent}
import java.io._
import scala.{math => Math}

object Utils {

  def loadImage(fname: String) : Image = {
    val url = getClass.getResource(fname)
    Toolkit.getDefaultToolkit.getImage(url)
  }

  def loadIcon(fname: String, desc: String = "") : ImageIcon = {
    new ImageIcon(loadImage(fname), desc)
  }

  def inSwingThread = EventQueue.isDispatchThread

  def runAsync(fn: => Unit) {
    new Thread(new Runnable {
        def run {
          fn
        }
      }).start
  }

  def runInSwingThread(fn: => Unit) {
    if(inSwingThread) {
      fn
    }
    else {
      javax.swing.SwingUtilities.invokeLater(new Runnable {
          override def run {
            fn
          }
        })
    }
  }

  def runInSwingThreadAndWait[T](fn: => T): T = {
    if(inSwingThread) {
      fn
    }
    else {
      var t: T = null.asInstanceOf[T]
      javax.swing.SwingUtilities.invokeAndWait(new Runnable {
          override def run {
            t = fn
          }
        })
      t
    }
  }

  def doublesEqual(d1: Double, d2: Double, tol: Double): Boolean = {
    if (d1 == d2) return true
    else if (Math.abs(d1 - d2) < tol) return true
    else return false
  }

  def schedule(secs: Double)(f: => Unit): Timer = {
    lazy val t: Timer = new Timer((secs * 1000).toInt, new ActionListener {
        def actionPerformed(e: ActionEvent) {
          t.stop
          f
        }
      })
    t.start
    t
  }

  def scheduleRec(secs: Double)(f: => Unit): Timer = {
    val t: Timer = new Timer((secs * 1000).toInt, new ActionListener {
        def actionPerformed(e: ActionEvent) {
          f
        }
      })
    t.start
    t
  }

  def replAssertEquals(a: Any, b: Any) {
    if (a != b) println("Not Good. First: %s, Second: %s" format (a.toString, b.toString))
    else println("Good")
  }

  // actually - the dir with the jars, one level under the actual install dir
  def installDir = {
    val dirs = System.getProperty("netbeans.dirs")
    dirs.split(File.pathSeparator).find {n =>
      new File(n, "modules/ext/scala-library.jar").exists
    }.getOrElse {throw new IllegalStateException("Unknown Install Dir")}
  }

  def readFile(is: InputStream): String = {
    val reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))
    val buf = new Array[Char](1024)
    var nbytes = reader.read(buf)
    val sb = new StringBuffer
    while (nbytes != -1) {
      sb.append(buf, 0, nbytes)
      nbytes = reader.read(buf)
    }
    reader.close()
    sb.toString
  }

  def stackTraceAsString(t: Throwable): String = {
    val result = new StringWriter()
    val printWriter = new PrintWriter(result)
    t.printStackTrace(printWriter)
    result.toString()
  }

  def deg2radians(angle: Double) = angle * Math.Pi / 180
  def rad2degrees(angle: Double) = angle * 180 / Math.Pi

  def stripCR(str: String) = str.replaceAll("\r\n", "\n")

  import org.openide.util.NbBundle

  case class BundleMessage(klass: Class[_], key: String) {
    def unapply(action: String): Option[Boolean] = {
      try {
        if (NbBundle.getMessage(klass, key) == action) Some(true) else None
      }
      catch {
        case e: Throwable => None
      }
    }
  }
}
