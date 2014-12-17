package ch.epfl.data
package cscala

import scala.native

object CLangNative {
  type Pointer[T] = Long
  type CTimeVal = CSysTime.CTimeVal

  @native def deref_long(v: Pointer[Long]): Long = ???
  @native def deref_double(v: Pointer[Double]): Double = ???
  @native def deref_int(v: Pointer[Int]): Int = ???
  @native def deref_char(v: Pointer[Char]): Char = ???
  @native def deref_bytes(v: Pointer[Array[Byte]], n: Int): Array[Byte] = ???
  @native def addr_long(v: Long): Pointer[Long] = ???
  @native def addr_double(v: Double): Pointer[Double] = ???
  @native def addr_int(v: Int): Pointer[Int] = ???
  @native def addr_char(v: Char): Pointer[Char] = ???
  @native def addr_bytes(v: Array[Byte], n: Int): Pointer[Byte] = ???
  @native def assign_long(p: Pointer[Long], v: Long): Unit = ???
  @native def assign_double(p: Pointer[Double], v: Double): Unit = ???
  @native def assign_int(p: Pointer[Int], v: Int): Unit = ???
  @native def assign_char(p: Pointer[Char], v: Char): Unit = ???
  @native def assign_bytes(p: Pointer[Byte], v: Array[Byte], n: Int): Unit = ???
  @native def sizeof_int(): Int = ???
  @native def sizeof_double(): Int = ???
  @native def sizeof_long(): Int = ???
  @native def sizeof_char(): Int = ???

  @native def addr_func1(v: CLangTypes.CFunc1): Pointer[_] = ???
  @native def addr_func2(v: CLangTypes.CFunc2): Pointer[_] = ???
  @native def addr_func3(v: CLangTypes.CFunc3): Pointer[_] = ???

  @native def eof(): Int = ???

  // Manually implemented functions
  @native def timeval_subtract(result: Pointer[CTimeVal], tv1: Pointer[CTimeVal], tv2: Pointer[CTimeVal]): Long = ???
}

object Native {
  type Pointer[T] = Long
  type CTimeVal = CSysTime.CTimeVal
  type CSize = Int
  type CFile = CLangTypes.CFile

  // stdlib.h
  //void	*malloc(size_t);
  @native def malloc[T](n: CSize): Pointer[T] = ???
  //void	 free(void *);
  @native def free[T](p: Pointer[T]): Unit = ???

  // stdio.h
  @native def stderr: Pointer[CFile] = ???
  @native def fopen(filename: Pointer[Char], mode: Pointer[Char]): Pointer[CFile] = ???
  @native def fopen(filename: String, mode: Pointer[Char]): Pointer[CFile] = ???
  @native def fopen(filename: Pointer[Char], mode: String): Pointer[CFile] = ???
  @native def fopen(filename: String, mode: String): Pointer[CFile] = ???
  @native def popen(f: Pointer[Char], mode: Pointer[Char]): Pointer[CFile] = ???
  @native def popen(f: String, mode: Pointer[Char]): Pointer[CFile] = ???
  @native def popen(f: Pointer[Char], mode: String): Pointer[CFile] = ???
  @native def popen(f: String, mode: String): Pointer[CFile] = ???
  @native def fprintf(f: Pointer[CFile], content: Pointer[Char]): Int = ???
  @native def fprintf(f: Pointer[CFile], content: String): Int = ???
  @native def fread[T](ptr: Pointer[T], size: Int, nitems: Int, stream: Pointer[CFile]): Int = ???
  @native def fwrite[T](ptr: Pointer[T], size: Int, nitems: Int, stream: Pointer[CFile]): Int = ???
  @native def feof(f: Pointer[CFile]): Boolean = ???
  @native def fclose(f: Pointer[CFile]): Int = ???
  @native def pclose(f: Pointer[CFile]): Int = ???
  @native def fseek(f: Pointer[CFile], offset: Long, whence: Int): Int = ???
  @native def fgetpos(f: Pointer[CFile], pos: Pointer[Long]): Int = ???
  @native def fsetpos(f: Pointer[CFile], pos: Pointer[Long]): Int = ???

  // sys/time.h
  @native def gettimeofday(tp: Pointer[CTimeVal], tzp: Pointer[Any]): Int = ???

