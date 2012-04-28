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
package net.kogics.kojo.livecoding

import java.util.regex.Pattern
import javax.swing.JLabel
import javax.swing.JSlider
import javax.swing.JToggleButton
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.text.Document
import net.kogics.kojo.util.Utils
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.scala.core.lexer.ScalaTokenId;

class IntManipulator(ctx: ManipulationContext) extends NumberManipulator(ctx) {
  val MY_SPECIAL_PATTERN = Pattern.compile("(\\d*)")

  def isHyperlinkPoint(doc: Document, offset: Int): Boolean = {
    try {
      val hi = TokenHierarchy.get(doc);
      val ts = hi.tokenSequence(ScalaTokenId.language);

      if (ts != null) {
        ts.move(offset);
        ts.moveNext()
        val tok = ts.token()
        // TODO just check token type instead of doing a regex match
        val newOffset = ts.offset()
        val matcherText = tok.text().toString()
        val m = MY_SPECIAL_PATTERN.matcher(matcherText)
        if (m.matches()) {
          target = m.group(1)
          val idx = matcherText.indexOf(target)
          targetStart = newOffset + idx
          targetEnd = targetStart + target.length();
          return true;
        }
      }
      return false;
    } catch {
      case t: Throwable => false
    }
  }
  
  def getHyperlinkSpan(doc: Document, offset: Int): Array[Int] = {
    Array(targetStart, targetEnd)
  }
  
  def activate(doc: Document, offset: Int) {
    activate(doc, offset, target, targetStart)
  }
  
  def activate(doc: Document, offset: Int, target0: String, targetStart: Int) = Utils.safeProcess {
    close()
    ctx.addManipulator(this)
    var target = target0
    var ntarget = target.toInt
    val slider = new JSlider();
    val leftLabel = new JLabel
    val rightLabel = new JLabel
    def reConfigSlider(around: Int, zoomB: JToggleButton) {
      slider.setMinimum(0)
      slider.setMaximum(math.max(around * 2, 18))
      slider.setMajorTickSpacing(math.max(math.floor(around * 2.0 / 10).toInt, 1))
      leftLabel.setText(slider.getMinimum.toString)
      rightLabel.setText(slider.getMaximum.toString)
      if (slider.getMajorTickSpacing == 1) {
        zoomB.setEnabled(false)
      }
      else {
        zoomB.setEnabled(true)
      }
    }
    slider.setValue(ntarget)
    slider.setPaintTicks(true)
      
    var lastrunval = ntarget
    slider.addChangeListener(new ChangeListener {
        def stateChanged(e: ChangeEvent) = Utils.safeProcess {
          val eslider = e.getSource.asInstanceOf[JSlider]
          ntarget = eslider.getValue()
          inSliderChange = true
          doc.remove(targetStart, target.length())
          target = ntarget.toString
          doc.insertString(targetStart, target, null);
          inSliderChange = false
            
          if (!eslider.getValueIsAdjusting) {
            // drag over
            if (ctx.isRunningEnabled) {
              if (lastrunval != ntarget) {
                lastrunval = ntarget
                Utils.invokeLaterInSwingThread {
                  ctx.runCode(doc.getText(0, doc.getLength))
                }
              }
            }
            else {
              eslider.setValue(lastrunval)
            }
          }
        }
      })
    
    val zoomListener = { zoomB: JToggleButton => 
      if (zoomB.isSelected) {
        val sval = slider.getValue
        slider.setMinimum(sval - 9)
        slider.setMaximum(sval + 9)
        slider.setMajorTickSpacing(1)
        leftLabel.setText(slider.getMinimum.toString)
        rightLabel.setText(slider.getMaximum.toString)
      }
      else {
        reConfigSlider(slider.getValue, zoomB)
      }
    }
    showPopup(offset, leftLabel, slider, rightLabel, zoomListener)
  }
}
