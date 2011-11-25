package net.kogics.kojo
import java.awt.Color
import net.kogics.kojo.core.Turtle

package object picture {
  type Painter = Turtle => Unit
  def rot(angle: Double) = Rotc(angle)  
  def scale(factor: Double) = Scalec(factor)
  def trans(x: Double, y: Double) = Transc(x, y)
  def fill(color: Color) = Fillc(color)
  def stroke(color: Color) = Strokec(color)
  def deco(painter: Painter) = Decoc(painter)
}
