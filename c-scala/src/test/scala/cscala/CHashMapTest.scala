package ch.epfl.data
package cscala
package test

import org.scalatest.{ FlatSpec, ShouldMatchers }
import collections._

class CHashMapTest extends FlatSpec with ShouldMatchers {
  System.loadLibrary("shallow")

  import CLangTypes._

  "CHashMap" should "create a HashMap on Ints" in {
    val hm = new CHashMap[Int, Int]()
    hm(1) = 2
    hm(2) = 3
    hm(1) should be(2)
    hm(2) should be(3)
  }

  //it should "create a HashMap on Strings" in {
  //  val hm = new CHashMap[String, Int]()
  //  hm("key1") = 2
  //  hm("key2") = 3
  //  hm("key1") should be(2)
  //  hm("key2") should be(3)
  //}

  it should "create a HashMap on Doubles" in {
    val hm = new CHashMap[Double, Int]()
    hm(1.0) = 2
    hm(2.0) = 3
    hm(1.0) should be(2)
    hm(2.0) should be(3)
  }

  //it should "create a HashMap from Ints to CArrayBuffers" in {
  //  val hm = new CHashMap[Int, CArrayBuffer[Int]]()
  //  val ab1 = new CArrayBuffer[Int]()
  //  ab1.append(1)
  //  val ab2 = new CArrayBuffer[Int]()
  //  ab2.append(2)

  //  hm(1) = ab1
  //  hm(5) = ab2
  //  hm(1)(0) should be(1)
  //  hm(5)(0) should be(2)
  //}

  it should "add, remove and get items in the HashMap" in {
    val hm = new CHashMap[Int, Int]()
    hm(1) = 2
    hm(2) = 3
    hm.size should be(2)
    hm.contains(2) should be(true)
    //TODO: put this back
    //hm.remove(2)
    //hm.contains(2) should be(false)
    //hm.size should be(1)
  }

  it should "support getOrElseUpdate" in {
    val hm = new CHashMap[Int, Int]()
    hm(1) = 2
    hm(2) = 3
    hm.getOrElseUpdate(1, 5) should be(2)
    hm.getOrElseUpdate(3, 5) should be(5)
    hm.size should be(3)
  }

  it should "support clear" in {
    val hm = new CHashMap[Int, Int]()
    hm(1) = 2
    hm(2) = 3
    hm.size should be(2)
    hm.clear
    hm.size should be(0)
  }
}
