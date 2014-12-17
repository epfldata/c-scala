package ch.epfl.data
package cscala
package collections

import scala.collection.mutable.LinearSeq
import scala.collection.mutable.Builder
import scala.collection.generic.GenericTraversableTemplate
import scala.collection.generic.GenericCompanion

import CLang._
import CLangTypes._
import GArrayHeader._
import GLibTypes._

object CArrayBuffer {
  def apply[T: CType]() = new CArrayBuffer[T]()
}

class CArrayBuffer[T: CType](val gArray: Pointer[GArray]) {
  def this() = this(g_array_new(0, 1, sizeof[T]))

  def apply(i: Int): T = {
    g_array_index[T](gArray, i)
  }
  def append(x: T): CArrayBuffer[T] = {
    g_array_append_vals(gArray, &(x).asInstanceOf[gconstpointer], 1)
    this
  }
  def foldLeft[U](x: U)(f: (U, T) => U): U = {
    var i = 0
    val len = size
    var state = x
    while (i < len) {
      val e = apply(i)
      state = f(state, e)
      i += 1
    }
    state
  }
  def size: Int = ->[GArray, Int](gArray, 'len)
  def minBy[U](f: T => U): T = {
    val len = size
    var i = 1
    var min = apply(0)
    var minBy = f(min).asInstanceOf[Int]
    while (i < len) {
      val e = apply(i)
      val eminBy = f(e).asInstanceOf[Int]
      if (eminBy < minBy) {
        min = e
        minBy = eminBy
      }
      i += 1
    }
    min
  }
  def remove(i: Int): CArrayBuffer[T] = {
    g_array_remove_index(gArray, i)
    this
  }

  def clear(): Unit = ???
}
