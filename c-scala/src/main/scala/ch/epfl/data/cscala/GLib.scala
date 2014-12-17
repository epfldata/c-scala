package ch.epfl.data
package cscala

/**
 * GLib types.
 *
 * Contains lots of type aliases and some GLib struct declarations. It should
 * always be imported when using GLib functions.
 */
object GLibTypes {
  import CLangTypes._

  class GArray(val bytes: Array[Byte]) extends CStruct
  implicit val GArrayInfo: CStructInfo[GArray] = new CStructInfo[GArray] {
    val sizes = List(
      'data -> CLang.sizeof[Pointer[gchar]],
      'len -> CLang.sizeof[guint])
    def create(bytes: Array[Byte]) = new GArray(bytes)
  }
  class GTree
  class GHashTable
  class GList[T](val bytes: Array[Byte]) extends CStruct
  implicit def GListInfo[T]: CStructInfo[GList[T]] = new CStructInfo[GList[T]] {
    val sizes = List(
      'data -> CLang.sizeof[gpointer],
      'next -> CLang.sizeof[Pointer[GList[T]]],
      'prev -> CLang.sizeof[Pointer[GList[T]]])
    def create(bytes: Array[Byte]) = new GList(bytes)
  }

  type gconstpointer = Pointer[Any]
  type gpointer = Pointer[Any]
  type gboolean = gint
  type gint = Int
  type guint = Int
  type gchar = Char
  type GCompareDataFunc = Pointer[(gconstpointer, gconstpointer, gpointer) => gint]
  type GCompareFunc2 = Pointer[(gconstpointer, gconstpointer) => gint]
  type GDestroyNotify = Pointer[(gpointer) => Unit]
  type GHPointer = Pointer[GHashTable]
  type GHashFunc = Pointer[gconstpointer => guint]
  type GEqualFunc = Pointer[(gconstpointer, gconstpointer) => gboolean]
  type GHFunc = Pointer[(gpointer, gpointer, gpointer) => Unit]
  type GHRFunc = Pointer[(gpointer, gpointer, gpointer) => gboolean]
  type GPointer[T] = Pointer[GList[T]]
  type GDestroy[T] = Pointer[Pointer[T] => Unit]
  type GFunc[T] = Pointer[(Pointer[T], Any) => Unit]
  type GCompareFunc[T] = Pointer[(Pointer[T], Pointer[T]) => Int]
}

/** The GList functions */
object GListHeader {
  import CLang._
  import CLangTypes._
  import GLibTypes._

  // Doubly linked lists functions
  def g_list_append[T](list: GPointer[T], data: Pointer[T]): GPointer[T] = new Pointer(Native.g_list_append(list.addr, data.addr))
  def g_list_prepend[T](list: GPointer[T], data: Pointer[T]): GPointer[T] = new Pointer(Native.g_list_prepend(list.addr, data.addr))
  def g_list_insert[T](list: GPointer[T], data: Pointer[T], position: Int): GPointer[T] = new Pointer(Native.g_list_insert(list.addr, data.addr, position))
  def g_list_insert_before[T](list: GPointer[T], sibling: GPointer[T], data: Pointer[T]): GPointer[T] = new Pointer(Native.g_list_insert_before(list.addr, sibling.addr, data.addr))
  def g_list_remove[T](list: GPointer[T], data: Pointer[T]): GPointer[T] = new Pointer(Native.g_list_remove(list.addr, data.addr))
  def g_list_remove_link[T](list: GPointer[T], llink: GPointer[T]): GPointer[T] = new Pointer(Native.g_list_remove_link(list.addr, llink.addr))
  def g_list_delete_link[T](list: GPointer[T], llink: GPointer[T]): GPointer[T] = new Pointer(Native.g_list_delete_link(list.addr, llink.addr))
  def g_list_remove_all[T](list: GPointer[T], data: Pointer[T]): GPointer[T] = new Pointer(Native.g_list_remove_all(list.addr, data.addr))
  def g_list_free[T](list: GPointer[T]): Unit = Native.g_list_free(list.addr)
  def g_list_free_full[T](list: GPointer[T], freeFunc: GDestroy[T]): Unit = Native.g_list_free_full(list.addr, freeFunc.addr)
  def g_list_alloc[T](): GPointer[T] = new Pointer(Native.g_list_alloc())
  def g_list_length[T](list: GPointer[T]): Int = Native.g_list_length(list.addr)
  def g_list_concat[T](list1: GPointer[T], list2: GPointer[T]): GPointer[T] = new Pointer(Native.g_list_concat(list1.addr, list2.addr))
  def g_list_foreach[T](list: GPointer[T], func: GFunc[T], userData: Any): Unit = Native.g_list_foreach(list.addr, func.addr, userData)
  def g_list_first[T](list: GPointer[T]): GPointer[T] = new Pointer(Native.g_list_first(list.addr))
  def g_list_last[T](list: GPointer[T]): GPointer[T] = new Pointer(Native.g_list_last(list.addr))
  def g_list_nth[T](list: GPointer[T], n: Int): GPointer[T] = new Pointer(Native.g_list_nth(list.addr, n))
  def g_list_nth_data[T](list: GPointer[T], n: Int): Pointer[T] = new Pointer(Native.g_list_nth_data(list.addr, n))
  def g_list_nth_prev[T](list: GPointer[T], n: Int): GPointer[T] = new Pointer(Native.g_list_nth_prev(list.addr, n))
  def g_list_find[T](list: GPointer[T], data: Pointer[T]): GPointer[T] = new Pointer(Native.g_list_find(list.addr, data.addr))
  def g_list_find_custom[T](list: GPointer[T], data: Pointer[T], func: GCompareFunc[T]): GPointer[T] = new Pointer(Native.g_list_find_custom(list.addr, data.addr, func.addr))
  def g_list_position[T](list: GPointer[T], llink: GPointer[T]): Int = Native.g_list_position(list.addr, llink.addr)
  def g_list_index[T](list: GPointer[T], data: Pointer[T]): Int = Native.g_list_index(list.addr, data.addr)

  // Macros
  def g_list_next[T](list: GPointer[T]): GPointer[T] = new Pointer[GList[T]](Native.g_list_next(list.addr))
}

/** The GHashTable functions */
object GHashTableHeader {
  import CLang._
  import CLangTypes._
  import GLibTypes._

  // Hash table functions
  def g_hash_table_new(hash: GHashFunc, equals: GEqualFunc): GHPointer = new Pointer(Native.g_hash_table_new(hash.addr, equals.addr))
  def g_hash_table_insert(ht: GHPointer, key: gpointer, value: gpointer): Unit = Native.g_hash_table_insert(ht.addr, key.addr, value.addr)
  def g_hash_table_replace(ht: GHPointer, key: gpointer, value: gpointer): Unit = Native.g_hash_table_replace(ht.addr, key.addr, value.addr)
  def g_hash_table_size(ht: GHPointer): guint = Native.g_hash_table_size(ht.addr)
  def g_hash_table_lookup(ht: GHPointer, key: gconstpointer): gpointer = new Pointer(Native.g_hash_table_lookup(ht.addr, key.addr))
  def g_hash_table_lookup_extended(ht: GHPointer, key: gconstpointer, origKey: gpointer, value: gpointer): gboolean = Native.g_hash_table_lookup_extended(ht.addr, key.addr, origKey.addr, value.addr)
  def g_hash_table_foreach(ht: GHPointer, func: GHFunc, userData: Any): Unit = Native.g_hash_table_foreach(ht.addr, func.addr, userData)
  def g_hash_table_find(ht: GHPointer, pred: GHRFunc, userData: Any): gpointer = new Pointer(Native.g_hash_table_find(ht.addr, pred.addr, userData))
  def g_hash_table_remove(ht: GHPointer, key: gconstpointer): Int = Native.g_hash_table_remove(ht.addr, key.addr)
  def g_hash_table_remove_all(ht: GHPointer): Unit = Native.g_hash_table_remove_all(ht.addr)
  def g_hash_table_foreach_remove(ht: GHPointer, pred: GHRFunc, userData: Any): Int = Native.g_hash_table_foreach_remove(ht.addr, pred.addr, userData)
  def g_hash_table_get_keys(ht: GHPointer): Pointer[GList[Any]] = new Pointer(Native.g_hash_table_get_keys(ht.addr))
  def g_hash_table_get_values(ht: GHPointer): Pointer[GList[Any]] = new Pointer(Native.g_hash_table_get_values(ht.addr))
}

/** The GArray functions */
object GArrayHeader {
  import CLang._
  import CLangTypes._
  import GLibTypes._

  // GArray functions (generated by ASTParser)
  def g_array_new(zero_terminated: gboolean, clear: gboolean, element_size: guint): Pointer[GArray] = new Pointer(Native.g_array_new(zero_terminated, clear, element_size))
  def g_array_sized_new(zero_terminated: gboolean, clear: gboolean, element_size: guint, reserved_size: guint): Pointer[GArray] = new Pointer(Native.g_array_sized_new(zero_terminated, clear, element_size, reserved_size))
  def g_array_free(array: Pointer[GArray], free_segment: gboolean): Pointer[gchar] = new Pointer(Native.g_array_free(array.addr, free_segment))
  def g_array_ref(array: Pointer[GArray]): Pointer[GArray] = new Pointer(Native.g_array_ref(array.addr))
  def g_array_unref(array: Pointer[GArray]): Unit = Native.g_array_unref(array.addr)
  def g_array_get_element_size(array: Pointer[GArray]): guint = Native.g_array_get_element_size(array.addr)
  def g_array_append_vals(array: Pointer[GArray], data: gconstpointer, len: guint): Pointer[GArray] = new Pointer(Native.g_array_append_vals(array.addr, data.addr, len))
  def g_array_prepend_vals(array: Pointer[GArray], data: gconstpointer, len: guint): Pointer[GArray] = new Pointer(Native.g_array_prepend_vals(array.addr, data.addr, len))
  def g_array_insert_vals(array: Pointer[GArray], index: guint, data: gconstpointer, len: guint): Pointer[GArray] = new Pointer(Native.g_array_insert_vals(array.addr, index, data.addr, len))
  def g_array_set_size(array: Pointer[GArray], length: guint): Pointer[GArray] = new Pointer(Native.g_array_set_size(array.addr, length))
  def g_array_remove_index(array: Pointer[GArray], index: guint): Pointer[GArray] = new Pointer(Native.g_array_remove_index(array.addr, index))
  def g_array_remove_index_fast(array: Pointer[GArray], index: guint): Pointer[GArray] = new Pointer(Native.g_array_remove_index_fast(array.addr, index))
  def g_array_remove_range(array: Pointer[GArray], index: guint, length: guint): Pointer[GArray] = new Pointer(Native.g_array_remove_range(array.addr, index, length))
  def g_array_sort(array: Pointer[GArray], compare_func: GCompareFunc2): Unit = Native.g_array_sort(array.addr, compare_func.addr)
  def g_array_sort_with_data(array: Pointer[GArray], compare_func: GCompareDataFunc, user_data: gpointer): Unit = Native.g_array_sort_with_data(array.addr, compare_func.addr, user_data.addr)
  def g_array_set_clear_func(array: Pointer[GArray], clear_func: GDestroyNotify): Unit = Native.g_array_set_clear_func(array.addr, clear_func.addr)

  // g_array_index is defined as follows:
  // #define g_array_index(a,t,i)      (((t*) (void *) (a)->data) [(i)])
  def g_array_index[T: CType](array: Pointer[GArray], i: Int) = {
    val data = ->[GArray, Pointer[T]](array, 'data).asInstanceOf[Pointer[T]]
    *(pointer_add(data, i))
  }
}

/** The GTree functions */
object GTreeHeader {
  import CLang._
  import CLangTypes._
  import GLibTypes._

  // GTree functions (generated by ASTParser)
  object GTraverseType {
    type GTraverseType = Int
    val G_IN_ORDER = 0
    val G_PRE_ORDER = 1
    val G_POST_ORDER = 2
    val G_LEVEL_ORDER = 3
  }
  import GTraverseType._
  type GTraverseFunc = Pointer[(gpointer, gpointer, gpointer) => gboolean]
  def g_tree_new(key_compare_func: GCompareFunc2): Pointer[GTree] = new Pointer(Native.g_tree_new(key_compare_func.addr))
  def g_tree_new_with_data(key_compare_func: GCompareDataFunc, key_compare_data: gpointer): Pointer[GTree] = new Pointer(Native.g_tree_new_with_data(key_compare_func.addr, key_compare_data.addr))
  def g_tree_new_full(key_compare_func: GCompareDataFunc, key_compare_data: gpointer, key_destroy_func: GDestroyNotify, value_destroy_func: GDestroyNotify): Pointer[GTree] = new Pointer(Native.g_tree_new_full(key_compare_func.addr, key_compare_data.addr, key_destroy_func.addr, value_destroy_func.addr))
  def g_tree_ref(tree: Pointer[GTree]): Pointer[GTree] = new Pointer(Native.g_tree_ref(tree.addr))
  def g_tree_unref(tree: Pointer[GTree]): Unit = Native.g_tree_unref(tree.addr)
  def g_tree_destroy(tree: Pointer[GTree]): Unit = Native.g_tree_destroy(tree.addr)
  def g_tree_insert(tree: Pointer[GTree], key: gpointer, value: gpointer): Unit = Native.g_tree_insert(tree.addr, key.addr, value.addr)
  def g_tree_replace(tree: Pointer[GTree], key: gpointer, value: gpointer): Unit = Native.g_tree_replace(tree.addr, key.addr, value.addr)
  def g_tree_remove(tree: Pointer[GTree], key: gconstpointer): gboolean = Native.g_tree_remove(tree.addr, key.addr)
  def g_tree_steal(tree: Pointer[GTree], key: gconstpointer): gboolean = Native.g_tree_steal(tree.addr, key.addr)
  def g_tree_lookup(tree: Pointer[GTree], key: gconstpointer): gpointer = new Pointer(Native.g_tree_lookup(tree.addr, key.addr))
  def g_tree_lookup_extended(tree: Pointer[GTree], lookup_key: gconstpointer, orig_key: Pointer[gpointer], value: Pointer[gpointer]): gboolean = Native.g_tree_lookup_extended(tree.addr, lookup_key.addr, orig_key.addr, value.addr)
  def g_tree_foreach(tree: Pointer[GTree], func: GTraverseFunc, user_data: gpointer): Unit = Native.g_tree_foreach(tree.addr, func.addr, user_data.addr)
  def g_tree_traverse(tree: Pointer[GTree], traverse_func: GTraverseFunc, traverse_type: GTraverseType, user_data: gpointer): Unit = Native.g_tree_traverse(tree.addr, traverse_func.addr, traverse_type, user_data.addr)
  def g_tree_search(tree: Pointer[GTree], search_func: GCompareFunc2, user_data: gconstpointer): gpointer = new Pointer(Native.g_tree_search(tree.addr, search_func.addr, user_data.addr))
  def g_tree_height(tree: Pointer[GTree]): gint = Native.g_tree_height(tree.addr)
  def g_tree_nnodes(tree: Pointer[GTree]): gint = Native.g_tree_nnodes(tree.addr)
}
