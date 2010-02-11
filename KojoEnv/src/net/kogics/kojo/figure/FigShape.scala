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

package net.kogics.kojo
package figure

import edu.umd.cs.piccolo.PNode
import edu.umd.cs.piccolo.PCanvas

trait FigShape extends core.VisualElement {

  protected val piccoloNode: PNode
  protected val canvas: PCanvas

  def hide() {
    piccoloNode.setVisible(false)
    canvas.repaint()
  }

  def show() {
    piccoloNode.setVisible(true)
    canvas.repaint()
  }

  def setColor(color: java.awt.Color) {
    piccoloNode.setPaint(color)
  }
}
