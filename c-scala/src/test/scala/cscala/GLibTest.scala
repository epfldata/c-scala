package ch.epfl.data
package cscala
package test

import java.io.{ File, FileOutputStream, PrintStream }
import collection.JavaConverters._

import org.scalatest.{ FlatSpec, ShouldMatchers }

import CLang._
import CLangTypes._
import CStdLib._
import CStdIO._
import CString._
import CSysTime._
import GLibTypes._
import GListHeader._
import GHashTableHeader._
import GArrayHeader._
import GTreeHeader._

class GLibTest extends FlatSpec with ShouldMatchers {
  System.loadLibrary("shallow")

  "Shallow GHashTable" should "perform hash table insert and size" in {
    val hash = (key: gconstpointer) => *(key.asInstanceOf[Pointer[Int]])
    val equals = (key1: gconstpointer, key2: gconstpointer) => {
      if (*(key1.asInstanceOf[Pointer[Int]]) == *(key2.asInstanceOf[Pointer[Int]])) 1 else 0
    }

    val hashp = &(hash)
    val equalsp = &(equals)
    val hm = g_hash_table_new(hashp, equalsp)

    g_hash_table_insert(hm, &(1).asInstanceOf[gconstpointer], &(2).asInstanceOf[gconstpointer])
    g_hash_table_size(hm) should be(1)
    g_hash_table_insert(hm, &(1).asInstanceOf[gconstpointer], &(2).asInstanceOf[gconstpointer])
    g_hash_table_size(hm) should be(1)
    g_hash_table_insert(hm, &(2).asInstanceOf[gconstpointer], &(2).asInstanceOf[gconstpointer])
    g_hash_table_size(hm) should be(2)
  }

  it should "lookup and replace elements" in {
    val hash = (key: gconstpointer) => *(key.asInstanceOf[Pointer[Int]])
    val equals = (key1: gconstpointer, key2: gconstpointer) => {
      if (*(key1.asInstanceOf[Pointer[Int]]) == *(key2.asInstanceOf[Pointer[Int]])) 1 else 0
    }

    val hashp = &(hash)
    val equalsp = &(equals)
    val hm = g_hash_table_new(hashp, equalsp)

    g_hash_table_insert(hm, &(1).asInstanceOf[gconstpointer], &(5).asInstanceOf[gconstpointer])
    g_hash_table_insert(hm, &(2).asInstanceOf[gconstpointer], &(6).asInstanceOf[gconstpointer])
    g_hash_table_insert(hm, &(3).asInstanceOf[gconstpointer], &(7).asInstanceOf[gconstpointer])

    *(g_hash_table_lookup(hm, &(1).asInstanceOf[gconstpointer]).asInstanceOf[Pointer[Int]]) should be(5)
    *(g_hash_table_lookup(hm, &(2).asInstanceOf[gconstpointer]).asInstanceOf[Pointer[Int]]) should be(6)
    g_hash_table_lookup(hm, &(23).asInstanceOf[gconstpointer]) should be(NULL)

    g_hash_table_replace(hm, &(2).asInstanceOf[gconstpointer], &(8).asInstanceOf[gconstpointer])
    *(g_hash_table_lookup(hm, &(2).asInstanceOf[gconstpointer]).asInstanceOf[Pointer[Int]]) should be(8)
  }

  it should "sum the elements using foreach" in {
    val hash = (key: gconstpointer) => *(key.asInstanceOf[Pointer[Int]])
    val equals = (key1: gconstpointer, key2: gconstpointer) => {
      if (*(key1.asInstanceOf[Pointer[Int]]) == *(key2.asInstanceOf[Pointer[Int]])) 1 else 0
    }

    val hm = g_hash_table_new(&(hash), &(equals))

    g_hash_table_insert(hm, &(1).asInstanceOf[gconstpointer], &(5).asInstanceOf[gconstpointer])
    g_hash_table_insert(hm, &(2).asInstanceOf[gconstpointer], &(6).asInstanceOf[gconstpointer])
    g_hash_table_insert(hm, &(3).asInstanceOf[gconstpointer], &(7).asInstanceOf[gconstpointer])

    val sum = malloc[Int](sizeof[Int])
    pointer_assign(sum, 0)

    val add = (key: gconstpointer, value: gconstpointer, data: gconstpointer) => {
      pointer_assign(sum, *(sum.asInstanceOf[Pointer[Int]]) + *(value.asInstanceOf[Pointer[Int]]))
    }

    g_hash_table_foreach(hm, &(add), NULL);

    *(sum) should be(5 + 6 + 7)
  }

