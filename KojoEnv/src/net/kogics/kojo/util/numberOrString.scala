package net.kogics.kojo.util

trait NumberOrString[T] {
  def value(s: String): T
  def typeName: String
}

object NoS {
  implicit object StringNoS extends NumberOrString[String] {
    def value(s: String) = s
    def typeName = "String"
  }

  implicit object DoubleNoS extends NumberOrString[Double] {
    def value(n: String) = n.toDouble
    def typeName = "Double"
  }

  implicit object IntNoS extends NumberOrString[Int] {
    def value(n: String) = n.toInt
    def typeName = "Int"
  }
}
