/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.kogics.kojo

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._

class CodeEditorTest {

  val netbeansDir = System.getProperty("nbjunit.workdir") + "../../../../../../../Kojo/build/cluster"
  val userDir = System.getProperty("nbjunit.workdir") + "../../../../../../../Kojo/build/testuserdir"
  val file = new java.io.File(netbeansDir)
  assertTrue(file.exists)
  System.setProperty("netbeans.dirs", netbeansDir)
  System.setProperty("netbeans.user", userDir)
  val ce = CodeEditor.instance
  val pane = ce.codePane
  val output = ce.output

  // run something and get rid of welcome message
  pane.setText("3")
  ce.runCode()
  awaitResult("3")

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
    ce.clrOutput()
    ce.runCode()
    awaitResult("20")
    assertEquals("x: Int = 10y: Int = 20", stripCrLfs(output.getText))
    assertEquals("", pane.getText)
    assertTrue(pane.getSelectionStart == pane.getSelectionEnd)
  }

  @Test
  def testRunAllTextWithError {
    val code = "val x = 10; val y = 20x"
    pane.setText(code)
    ce.clrOutput()
    ce.runCode()
    awaitResult("20x")
    assertEquals(code, pane.getText)
    assertTrue(pane.getSelectionStart == pane.getSelectionEnd)
  }

  @Test
  def testRunSelectedText {
    val code = "val x = 10; val y = 20"
    pane.setText(code)
    pane.setSelectionStart(12)
    pane.setSelectionEnd(22)

    ce.clrOutput()
    ce.runCode()
    awaitResult("20")
    assertEquals("y: Int = 20", stripCrLfs(output.getText))
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

    ce.clrOutput()
    ce.runCode()
    awaitResult("20x")
    assertEquals(code, pane.getText)
    assertEquals(12, pane.getSelectionStart)
    assertEquals(23, pane.getSelectionEnd)
  }

  def stripCrLfs(str: String) = str.replaceAll("\r?\n", "")

  def awaitResult(res: String) {
    var totalSleep = 0l
    val sleepTime = 100
    Thread.sleep(sleepTime)
    totalSleep += sleepTime
    var outText = output.getText
    while (!outText.contains(res)) {
      if (totalSleep > 6000) throw new RuntimeException("Timeout waiting for: " + res)
      Thread.sleep(sleepTime)
      totalSleep += sleepTime
      outText = output.getText
    }
  }
}
