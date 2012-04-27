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
import java.awt.Rectangle
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JEditorPane
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

trait ManipulationContext {
  def isRunningEnabled: Boolean
  def runCode(code: String): Unit
  def codePane: JEditorPane
  def addManipulator(im: InteractiveManipulator)
  def removeManipulator(im: InteractiveManipulator)
}

trait InteractiveManipulator {
  def isAbsent: Boolean
  def isPresent: Boolean
  def close(): Unit
  def inSliderChange: Boolean
  def activate(doc: Document, offset: Int, target0: String, targetStart: Int)
}

class IntManipulator(ctx: ManipulationContext) extends InteractiveManipulator {
  private var numberTweakPopup: Popup = _
  var inSliderChange = false
  
  ctx.addManipulator(this)
    
  def isAbsent = numberTweakPopup == null
  def isPresent = !isAbsent
    
  def close() {
    if (numberTweakPopup != null) {
      numberTweakPopup.hide()
      numberTweakPopup = null
      ctx.removeManipulator(this)
    }
  }
    
  def activate(doc: Document, offset: Int, target0: String, targetStart: Int) = Utils.safeProcess {
    close()
    var target = target0
    val ntarget = target.toInt
    val slider = new JSlider();
    val leftLabel = new JLabel
    val rightLabel = new JLabel
    def reConfigSlider(around: Int) {
      slider.setMinimum(0)
      slider.setMaximum(around * 2)
      slider.setMajorTickSpacing(math.max(math.floor(around * 2.0 / 10).toInt, 1))
      leftLabel.setText(slider.getMinimum.toString)
      rightLabel.setText(slider.getMaximum.toString)
    }
    reConfigSlider(ntarget)
    slider.setValue(ntarget)
    slider.setPaintTicks(true)
      
    var lastrunval = ntarget
    slider.addChangeListener(new ChangeListener {
        def stateChanged(e: ChangeEvent) = Utils.safeProcess {
          val eslider = e.getSource.asInstanceOf[JSlider]
          val newnum = eslider.getValue()
          inSliderChange = true
          doc.remove(targetStart, target.length())
          target = newnum.toString
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
              eslider.setValue(lastrunval)
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
    zoomB.setToolTipText("Focus slider around its current value")
    zoomB.addActionListener(new ActionListener {
        def actionPerformed(e: ActionEvent) {
          if (zoomB.isSelected) {
            val sval = slider.getValue
            slider.setMinimum(math.max(sval - 9, 0))
            slider.setMaximum(sval + 9)
            slider.setMajorTickSpacing(1)
            leftLabel.setText(slider.getMinimum.toString)
            rightLabel.setText(slider.getMaximum.toString)
          }
          else {
            reConfigSlider(slider.getValue)
          }
        }
      })
    panel.add(zoomB)
    panel.add(leftLabel)
    panel.add(slider)
    panel.add(rightLabel)
    panel.add(new JLabel(" " * 10))
    numberTweakPopup = factory.getPopup(ctx.codePane, panel, pt.x-50, pt.y - (rect.height * 3).toInt)
    numberTweakPopup.show()
  }
}
