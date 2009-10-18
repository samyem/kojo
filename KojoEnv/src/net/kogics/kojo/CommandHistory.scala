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

trait HistoryListener {
  def itemAdded
  def selectionChanged(n: Int)
}

object CommandHistory extends Singleton[CommandHistory] {
  protected def newInstance = new CommandHistory
}

class CommandHistory private[kojo] {
  val Log = java.util.logging.Logger.getLogger(getClass.getName);

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

  def add(code: String) {
    history += code
    hIndex = history.size
    if(listener.isDefined) listener.get.itemAdded
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

  val Seperator = "---Seperator---"
  def asString: String = {
    val sb = new StringBuilder
    history.foreach { hitem =>
      sb.append(hitem.replaceAll("\n", "---LineBreak---"))
      sb.append(Seperator)
    }
    sb.toString
  }

  def loadFrom(stringHistory: String) {
//    Log.info("Loading History from: " + stringHistory)
    if (stringHistory == null) return
    
    val items = stringHistory.split(Seperator)
    items.foreach {hItem => add(hItem.replaceAll("---LineBreak---", "\n"))}
  }
}
