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
package net.kogics.kojo.util

// Ideas borrowed from Scalaz
// Can't afford to bundle all of Scalaz within Kojo just yet (because of size constraints)
object Typeclasses extends Semigroups
                      with Identitys {
    def some[A](a: A): Option[A] = Some(a)
    def none[A] = None
}

trait Semigroup[S] {
    def append(s1: S, s2: => S): S
}

trait Semigroups {
    def semigroup[S](f: (S, => S) => S): Semigroup[S] = new Semigroup[S] {
        def append(s1: S, s2: => S) = f(s1, s2)
    }
}

object Semigroup {
    import Typeclasses._
    implicit def StringSemigroup: Semigroup[String] = semigroup(_ + _)
    implicit def IntSemigroup: Semigroup[Int] = semigroup(_ + _)
    implicit def ListSemigroup[A]: Semigroup[List[A]] = semigroup(_ ++ _)
    implicit def OptionSemigroup[A : Semigroup]: Semigroup[Option[A]] = semigroup((a, b) => { (a,b) match {
                case (Some(va), Some(vb)) => Some(va |+| vb)
                case (Some(va), None) => Some(va)
                case (None, Some(vb)) => Some(vb)
                case (None, None) => None
            }})
}


trait Identity[A]  {
    def value: A
    def |+|(a: => A)(implicit s: Semigroup[A]): A = s append (value, a)
}

trait Identitys {
    implicit def mkIdentity[A](x: => A): Identity[A] = Identity(x)
    implicit def unMkIdentity[A](x: Identity[A]): A = x.value

    val unital = mkIdentity(())
}

object Identity { 
    def apply[A](a: => A): Identity[A] = new Identity[A] {
        lazy val value = a
    }
    def unapply[A](v: Identity[A]): Option[A] = Some(v.value)
}
