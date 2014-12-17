package ch.epfl.data
package cscala
package collections

import scala.collection.mutable.HashMap
import scala.collection.mutable.LinearSeq
import scala.collection.mutable.Builder
import scala.collection.generic.GenericTraversableTemplate
import scala.collection.generic.GenericCompanion

import CLang._
import CLangTypes._
import GHashTableHeader._
import GLibTypes._

// This is part of a HashMap implemented using GHashTable. Keys are used
// directly, without boxing so it will only work with key types which fit in a
// pointer.
object CHashMap {
  def apply[A: CKeyType, B: CType]() = new CHashMap[A, B]()
}

class CHashMap[A: CKeyType, B: CType] {
  // Aey can be any of:
  // String, Int, Double, Record
  // Different hash and equal functions depending on which it is
  val gHashTable = {
    val hash = (k: gconstpointer) => as[A](k).hashCode
    val equal = (k1: gconstpointer, k2: gconstpointer) =>
      if (as[A](k1) == as[A](k2)) 1 else 0
    g_hash_table_new(&(hash), &(equal))
  }

  def update(k: A, v: B) = {
    g_hash_table_insert(gHashTable, as[gconstpointer](k), as[gconstpointer](v))
  }

  def apply(k: A): B = {
    as[B](g_hash_table_lookup(gHashTable, as[gconstpointer](k)))
  }

  def size: Int = g_hash_table_size(gHashTable)

  def clear(): Unit = {
    g_hash_table_remove_all(gHashTable)
  }

  //def keySet: CSet[A] = {
  //  new CSet[A](g_hash_table_get_keys(gHashTable).asInstanceOf[Pointer[LGList[A]]])
  //}

  def contains(k: A): Boolean = {
    g_hash_table_lookup(gHashTable, as[gconstpointer](k)) != NULL[Any]
  }

  def remove(k: A) = {
    val v = apply(k)
    g_hash_table_remove(gHashTable, as[gconstpointer](k))
    Option(v)
  }

  def getOrElseUpdate(k: A, v: => B): B = {
    val res = g_hash_table_lookup(gHashTable, as[gconstpointer](k))
    if (res != NULL[B]) as[B](res)
    else {
      update(k, v)
      v
    }
  }
}
