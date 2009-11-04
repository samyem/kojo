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

import scala.collection._
import java.io._

trait HistoryListener {
  def itemAdded
  def selectionChanged(n: Int)
}

class HistorySaver {
  var writer = new BufferedWriter(new FileWriter(CommandHistory.historyFile, true))

  def append(script: String) = {
    writer.append(script)
    writer.append(CommandHistory.Seperator)
    writer.flush()
  }

  def write(items: Array[String]) {
    writer.close()
    writer = new BufferedWriter(new FileWriter(CommandHistory.historyFile, false))
    items.foreach { hItem =>
      writer.append(hItem)
      writer.append(CommandHistory.Seperator)
    }
    writer.close()
    writer = new BufferedWriter(new FileWriter(CommandHistory.historyFile, true))
  }
}

object CommandHistory extends Singleton[CommandHistory] {
//  val Log = java.util.logging.Logger.getLogger("CommandHistoryO");
  val Seperator = "---Seperator---"
  val MaxHistorySize = 1000

  val historyFile = {
    val userDir = System.getProperty("netbeans.user")
    val historyFileName = userDir + File.separator + "config" + File.separator + "history.txt"
    new File(historyFileName)
  }

  def loadHistory(): String = {
    if (!historyFile.exists) {
      historyFile.createNewFile()
      ""
    }
    else {
      import net.kogics.kojo.util.RichFile._
      historyFile.readAsString
    }
  }

  protected def newInstance = {
    val hist = new CommandHistory(new HistorySaver(), CommandHistory.MaxHistorySize)
    hist.loadFrom(loadHistory())
    hist
  }
}

class CommandHistory private[kojo] (historySaver: HistorySaver, maxHistorySize: Int) {
  val Log = java.util.logging.Logger.getLogger(getClass.getName)

  val history = new mutable.ArrayBuffer[String]
  var hIndex = 0
  var listener: Option[HistoryListener] = None

  def setListener(l: HistoryListener) {
    if (listener.isDefined) throw new IllegalArgumentException("Listener already defined")
    listener = Some(l)
  }

  def clearListener() {
    listener = None
  }

  def internalAdd(code: String) {
    history += code
    hIndex = history.size
  }

  def add(code: String) {
    internalAdd(code)
    if(listener.isDefined) listener.get.itemAdded
    historySaver.append(code)
  }

  def hasPrevious = hIndex > 0

  def toPosition(idx: Int): Option[String] = {
    if (idx < 0 || idx > size-1) None
    else {
      hIndex = idx
      if(listener.isDefined) listener.get.selectionChanged(hIndex)
      Some(history(hIndex))
    }
  }

  def previous: Option[String] = {
    if (hIndex == 0) None
    else {
      hIndex -= 1
      if(listener.isDefined) listener.get.selectionChanged(hIndex)
      Some(history(hIndex))
    }
  }

  def next: Option[String] = {
    if (hIndex == history.size) None
    else {
      hIndex += 1
      if(listener.isDefined) listener.get.selectionChanged(hIndex)
      if (hIndex == history.size) None
      else Some(history(hIndex))
    }
  }

  def size = history.size
  def apply(idx: Int) = history(idx)
  
  def clear {
    history.clear
    hIndex = -1
    listener = None
  }

  def loadFrom(stringHistory: String) {
//    Log.info("Loading History from: " + stringHistory)
    if (stringHistory == null || stringHistory.trim() == "") return
    
    var items = stringHistory.split(CommandHistory.Seperator)
    if (items.length > maxHistorySize) {
      items = items.slice(items.size - maxHistorySize, items.size)
      historySaver.write(items)
    }
    items.foreach {hItem => internalAdd(hItem)}
  }
}
