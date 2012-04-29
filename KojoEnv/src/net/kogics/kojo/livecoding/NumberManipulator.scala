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

package net.kogics.kojo
package livecoding

import util.Utils
import java.awt.Color
import java.awt.Point
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSlider
import javax.swing.JTextField
import javax.swing.JToggleButton
import javax.swing.Popup
import javax.swing.PopupFactory
import javax.swing.SwingUtilities


abstract class NumberManipulator(ctx: ManipulationContext) extends InteractiveManipulator {
  var target = ""
  var targetStart = 0
  var targetEnd = 0

  var numberTweakPopup: Popup = _
  var stepT: JTextField = _
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

  def showPopup(offset: Int, 
                leftLabel: JLabel, 
                slider: JSlider, 
                rightLabel: JLabel, 
                zoomListener: JToggleButton => Unit,
                stepListener: Option[(JTextField, JToggleButton) => Unit]) {
    val factory = PopupFactory.getSharedInstance();
    val rect = ctx.codePane.modelToView(offset)
    val pt = new Point(rect.x, rect.y)
    SwingUtilities.convertPointToScreen(pt, ctx.codePane)
    implicit val klass = getClass
    val zoomB = new JToggleButton("\u20aa")
    val stepB = new JButton("\u2551")
    stepT = new JTextField(4)
    zoomB.setToolTipText(Utils.loadString("CTL_Decrease"))
    zoomB.addActionListener(new ActionListener {
        def actionPerformed(e: ActionEvent) {
          zoomListener(zoomB)
          if (zoomB.isSelected) {
            zoomB.setToolTipText(Utils.loadString("CTL_Increase"))
            stepB.setEnabled(false)
            stepT.setEnabled(false)
          }
          else {
            zoomB.setToolTipText(Utils.loadString("CTL_Decrease"))
            stepB.setEnabled(true)
            stepT.setEnabled(true)
          }
        }      
      })
    zoomListener(zoomB)
    
    val panel = new JPanel()
    panel.setBorder(BorderFactory.createLineBorder(Color.gray, 1))

    stepListener.foreach { stepL =>
      val stepP= new JPanel
      stepP.setBorder(BorderFactory.createLineBorder(Color.gray, 1))
      stepP.add(stepT)
      stepB.setToolTipText("Change Stepsize")
      stepP.add(stepB)
      stepB.addActionListener(new ActionListener {
          def actionPerformed(e: ActionEvent) {
            stepL(stepT, zoomB)
          }      
        })
      panel.add(stepP)
    }
    
    panel.add(zoomB)
    panel.add(leftLabel)
    panel.add(slider)
    panel.add(rightLabel)
    panel.add(new JLabel(" " * 10))
    numberTweakPopup = factory.getPopup(ctx.codePane, panel, pt.x-50, pt.y + (rect.height * 1.5).toInt)
    numberTweakPopup.show()
  }
}
