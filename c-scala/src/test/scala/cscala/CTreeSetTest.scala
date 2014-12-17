package ch.epfl.data
package cscala
package test

import org.scalatest.{ FlatSpec, ShouldMatchers }
import collections._

class CTreeSetTest extends FlatSpec with ShouldMatchers {
  System.loadLibrary("shallow")

  import CLangTypes._

  "CTreeSet" should "create a TreeSet of Ints" in {
    val ts = new CTreeSet[Int]
    ts += 3
    ts += 1
    ts += 5

    ts.size should be(3)
    ts.head should be(1)
  }

  it should "create a TreeSet of Strings" in {
    val ts = new CTreeSet[String]
    ts += "aef"
    ts += "abc"
    ts += "fre"

    ts.size should be(3)
    ts.head should be("abc")
  }

  it should "support removal" in {
    val ts = new CTreeSet[Int]
    ts += 3
    ts += 1
    ts += 5

    ts -= 1
    ts.head should be(3)

    ts -= 5
    ts.head should be(3)
    ts.size should be(1)
  }

  it should "support different orderings" in {
    val ordering = implicitly[Ordering[Int]].reverse
    val ts = new CTreeSet[Int]()(implicitly[CType[Int]], ordering)

    ts += 3
    ts += 1
    ts += 5

    ts.head should be(5)
  }
}
