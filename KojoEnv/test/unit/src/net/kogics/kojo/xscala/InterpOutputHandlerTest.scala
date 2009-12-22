/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.kogics.kojo.xscala

import org.junit._
import Assert._

import org.junit.runner.RunWith

import org.jmock.integration.junit4.{JMock, JUnit4Mockery=>Mockery}
import org.jmock.Expectations
import org.jmock.Expectations._
import org.jmock.lib.legacy.ClassImposteriser

import net.kogics.kojo.core.RunContext

@RunWith(classOf[JMock])
class InterpOutputHandlerTest {

  val context: Mockery = new Mockery() {
    {
      setImposteriser(ClassImposteriser.INSTANCE)
    }
  }

  @Test
  def testSimpleOutput = {
    val output = "plain text\n"
    val runCtx = (context.mock(classOf[RunContext])).asInstanceOf[RunContext]
    context.checking (new Expectations {
        one(runCtx).reportOutput(output)
      })

    val outputHandler = new InterpOutputHandler(runCtx)
    outputHandler.reportInterpOutput(output)
  }

  @Test
  def testSimpleOutputTwice = {
    val output = "plain text\n"
    val output2 = "plain text2\n"
    val runCtx = (context.mock(classOf[RunContext])).asInstanceOf[RunContext]
    context.checking (new Expectations {
        one(runCtx).reportOutput(output)
        one(runCtx).reportOutput(output2)
      })

    val outputHandler = new InterpOutputHandler(runCtx)
    outputHandler.reportInterpOutput(output)
    outputHandler.reportInterpOutput(output2)
  }

  @Test
  def testErrorPattern1 = {
    val output1 = "<console>:1: error: Invalid literal number\n"
    val output2 = "       12x\n"
    val output3 = "       ^\n"

    val runCtx = (context.mock(classOf[RunContext])).asInstanceOf[RunContext]
    context.checking (new Expectations {
        one(runCtx).reportErrorMsg(output1)
        one(runCtx).reportErrorText(output2)
        one(runCtx).reportOutput(output3)
      })

    val outputHandler = new InterpOutputHandler(runCtx)
    outputHandler.reportInterpOutput(output1)
    outputHandler.reportInterpOutput(output2)
    outputHandler.reportInterpOutput(output3)
  }

  @Test
  def testErrorPattern2 = {
    val output1 = "<console>:8: error: not enough arguments for constructor Range: (start: Int,end: Int,step: Int)scala.collection.immutable.Range.\nUnspecified value parameters start, end, step.\n"
    val output2 = "val l = new Range\n"
    val output3 = "        ^\n"

    val runCtx = (context.mock(classOf[RunContext])).asInstanceOf[RunContext]
    context.checking (new Expectations {
        one(runCtx).reportErrorMsg(output1)
        one(runCtx).reportErrorText(output2)
        one(runCtx).reportOutput(output3)
      })

    val outputHandler = new InterpOutputHandler(runCtx)
    outputHandler.reportInterpOutput(output1)
    outputHandler.reportInterpOutput(output2)
    outputHandler.reportInterpOutput(output3)
  }
}
