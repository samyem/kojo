/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.kogics.kojo

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._

import java.io.File
import net.kogics.kojo.util._

class CodeExecutionSupportTest extends KojoTestBase {

  val netbeansDir = System.getProperty("nbjunit.workdir") + "../../../../../../../Kojo/build/cluster"
  val userDir = System.getProperty("nbjunit.workdir") + "../../../../../../../Kojo/build/testuserdir"
  val nbd = new File(netbeansDir)
  assertTrue(nbd.exists)
  // make sure user config dir exists
  val configDir = new File(userDir + File.separator + "config")
  if (!configDir.exists) configDir.mkdirs()

  System.setProperty("netbeans.dirs", netbeansDir)
  System.setProperty("netbeans.user", userDir)

  try {
    val ce0 = CodeExecutionSupport.instance()
  }
  catch {
    case e: IllegalStateException => assertTrue(true)
  }

  val ce = CodeExecutionSupport.initedInstance(new javax.swing.JEditorPane(), new org.openide.awt.UndoRedo.Manager)
  val pane = ce.codePane

  val commandHistory = CommandHistory.instance
  commandHistory.setListener(new HistoryListener {
      def itemAdded {
        ce.loadCodeFromHistory(commandHistory.size)
      }

      def selectionChanged(n: Int) {
        ce.loadCodeFromHistory(n)
      }
    })


  @Test
  def testRunAllText {
    pane.setText("val x = 10; val y = 20")
    val output = ce.runCodeWithOutputCapture()
    assertEquals("x: Int = 10y: Int = 20", stripCrLfs(output))
    Utils.runInSwingThreadAndWait {  /* noop */  }
    assertEquals("", pane.getText)
    assertTrue(pane.getSelectionStart == pane.getSelectionEnd)
  }

  @Test
  def testRunAllTextWithError {
    val code = "val x = 10; val y = 20x"
    pane.setText(code)
    val output = ce.runCodeWithOutputCapture()
    assertTrue(output.contains(": error:"))
    Utils.runInSwingThreadAndWait {  /* noop */  }
    assertEquals(code, pane.getText)
    assertTrue(pane.getSelectionStart == pane.getSelectionEnd)
  }

  @Test
  def testRunSelectedText {
    val code = "val x = 10; val y = 20"
    pane.setText(code)
    pane.setSelectionStart(12)
    pane.setSelectionEnd(22)

    val output = ce.runCodeWithOutputCapture()
    assertEquals("y: Int = 20", stripCrLfs(output))
    Utils.runInSwingThreadAndWait {  /* noop */  }
    assertEquals(code, pane.getText)
    assertEquals(12, pane.getSelectionStart)
    assertEquals(22, pane.getSelectionEnd)
  }

  @Test
  def testRunSelectedTextWithError {
    val code = "val x = 10; val y = 20x"
    pane.setText(code)
    pane.setSelectionStart(12)
    pane.setSelectionEnd(23)

    val output = ce.runCodeWithOutputCapture()
    assertTrue(output.contains(": error:"))
    Utils.runInSwingThreadAndWait {  /* noop */  }
    assertEquals(code, pane.getText)
    assertEquals(12, pane.getSelectionStart)
    assertEquals(23, pane.getSelectionEnd)
  }

  def stripCrLfs(str: String): String  = {
    val str0 = str.replaceAll("\r?\n", "")
    str0.replaceAll("---", "")
  }

  @Test
  def testIsSingleLine {
    val code = "12"
    assertTrue(ce.isSingleLine(code))
  }

  @Test
  def testIsSingleLine2 {
    val code = "12\n"
    assertTrue(ce.isSingleLine(code))
  }

  @Test
  def testIsSingleLine3 {
    val code = "12\n14"
    assertFalse(ce.isSingleLine(code))
  }

  @Test
  def testIsSingleLine4 {
    val code = sample.SampleCode.Rangoli
    assertFalse(ce.isSingleLine(code))
  }
}
