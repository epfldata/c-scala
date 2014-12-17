C.scala: A Shallow DSL for C
============================

This project defines a Scala library for the core C language and some useful C
libraries. It also provides implementations of Scala collections such as HashMap
or List in the C DSL.

Installation
------------

The shallow C definitions are accompanied by implementations which use the JNI.
`sbt compile` will take care of generating and compiling the required C code,
provided that:

  * GLib is installed on your system. You can check this by making sure that
    `pkg-config --list-all | grep glib` prints something.

  * clang is installed on your system (`which clang` should not return an error).

  * The JNI headers for your system are as specified in `project/Build.scala`.

Please note that this has been tested only on Mac OS 10.9+.


The core C language
-------------------

The functionality of the C language itself is provided in the `cscala.CLang` package.
This includes functions such as `sizeof`, address-taking and dereferencing. 

This functionality is contained in the `cscala.CLang` package. The following import
is also needed in order to use it:

```scala
import cscala.CLangTypes._
```

To use the JNI implementations, the calling code needs to load the library
generated during compilation:

```scala
System.loadLibrary("shallow")
```

### Pointers

C.scala deals with pointers by providing the Scala type `Pointer[T]`. Pointers
to values can be obtained using the `&` method and those pointers can be
dereferenced using the `*` method:

```scala
val x: Int = 5
val px: Pointer[Int] = &(x)
println(*(x))
// => 5
```

Note: currently, calling `&` on the same value twice will return pointers to
different copies of the value.

It is possible to perform pointer arithmetic using the `+` and `-` methods.
Assignment is possible using `pointer_assign` or `update`. For example, using
`malloc` from the `cscala.StdLib` package it is possible to write array-like
operations:

```scala
val array: Pointer[Int] = malloc[Int](5) // Allocates space for 5 integers
for (i <- 0 until 5) {
  array(i) = i
}

println(*(array + 3))
// => 3
```

### Structs

It is possible to declare and use structs with C.scala, but declaration is
currently cumbersome. A struct is declared by declaring both a subclass of
`CStruct` and an instance of `CStructInfo[T]` for your new struct `T`. For
example:

```scala
class CTimeVal(val bytes: Array[Byte]) extends CStruct {
  def this(sec: Long, usec: Long) =
    this(ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN).putLong(sec).putLong(usec).array)
}
implicit val CTimeValInfo: CStructInfo[CTimeVal] = new CStructInfo[CTimeVal] {
  val sizes = List(
    'tv_sec -> sizeof[Long],
    'tv_usec -> sizeof[Long])
  def create(bytes: Array[Byte]) = new CTimeVal(bytes)
}
```

Structs can be created using `malloc`, `pointer_assign` and `+`, or by
simply creating a new instance of the `CStruct`. Member selection in pointers to
structs can be done using the `->` method:

```scala
val tv = new CTimeVal(12 ,45)
val p = &(tv)
println(->[CTimeVal, Long](p, 'tv_sec))
// => 12
```

### Casts

C programmers expect to be able to cast between types without runtime errors.
Since Scala casts will fail at runtime when attempting, for example, a cast from
a `Pointer[Int]` to a `Int`, C.scala provides an alternative way to cast via
the `as` method. `as[Int](p: Pointer[Int])` will cast the pointer to an int
(the underlying address pointed to) without failing at runtime.


C libraries
-----------

A handful of useful C libraries are provided by C.scala. Currently functions in
the following libraries are provided:

  * `stdlib.h` via the `cscala.CStdLib` package (only `free` and `malloc`).
  * `stdio.h` via the `cscala.CStdIO` package.
  * `sys/time.h` via the `cscala.CSysTime` package (only `gettimeofday`,
    `timeval_subtract` and the `timeval` struct
  * `string.h` via the `CString` pacakge.

Some collections from GLib are also provided:

  * [Doubly-linked lists](https://developer.gnome.org/glib/stable/glib-Doubly-Linked-Lists.html)
    via the `cscala.GListHeader` package.
  * [Hash tables](https://developer.gnome.org/glib/stable/glib-Hash-Tables.html)
    via the `cscala.GHashTableHeader` package.
  * [Arrays](https://developer.gnome.org/glib/stable/glib-Arrays.html) via the
  * `cscala.GArrayHeader` package.
  * [Balanced binary
  * trees](https://developer.gnome.org/glib/stable/glib-Balanced-Binary-Trees.html)
  * via the `cscala.GTreeHeader` package.

To use the GLib libraries, the following import is needed:

```scala
import cscala.GLibTypes._
```


Scala collections
-----------------

The libraries presented above can be used to prototype C implementations of
Scala collections. This has already been done for a few collections:

  * HashMaps in `cscala.collections.CHashMap`.
  * ArrayBuffers in `cscala.collections.CArrayBuffer`.
  * Lists in `cscala.collections.CList`.
  * Sets in `cscala.collections.CSet` (partially).
  * TreeSets in `cscala.collections.CTreeSet` (partially).

To use these collections, the following import is needed:

```scala
import cscala.collections.CCollectionsTypes._
```


Issues
======

  * Struct declaration and selection should be cleaned up so that `->` no longer
    needs type parameters all the time, and declaration can be done more easily
    (e.g. just a case class).

  * The `->` method should be on the `PointerOps` implicit class.

  * Casting is only partly implemented.

  * Varargs functions such as printf aren't implemented.