  it should "work with strings as keys" in {
    val hash = (key: gconstpointer) => strlen(*(key.asInstanceOf[Pointer[Pointer[Char]]]))
    val equals = (key1: gconstpointer, key2: gconstpointer) => {
      if (strcmp(*(key1.asInstanceOf[Pointer[Pointer[Char]]]), *(key2.asInstanceOf[Pointer[Pointer[Char]]])) == 0) 1 else 0
    }

    val hm = g_hash_table_new(&(hash), &(equals))

    val key1 = malloc[Char](6)
    val key2 = malloc[Char](6)
    val key3 = malloc[Char](6)
    strcpy(key1, "hello")
    strcpy(key2, "hello")
    strcpy(key3, "a")
    g_hash_table_insert(hm, &(key1).asInstanceOf[gconstpointer], &(1).asInstanceOf[gconstpointer])
    g_hash_table_insert(hm, &(key3).asInstanceOf[gconstpointer], &(2).asInstanceOf[gconstpointer])

    *(g_hash_table_lookup(hm, &(key3).asInstanceOf[gconstpointer]).asInstanceOf[Pointer[Int]]) should be(2)
    *(g_hash_table_lookup(hm, &(key2).asInstanceOf[gconstpointer]).asInstanceOf[Pointer[Int]]) should be(1)
  }

  "Shallow GList" should "perform list insertion and size" in {
    val list = g_list_append(NULL[GList[Int]], &(1))
    g_list_length(list) should be(1)

    val list1 = g_list_prepend(list, &(2))
    g_list_length(list1) should be(2)
    g_list_length(list) should be(1)

    g_list_insert(list1, &(3), 1)
    g_list_length(list1) should be(3)
  }

  it should "perform lookup and remove operations" in {
    var list = g_list_append(NULL[GList[Int]], &(1))
    g_list_append(list, &(2))
    g_list_append(list, &(3))
    g_list_append(list, &(4))

    *(g_list_nth_data(list, 1)) should be(2)
    *(g_list_nth_data(list, 2)) should be(3)
    *(g_list_nth_data(list, 3)) should be(4)

    list = g_list_remove_link(list, g_list_first(list))

    *(g_list_nth_data(list, 1)) should be(3)
    *(g_list_nth_data(list, 0)) should be(2)
  }

  "Shallow GArray" should "perform array operations" in {
    val array = g_array_new(0, 0, sizeof[Char])
    g_array_get_element_size(array) should be(sizeof[Char])

    val data = 'c'
    g_array_insert_vals(array, 0, &(data).asInstanceOf[Pointer[Any]], 1)

    g_array_index[Char](array, 0) should be('c')
  }

  "Shallow GTree" should "perform creation, insertion and lookup" in {
    val equals = (key1: Pointer[Any], key2: Pointer[Any]) => {
      val n1 = key1.asInstanceOf[Pointer[Int]]
      val n2 = key2.asInstanceOf[Pointer[Int]]
      if (*(n1) == *(n2)) 0 else 1
    }
    val tree = g_tree_new(&(equals))
    val k1 = &(12).asInstanceOf[gconstpointer]
    val k2 = &(16).asInstanceOf[gconstpointer]
    g_tree_insert(tree, k1, &(1).asInstanceOf[gpointer])
    g_tree_insert(tree, k2, &(2).asInstanceOf[gpointer])

    *(g_tree_lookup(tree, &(12).asInstanceOf[gconstpointer]).asInstanceOf[Pointer[Int]]) should be(1)
    *(g_tree_lookup(tree, &(16).asInstanceOf[gconstpointer]).asInstanceOf[Pointer[Int]]) should be(2)
  }
}
