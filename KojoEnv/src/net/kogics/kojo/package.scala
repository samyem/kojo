package net.kogics

package object kojo {
  sealed trait CodingMode
  case object TwMode extends CodingMode
  case object MwMode extends CodingMode
  case object StagingMode extends CodingMode
}