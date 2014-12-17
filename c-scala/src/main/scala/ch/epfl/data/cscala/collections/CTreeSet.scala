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
import GTreeHeader._
import GLibTypes._

class CTreeSet[T: CType](var gTree: Pointer[GTree])(implicit ordering: Ordering[T]) {
  def this()(implicit ordering: Ordering[T]) = this(g_tree_new(&((p1: gconstpointer, p2: gconstpointer) => {
    ordering.compare(*(p1.asInstanceOf[Pointer[T]]), *(p2.asInstanceOf[Pointer[T]]))
  })))

  def +=(elem: T) = {
    val ep = &(elem).asInstanceOf[gconstpointer]
    g_tree_insert(gTree, ep, ep)
    this
  }

  def -=(elem: T) = {
    val ep = &(elem).asInstanceOf[gconstpointer]
    g_tree_remove(gTree, ep)
    this
  }

  def size: Int = g_tree_nnodes(gTree)

  def head: T = {
    val init: Pointer[T] = malloc[T](sizeof[T])

    val headFunc = (key: gpointer, value: gpointer, data: gpointer) => {
      pointer_assign(data.asInstanceOf[Pointer[T]], *(value.asInstanceOf[Pointer[T]]))
      0
    }
    g_tree_foreach(gTree, &(headFunc), init.asInstanceOf[gpointer])
    *(init.asInstanceOf[Pointer[T]])
  }
}
