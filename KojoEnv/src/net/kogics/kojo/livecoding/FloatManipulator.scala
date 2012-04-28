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

class FloatManipulator(ctx: ManipulationContext) extends NumberManipulator(ctx) {
  val MY_SPECIAL_PATTERN = Pattern.compile("""(\d*\.\d\d?)""")

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
    var delta = 0.1
    var formatter = "%.2f"
    var target = target0
    var ncenter = target0.toDouble
    var ntarget = ncenter
    val slider = new JSlider();
    val leftLabel = new JLabel
    val rightLabel = new JLabel
    def slider2double(n: Int) = {
      ncenter + (n-9) * delta
    }
    def double2slider(n: Double): Int = {
      9 + math.round((n - ncenter) / delta).toInt
    }
    def uiDouble(s: String) = {
      val uid = Utils.stripTrailingChar(s, '0')
      if (uid.endsWith(".")) uid + "0" else uid
    }
    
    def reConfigSlider(around: Double) {
      ncenter = around
      slider.setValue(9)
      slider.setMinimum(0)
      slider.setMaximum(18)
      slider.setMajorTickSpacing(1)
      leftLabel.setText(uiDouble(formatter format slider2double(slider.getMinimum)))
      rightLabel.setText(uiDouble(formatter format slider2double(slider.getMaximum)))
    }
    slider.setValue(9)
    slider.setPaintTicks(true)
      
    var lastrunval = ntarget
    slider.addChangeListener(new ChangeListener {
        def stateChanged(e: ChangeEvent) = Utils.safeProcess {
          val eslider = e.getSource.asInstanceOf[JSlider]
          val newnum = eslider.getValue()
          inSliderChange = true
          doc.remove(targetStart, target.length())
          ntarget = slider2double(newnum)
          target = uiDouble(formatter format ntarget)
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
              eslider.setValue(double2slider(lastrunval))
            }
          }
        }
      })

    val zoomListener = { zoomB: JToggleButton => 
      val around = slider2double(slider.getValue)
      if (zoomB.isSelected) {
        delta = 0.01
        zoomB.setToolTipText("Increase Slider Stepsize")
      }
      else {
        delta = 0.1
        zoomB.setToolTipText("Decrease Slider Stepsize")
      }
      reConfigSlider(around)
    }
    showPopup(offset, leftLabel, slider, rightLabel, zoomListener)
  }
}
