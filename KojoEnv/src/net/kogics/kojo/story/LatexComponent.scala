/*
 * Copyright (C) 2010 Lalit Pant <pant.lalit@gmail.com>
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

package net.kogics.kojo.story

import java.awt._
import javax.swing._
import org.scilab.forge.jlatexmath._

class LatexComponent(latex: String) extends JPanel {
  val formula = new TeXFormula(latex)
  val icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 18)
  icon.setInsets(new Insets(5, 5, 5, 5))
  val jl = new JLabel()
  setBorder(BorderFactory.createLineBorder(Color.black))

  override def getPreferredSize() = {
    new Dimension(icon.getTrueIconWidth.toInt, icon.getTrueIconHeight.toInt + 10)
  }

  override def paintComponent(g: Graphics) {
    super.paintComponent(g)
    icon.paintIcon(jl, g, 0, 0);
  }
}

