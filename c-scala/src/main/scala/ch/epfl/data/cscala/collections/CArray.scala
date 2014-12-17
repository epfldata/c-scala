package ch.epfl.data
package cscala
package collections

import scala.collection.mutable.LinearSeq
import scala.collection.mutable.Builder
import scala.collection.generic.GenericTraversableTemplate
import scala.collection.generic.GenericCompanion

import CLang._
import CLangTypes._
import CStdLib._
import CCollectionsTypes._

class CArray[T: CType](val struct: Pointer[CArrayStruct[T]]) {
  def this(_length: Int) = this(&(new CArrayStruct(malloc[T](_length * sizeof[T]), _length)))

  def update(i: Int, x: T): Unit = {
    val array = ->[CArrayStruct[T], Pointer[T]](struct, 'array)
    val pos = pointer_add(array, i * sizeof[T])
    pointer_assign(pos, x)
  }

  def apply(i: Int): T = {
    val array = ->[CArrayStruct[T], Pointer[T]](struct, 'array)
    val pos = pointer_add(array, i * sizeof[T])
    *(pos)
  }

  def length: Int = ->[CArrayStruct[T], Int](struct, 'length)
}
