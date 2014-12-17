package ch.epfl.data
package cscala
package test

import org.scalatest.{ FlatSpec, ShouldMatchers }
import collections._

class CArrayBufferTest extends FlatSpec with ShouldMatchers {
  System.loadLibrary("shallow")

  import CLangTypes._
  import CLang._
  import CString._
  import CStdLib._

  "CArrayBuffer" should "create ArrayBuffers for Ints" in {
    val ab = new CArrayBuffer[Int]()
    ab.append(1)
    ab.append(2)
    ab.append(3)
    ab.append(4)
    ab(0) should be(1)
    ab(1) should be(2)
    ab(2) should be(3)
    ab(3) should be(4)
  }

  it should "create ArrayBuffers for Pointer[Char]" in {
    val ab = new CArrayBuffer[Pointer[Char]]()
    val s1 = malloc[Char](5 * sizeof[Char])
    strcpy(s1, "hello")
    val s2 = malloc[Char](5 * sizeof[Char])
    strcpy(s2, "world")
    ab.append(s1)
    ab.append(s2)
    ab(0) should be(s1)
    ab(1) should be(s2)
  }

  it should "create ArrayBuffers for Strings" in {
    val ab = new CArrayBuffer[String]()
    ab.append("hello ")
    ab.append("world")
    ab(0) should be("hello ")
    ab(1) should be("world")
  }

  it should "support removal" in {
    val ab = new CArrayBuffer[Int]()
    ab.size should be(0)
    ab.append(1)
    ab.append(2)
    ab.append(3)
    ab.size should be(3)
    ab.remove(1)
    ab.size should be(2)
    ab.remove(0)
    ab.size should be(1)
    ab(0) should be(3)
  }

  it should "support minBy with Ints" in {
    val comp = (x: Int) => -x
    val ab = new CArrayBuffer[Int]()
    ab.append(3)
    ab.append(6)
    ab.append(2)
    ab.append(5)
    ab.append(1)
    ab.minBy(comp) should be(6)
  }

  it should "support minBy with Strings" in {
    val comp = (x: String) => strlen(x)
    val ab = new CArrayBuffer[String]()
    ab.append("longword")
    ab.append("short")
    ab.append("longagain")
    ab.minBy(comp) should be("short")
  }

  it should "support foldLeft with Ints" in {
    val ab = new CArrayBuffer[Int]()
    ab.append(5)
    ab.append(4)
    ab.append(4)
    ab.append(6)
    ab.foldLeft(0) { (x: Int, y: Int) => x + y } should be(19)
  }

  it should "support foldLeft with Strings" in {
    val ab = new CArrayBuffer[String]()
    ab.append("four")
    ab.append("abcdefg")
    ab.foldLeft(0) { (x: Int, y: String) => x + strlen(y) } should be(11)
  }
}
