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

import java.awt.Color
import java.awt.Point
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.regex.Pattern
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSlider
import javax.swing.JToggleButton
import javax.swing.Popup
import javax.swing.PopupFactory
import javax.swing.SwingUtilities
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.text.Document
import net.kogics.kojo.util.Utils
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.scala.core.lexer.ScalaTokenId;

class FloatManipulator(ctx: ManipulationContext) extends InteractiveManipulator {
  val MY_SPECIAL_PATTERN = Pattern.compile("""(\d*\.\d\d?)""")
  var target = ""
  var targetStart = 0
  var targetEnd = 0

  private var numberTweakPopup: Popup = _
  var inSliderChange = false
  
  def isAbsent = numberTweakPopup == null
  def isPresent = !isAbsent
    
  def close() {
    if (numberTweakPopup != null) {
      numberTweakPopup.hide()
      numberTweakPopup = null
      ctx.removeManipulator(this)
    }
  }

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
    var ntarget = target0.toDouble
    val slider = new JSlider();
    val leftLabel = new JLabel
    val rightLabel = new JLabel
    def slider2double(n: Int) = {
      ntarget + (n-9) * delta
    }
    def double2slider(n: Double): Int = {
      9 + math.round((n - ntarget) / delta).toInt
    }
    def uiDouble(s: String) = Utils.stripTrailingChar(s, '0')
    
    def reConfigSlider(around: Double) {
      ntarget = around
      slider.setValue(9)
      slider.setMinimum(0)
      slider.setMaximum(18)
      slider.setMajorTickSpacing(1)
      leftLabel.setText(uiDouble(formatter format slider2double(slider.getMinimum)))
      rightLabel.setText(uiDouble(formatter format slider2double(slider.getMaximum)))
    }
    reConfigSlider(ntarget)
    slider.setPaintTicks(true)
      
    var lastrunval = ntarget
    slider.addChangeListener(new ChangeListener {
        def stateChanged(e: ChangeEvent) = Utils.safeProcess {
          val eslider = e.getSource.asInstanceOf[JSlider]
          val newnum = eslider.getValue()
          inSliderChange = true
          doc.remove(targetStart, target.length())
          target = uiDouble(formatter format slider2double(newnum))
          doc.insertString(targetStart, target, null);
          inSliderChange = false
            
          if (!eslider.getValueIsAdjusting) {
            // drag over
            if (ctx.isRunningEnabled) {
              if (lastrunval != newnum) {
                lastrunval = newnum
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
    
    val factory = PopupFactory.getSharedInstance();
    val rect = ctx.codePane.modelToView(offset)
    val pt = new Point(rect.x, rect.y)
    SwingUtilities.convertPointToScreen(pt, ctx.codePane)
    val panel = new JPanel()
    panel.setBorder(BorderFactory.createLineBorder(Color.gray, 1))
    val zoomB = new JToggleButton("\u20aa")
    zoomB.setToolTipText("Decrease Slider Stepsize")
    zoomB.addActionListener(new ActionListener {
        def actionPerformed(e: ActionEvent) {
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
      })
    panel.add(zoomB)
    panel.add(leftLabel)
    panel.add(slider)
    panel.add(rightLabel)
    panel.add(new JLabel(" " * 10))
    numberTweakPopup = factory.getPopup(ctx.codePane, panel, pt.x-50, pt.y + (rect.height * 2).toInt)
    numberTweakPopup.show()
  }
}
