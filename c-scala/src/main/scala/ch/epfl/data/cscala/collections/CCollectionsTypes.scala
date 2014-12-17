package ch.epfl.data
package cscala
package collections

import java.nio.ByteBuffer
import java.nio.ByteOrder

object CCollectionsTypes {
  import CLangTypes._
  import CLang._
  import GLibTypes._

  class CArrayStruct[T: CType](val bytes: Array[Byte]) extends CStruct {
    def this(array: Pointer[T], length: Int) =
      this(ByteBuffer.allocate(sizeof[Pointer[T]] + sizeof[Int]).order(ByteOrder.LITTLE_ENDIAN).putLong(array.addr).putInt(length).array)
  }

  implicit def CArrayInfo[T: CType]: CStructInfo[CArrayStruct[T]] = new CStructInfo[CArrayStruct[T]] {
    val sizes = List(
      'array -> sizeof[Pointer[T]],
      'length -> sizeof[Int])
    def create(bytes: Array[Byte]) = new CArrayStruct[T](bytes)
  }

  implicit def cArrayType[T: CType] = new CType[CArray[T]] {
    def addr(v: CArray[T]): Pointer[CArray[T]] = {
      v.struct.asInstanceOf[Pointer[CArray[T]]]
    }
    def deref(v: Pointer[CArray[T]]): CArray[T] = {
      new CArray(v.asInstanceOf[Pointer[CArrayStruct[T]]])
    }
    def assign(p: Pointer[CArray[T]], v: CArray[T]) = {
      val cArrayStruct = p.asInstanceOf[Pointer[CArrayStruct[T]]]
      pointer_assign(cArrayStruct, *(v.struct))
    }
    def size = sizeof[Pointer[CArrayStruct[T]]]
  }
}
