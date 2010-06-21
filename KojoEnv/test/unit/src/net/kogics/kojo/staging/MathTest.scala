/*
 * Copyright (C) 2009 Lalit Pant <pant.lalit@gmail.com>
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
package staging

import org.junit.Test
import org.junit.Assert._

import net.kogics.kojo.util._

class MathTest extends StagingTestBase {

  @Test
  // lalit sez: if we have more than five tests, we run out of heap space - maybe
  // a leak in the Scala interpreter/compiler subsystem. So we run (mostly)
  // everything in one test
  def test1 = {
  //W
  //W==Math==
  //W
  //W{{{
  //Wconstrain(value, min, max)
    Tester("Staging.constrain(-22, -13, 78)", Some("Double = -13.0"))
    Tester("Staging.constrain(-2, -13, 78)", Some("Double = -2.0"))
    Tester("Staging.constrain(22, -13, 78)", Some("Double = 22.0"))
    Tester("Staging.constrain(82, -13, 78)", Some("Double = 78.0"))
  //W}}}
  //W
  //WConstrains _value_ to be no less than _min_ and no greater than _max_.
  //W
  //W{{{
  //Wnorm(value, low, high)
    Tester("Staging.norm(-22, -13, 78).toFloat", Some("Float = -0.0989011"))
    Tester("Staging.norm(-2, -13, 78).toFloat", Some("Float = 0.12087912"))
    Tester("Staging.norm(22, -13, 78).toFloat", Some("Float = 0.3846154"))
    Tester("Staging.norm(82, -13, 78).toFloat", Some("Float = 1.043956"))
  //W}}}
  //W
  //WNormalizes _value_ to the range 0.0 -- 1.0, such that _value_ == _low_
  //Wyields 0.0 and _value_ == _high_ yields 1.0, and values in between yield
  //Wthe corresponding fraction.  Values outside the range yield results < 0.0 or
  //W> 1.0, scaled to the _low_ / _high_ range.
  //W
  //W{{{
  //Wmap(value, min1, max1, min2, max2)
    Tester("Staging.map(-22, -13, 78, 5, 20).toFloat", Some("Float = 3.5164835"))
    Tester("Staging.map(-2, -13, 78, 5, 20).toFloat", Some("Float = 6.8131866"))
    Tester("Staging.map(22, -13, 78, 5, 20).toFloat", Some("Float = 10.769231"))
    Tester("Staging.map(82, -13, 78, 5, 20).toFloat", Some("Float = 20.65934"))
  //W}}}
  //W
  //WMaps _value_ from the range _low1_ -- _high1_ to the range
  //W_low2_ -- _high2_.
  //W
  //W{{{
  //Wsq(value)
    Tester("Staging.sq(-8)", Some("Double = 64.0"))
  //W}}}
  //W
  //WYields _value_ squared.
  //W
  //W{{{
  //Wdist(x0, y0, x1, y1)
  //Wdist(p1, p2)
  //W}}}
  //W
  //WYields the distance between two points.
  //W
  //W{{{
  //Wlerp(low, high, value)
    Tester("Staging.lerp(-22, -13, 0.0).toFloat", Some("Float = -22.0"))
    Tester("Staging.lerp(-22, -13, 0.3).toFloat", Some("Float = -19.3"))
    Tester("Staging.lerp(-22, -13, 0.5).toFloat", Some("Float = -17.5"))
    Tester("Staging.lerp(-22, -13, 1.0).toFloat", Some("Float = -13.0"))
  //W}}}
  //W
  //WThe inverse of `norm`; yields a number in the range _low_ -- _high_
  //Wwhich corresponds to the position of _value_ in the range 0.0 -- 1.0.
  //W
  }
}