  // string.h
  //void	*memchr(const void *, int, size_t);
  @native def memchr(s: Pointer[Byte], c: Int, n: CSize): Pointer[Byte] = ???
  //int	 memcmp(const void *, const void *, size_t);
  @native def memcmp(s1: Pointer[Byte], s2: Pointer[Byte], n: CSize): Int = ???
  //void	*memcpy(void *, const void *, size_t);
  @native def memcpy(dst: Pointer[Byte], src: Pointer[Byte], n: CSize): Pointer[Byte] = ???
  //void	*memmove(void *, const void *, size_t);
  @native def memmove(dst: Pointer[Byte], src: Pointer[Byte], n: CSize): Pointer[Byte] = ???
  //void	*memset(void *, int, size_t);
  @native def memset(s: Pointer[Byte], c: Int, n: CSize): Pointer[Byte] = ???
  //char	*strcat(char *, const char *);
  @native def strcat(s1: Pointer[Char], s2: Pointer[Char]): Pointer[Char] = ???
  @native def strcat(s1: Pointer[Char], s2: String): Pointer[Char] = ???
  //char	*strchr(const char *, int);
  @native def strchr(s: Pointer[Char], c: Int): Pointer[Char] = ???
  @native def strchr(s: String, c: Int): Pointer[Char] = ???
  //int	 strcmp(const char *, const char *);
  @native def strcmp(s1: Pointer[Char], s2: Pointer[Char]): Int = ???
  @native def strcmp(s1: String, s2: Pointer[Char]): Int = ???
  @native def strcmp(s1: Pointer[Char], s2: String): Int = ???
  @native def strcmp(s1: String, s2: String): Int = ???
  //int	 strcoll(const char *, const char *);
  @native def strcoll(s1: Pointer[Char], s2: Pointer[Char]): Int = ???
  @native def strcoll(s1: String, s2: Pointer[Char]): Int = ???
  @native def strcoll(s1: Pointer[Char], s2: String): Int = ???
  @native def strcoll(s1: String, s2: String): Int = ???
  //char	*strcpy(char *, const char *);
  @native def strcpy(dst: Pointer[Char], src: Pointer[Char]): Pointer[Char] = ???
  @native def strcpy(dst: Pointer[Char], src: String): Pointer[Char] = ???
  //size_t	 strcspn(const char *, const char *);
  @native def strcspn(s1: Pointer[Char], s2: Pointer[Char]): CSize = ???
  @native def strcspn(s1: Pointer[Char], s2: String): CSize = ???
  @native def strcspn(s1: String, s2: Pointer[Char]): CSize = ???
  @native def strcspn(s1: String, s2: String): CSize = ???
  //char	*strerror(int) __DARWIN_ALIAS(strerror);
  @native def strerror(errnum: Int): Pointer[Char] = ???
  //size_t	 strlen(const char *);
  @native def strlen(s: Pointer[Char]): CSize = ???
  @native def strlen(s: String): CSize = ???
  //char	*strncat(char *, const char *, size_t);
  @native def strncat(s1: Pointer[Char], s2: Pointer[Char], n: CSize): Pointer[Char] = ???
  @native def strncat(s1: Pointer[Char], s2: String, n: CSize): Pointer[Char] = ???
  //int	 strncmp(const char *, const char *, size_t);
  @native def strncmp(s1: Pointer[Char], s2: Pointer[Char], n: CSize): Int = ???
  @native def strncmp(s1: Pointer[Char], s2: String, n: CSize): Int = ???
  @native def strncmp(s1: String, s2: Pointer[Char], n: CSize): Int = ???
  @native def strncmp(s1: String, s2: String, n: CSize): Int = ???
  //char	*strncpy(char *, const char *, size_t);
  @native def strncpy(s1: Pointer[Char], s2: Pointer[Char], n: CSize): Pointer[Char] = ???
  @native def strncpy(s1: Pointer[Char], s2: String, n: CSize): Pointer[Char] = ???
  //char	*strpbrk(const char *, const char *);
  @native def strpbrk(s1: Pointer[Char], s2: Pointer[Char]): Pointer[Char] = ???
  @native def strpbrk(s1: Pointer[Char], s2: String): Pointer[Char] = ???
  @native def strpbrk(s1: String, s2: Pointer[Char]): Pointer[Char] = ???
  @native def strpbrk(s1: String, s2: String): Pointer[Char] = ???
  //char	*strrchr(const char *, int);
  @native def strrchr(s: Pointer[Char], c: Int): Pointer[Char] = ???
  @native def strrchr(s: String, c: Int): Pointer[Char] = ???
  //size_t	 strspn(const char *, const char *);
  @native def strspn(s1: Pointer[Char], s2: Pointer[Char]): CSize = ???
  @native def strspn(s1: String, s2: Pointer[Char]): CSize = ???
  @native def strspn(s1: Pointer[Char], s2: String): CSize = ???
  @native def strspn(s1: String, s2: String): CSize = ???
  //char	*strstr(const char *, const char *);
  @native def strstr(s1: Pointer[Char], s2: Pointer[Char]): Pointer[Char] = ???
  @native def strstr(s1: Pointer[Char], s2: String): Pointer[Char] = ???
  @native def strstr(s1: String, s2: Pointer[Char]): Pointer[Char] = ???
  @native def strstr(s1: String, s2: String): Pointer[Char] = ???
  //char	*strtok(char *, const char *);
  @native def strtok(s: Pointer[Char], sep: Pointer[Char]): Pointer[Char] = ???
  @native def strtok(s: Pointer[Char], sep: String): Pointer[Char] = ???
  //size_t	 strxfrm(char *, const char *, size_t);
  @native def strxfrm(s1: Pointer[Char], s2: Pointer[Char], n: CSize): CSize = ???
  @native def strxfrm(s1: Pointer[Char], s2: String, n: CSize): CSize = ???

