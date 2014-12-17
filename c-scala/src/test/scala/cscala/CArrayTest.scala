package ch.epfl.data
package cscala

import org.scalatest.{ FlatSpec, ShouldMatchers }
import collections._

class CArrayTest extends FlatSpec with ShouldMatchers {
  System.loadLibrary("shallow")

  "CArray" should "create Arrays of Ints" in {
    val a = new CArray[Int](10)
    a.length should be(10)
    a.update(5, 5)
    a.update(1, 6)
    a(1) should be(6)
    a(5) should be(5)
  }

  it should "create Arrays of Strings" in {
    val a = new CArray[String](10)
    a.length should be(10)
    a.update(5, "abc")
    a.update(1, "def")
    a(1) should be("def")
    a(5) should be("abc")
  }
}
