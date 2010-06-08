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
package staging

//import org.junit.After
//import org.junit.Before
import org.junit.Test
import org.junit.Assert._

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{CountDownLatch, TimeUnit}

import net.kogics.kojo.core.RunContext

import net.kogics.kojo.util._

// cargo coding off CodePaneTest
class StagingTestBase extends KojoTestBase {
  val fileStr = System.getProperty("nbjunit.workdir") + "../../../../../../../Kojo/build/cluster"
  val file = new java.io.File(fileStr)
  assertTrue(file.exists)
  System.setProperty("netbeans.dirs", fileStr)

  val runCtx = new TestRunContext(this)

  val codeRunner = new xscala.ScalaCodeRunner(runCtx, SpriteCanvas.instance, geogebra.GeoGebraCanvas.instance.geomCanvas)
  val pane = new javax.swing.JEditorPane()
  val Delimiter = ""

  var latch: CountDownLatch = _
  def runCode() {
    latch = new CountDownLatch(1)
    codeRunner.runCode(pane.getText())
    latch.await()
  }

  def scheduleInterruption() {
    new Thread(new Runnable {
        def run() {
          Thread.sleep(1000)
          codeRunner.interruptInterpreter()
        }
      }).start()
  }

  type PNode = edu.umd.cs.piccolo.PNode
  type PPath = edu.umd.cs.piccolo.nodes.PPath

  def makeString(pnode: PNode) = {
    val x = pnode.getX.round + 1
    val y = pnode.getY.round + 1
    if (pnode.isInstanceOf[kgeom.PArc]) {
      "PArc(" + x + "," + y + ")"
    }
    else if (pnode.isInstanceOf[kgeom.PPoint]) {
      "PPoint(" + x + "," + y + ")"
    }
    else if (pnode.isInstanceOf[kgeom.PolyLine]) {
      "PolyLine(" + (x + 1) + "," + (y + 1) + ")"
    }
    else if (pnode.isInstanceOf[PPath]) {
      "PPath(" + x + "," + y + ")"
    }
    else pnode.toString
  }

}