  type GHashFunc[K] = Long
  type GEqualFunc[K] = Long
  type GHPointer[K, V] = Long
  type GBoolean = Int
  type GHRFunc[K, V] = Long
  type GHFunc[K, V] = Long
  type GLPointer[K] = Long

  @native def g_hash_table_new[K, V](hash: GHashFunc[K], equals: GEqualFunc[K]): GHPointer[K, V] = ???
  @native def g_hash_table_insert[K, V](ht: GHPointer[K, V], key: Pointer[K], value: Pointer[V]): gboolean = ???
  @native def g_hash_table_replace[K, V](ht: GHPointer[K, V], key: Pointer[K], value: Pointer[V]): gboolean = ???
  @native def g_hash_table_size(ht: GHPointer[_, _]): Int = ???
  @native def g_hash_table_lookup[K, V](ht: GHPointer[K, V], key: Pointer[K]): Pointer[V] = ???
  @native def g_hash_table_lookup_extended[K, V](ht: GHPointer[K, V], key: Pointer[K], origKey: Pointer[K], value: Pointer[V]): GBoolean = ???
  @native def g_hash_table_foreach[K, V](ht: GHPointer[K, V], func: GHFunc[K, V], userData: Any): Unit = ???
  @native def g_hash_table_find[K, V](ht: GHPointer[K, V], pred: GHRFunc[K, V], userData: Any): Pointer[V] = ???
  @native def g_hash_table_remove[K, V](ht: GHPointer[K, V], key: Pointer[K]): Int = ???
  @native def g_hash_table_remove_all[K, V](ht: GHPointer[K, V]): Unit = ???
  @native def g_hash_table_foreach_remove[K, V](ht: GHPointer[K, V], pred: GHRFunc[K, V], userData: Any): Int = ???
  @native def g_hash_table_get_keys[K](ht: GHPointer[K, _]): GLPointer[K] = ???
  @native def g_hash_table_get_values[V](ht: GHPointer[_, V]): GLPointer[V] = ???

  type GDestroy[T] = Long
  type GFunc[T] = Long
  type GCompareFunc[T] = Long

  @native def g_list_append[T](list: GLPointer[T], data: Pointer[T]): GLPointer[T] = ???
  @native def g_list_prepend[T](list: GLPointer[T], data: Pointer[T]): GLPointer[T] = ???
  @native def g_list_insert[T](list: GLPointer[T], data: Pointer[T], position: Int): GLPointer[T] = ???
  @native def g_list_insert_before[T](list: GLPointer[T], sibling: GLPointer[T], data: Pointer[T]): GLPointer[T] = ???
  @native def g_list_remove[T](list: GLPointer[T], data: Pointer[T]): GLPointer[T] = ???
  @native def g_list_remove_link[T](list: GLPointer[T], llink: GLPointer[T]): GLPointer[T] = ???
  @native def g_list_delete_link[T](list: GLPointer[T], llink: GLPointer[T]): GLPointer[T] = ???
  @native def g_list_remove_all[T](list: GLPointer[T], data: Pointer[T]): GLPointer[T] = ???
  @native def g_list_free(list: GLPointer[_]): Unit = ???
  @native def g_list_free_full[T](list: GLPointer[T], freeFunc: GDestroy[T]): Unit = ???
  @native def g_list_alloc[T](): GLPointer[T] = ???
  @native def g_list_length(list: GLPointer[_]): Int = ???
  @native def g_list_concat[T](list1: GLPointer[T], list2: GLPointer[T]): GLPointer[T] = ???
  @native def g_list_foreach[T](list: GLPointer[T], func: GFunc[T], userData: Any): Unit = ???
  @native def g_list_first[T](list: GLPointer[T]): GLPointer[T] = ???
  @native def g_list_last[T](list: GLPointer[T]): GLPointer[T] = ???
  @native def g_list_nth[T](list: GLPointer[T], n: Int): GLPointer[T] = ???
  @native def g_list_nth_data[T](list: GLPointer[T], n: Int): Pointer[T] = ???
  @native def g_list_nth_prev[T](list: GLPointer[T], n: Int): GLPointer[T] = ???
  @native def g_list_find[T](list: GLPointer[T], data: Pointer[T]): GLPointer[T] = ???
  @native def g_list_find_custom[T](list: GLPointer[T], data: Pointer[T], func: GCompareFunc[T]): GLPointer[T] = ???
  @native def g_list_position[T](list: GLPointer[T], llink: GLPointer[T]): Int = ???
  @native def g_list_index[T](list: GLPointer[T], data: Pointer[T]): Int = ???

  @native def g_list_next[T](list: GLPointer[T]): GLPointer[T] = ???

  class GArray
  type gconstpointer = Pointer[Any]
  type gpointer = Pointer[Any]
  type gboolean = gint
  type gint = Int
  type guint = Int
  type gchar = Char
  type GCompareDataFunc = Pointer[(gconstpointer, gconstpointer, gpointer) => gint]
  type GCompareFunc2 = Pointer[(gconstpointer, gconstpointer) => gint]
  type GDestroyNotify = Pointer[(gpointer) => Unit]

  @native def g_array_new(zero_terminated: Int, clear: Int, element_size: Int): Long = ???
  @native def g_array_sized_new(zero_terminated: gboolean, clear: gboolean, element_size: guint, reserved_size: guint): Pointer[GArray] = ???
  @native def g_array_free(array: Pointer[GArray], free_segment: gboolean): Pointer[gchar] = ???
  @native def g_array_ref(array: Pointer[GArray]): Pointer[GArray] = ???
  @native def g_array_unref(array: Pointer[GArray]): Unit = ???
  @native def g_array_get_element_size(array: Pointer[GArray]): guint = ???
  @native def g_array_append_vals(array: Pointer[GArray], data: gconstpointer, len: guint): Pointer[GArray] = ???
  @native def g_array_prepend_vals(array: Pointer[GArray], data: gconstpointer, len: guint): Pointer[GArray] = ???
  @native def g_array_insert_vals(array: Pointer[GArray], index: guint, data: gconstpointer, len: guint): Pointer[GArray] = ???
  @native def g_array_set_size(array: Pointer[GArray], length: guint): Pointer[GArray] = ???
  @native def g_array_remove_index(array: Pointer[GArray], index: guint): Pointer[GArray] = ???
  @native def g_array_remove_index_fast(array: Pointer[GArray], index: guint): Pointer[GArray] = ???
  @native def g_array_remove_range(array: Pointer[GArray], index: guint, length: guint): Pointer[GArray] = ???
  @native def g_array_sort(array: Pointer[GArray], compare_func: GCompareFunc2): Unit = ???
  @native def g_array_sort_with_data(array: Pointer[GArray], compare_func: GCompareDataFunc, user_data: gpointer): Unit = ???
  @native def g_array_set_clear_func(array: Pointer[GArray], clear_func: GDestroyNotify): Unit = ???

  @native class GTree
  type GTraverseType = Int
  type GTraverseFunc = Pointer[(gpointer, gpointer, gpointer) => gboolean]
  @native def g_tree_new(key_compare_func: GCompareFunc2): Pointer[GTree] = ???
  @native def g_tree_new_with_data(key_compare_func: GCompareDataFunc, key_compare_data: gpointer): Pointer[GTree] = ???
  @native def g_tree_new_full(key_compare_func: GCompareDataFunc, key_compare_data: gpointer, key_destroy_func: GDestroyNotify, value_destroy_func: GDestroyNotify): Pointer[GTree] = ???
  @native def g_tree_ref(tree: Pointer[GTree]): Pointer[GTree] = ???
  @native def g_tree_unref(tree: Pointer[GTree]): Unit = ???
  @native def g_tree_destroy(tree: Pointer[GTree]): Unit = ???
  @native def g_tree_insert(tree: Pointer[GTree], key: gpointer, value: gpointer): Unit = ???
  @native def g_tree_replace(tree: Pointer[GTree], key: gpointer, value: gpointer): Unit = ???
  @native def g_tree_remove(tree: Pointer[GTree], key: gconstpointer): gboolean = ???
  @native def g_tree_steal(tree: Pointer[GTree], key: gconstpointer): gboolean = ???
  @native def g_tree_lookup(tree: Pointer[GTree], key: gconstpointer): gpointer = ???
  @native def g_tree_lookup_extended(tree: Pointer[GTree], lookup_key: gconstpointer, orig_key: Pointer[gpointer], value: Pointer[gpointer]): gboolean = ???
  @native def g_tree_foreach(tree: Pointer[GTree], func: GTraverseFunc, user_data: gpointer): Unit = ???
  @native def g_tree_traverse(tree: Pointer[GTree], traverse_func: GTraverseFunc, traverse_type: GTraverseType, user_data: gpointer): Unit = ???
  @native def g_tree_search(tree: Pointer[GTree], search_func: GCompareFunc2, user_data: gconstpointer): gpointer = ???
  @native def g_tree_height(tree: Pointer[GTree]): gint = ???
  @native def g_tree_nnodes(tree: Pointer[GTree]): gint = ???
}
