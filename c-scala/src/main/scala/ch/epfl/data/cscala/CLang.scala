package ch.epfl.data
package cscala

import scala.util.control.Breaks._
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * This package contain type classes for dereferencing, assigning to, etc.
 * pointers. It should always be imported.
 */
object CLangTypes {
  type CSize = Int
  class CFile

  case class Pointer[T](val addr: Long)
  trait CStruct { val bytes: Array[Byte] }
  abstract class CStructInfo[T <: CStruct] {
    def sizes: List[(Symbol, CSize)]
    def apply(s: Symbol): CSize = sizes.takeWhile(_._1 != s).map(_._2).sum
    def create(bytes: Array[Byte]): T
  }
  abstract class CFunc1 { def apply(x: Long): Long }
  abstract class CFunc2 { def apply(x: Long, y: Long): Long }
  abstract class CFunc3 { def apply(x: Long, y: Long, z: Long): Long }

  trait CAddressable[T] { def addr(v: T): Pointer[T] }
  trait CDereferenceable[T] { def deref(v: Pointer[T]): T }
  trait CAssignable[T] { def assign(p: Pointer[T], v: T): Unit }
  trait CSizeable[T] { def size: CSize }
  trait CHashable[T] { def hash(v: T): Int }
  trait CComparable[T] { def equals(v1: T, v2: T): Int }

  trait CType[T] extends CAddressable[T] with CDereferenceable[T] with CAssignable[T] with CSizeable[T]
  trait CKeyType[T] extends CType[T] with CHashable[T] with CComparable[T]

  implicit val intType = new CKeyType[Int] {
    def addr(v: Int): Pointer[Int] = CLang.addr_int(v)
    def deref(v: Pointer[Int]): Int = CLang.deref_int(v)
    def assign(p: Pointer[Int], v: Int) = CLang.assign_int(p, v)
    def size = CLang.sizeof_int()
    def hash(n: Int) = n
    def equals(n1: Int, n2: Int) = if (n1 == n2) 1 else 0
  }
  implicit val longType = new CType[Long] {
    def addr(v: Long): Pointer[Long] = CLang.addr_long(v)
    def deref(v: Pointer[Long]): Long = CLang.deref_long(v)
    def assign(p: Pointer[Long], v: Long) = CLang.assign_long(p, v)
    def size = CLang.sizeof_long()
  }
  implicit val doubleType = new CKeyType[Double] {
    def addr(v: Double): Pointer[Double] = CLang.addr_double(v)
    def deref(v: Pointer[Double]): Double = CLang.deref_double(v)
    def assign(p: Pointer[Double], v: Double) = CLang.assign_double(p, v)
    def size = CLang.sizeof_double()
    def hash(n: Double) = (n / 10).toInt
    def equals(n1: Double, n2: Double) = if (n1 == n2) 1 else 0
  }
  implicit val charType = new CType[Char] {
    def addr(v: Char): Pointer[Char] = CLang.addr_char(v)
    def deref(v: Pointer[Char]): Char = CLang.deref_char(v)
    def assign(p: Pointer[Char], v: Char) = CLang.assign_char(p, v)
    def size = CLang.sizeof_char()
  }
  implicit val stringType = new CType[String] {
    def addr(v: String): Pointer[String] = {
      val a = CStdLib.malloc[Char](v.size * CLang.sizeof[Char])
      CString.strcpy(a, v)
      a.asInstanceOf[Pointer[String]]
    }
    def deref(v: Pointer[String]): String = {
      var c = v.asInstanceOf[Pointer[Char]]
      val bs = CLang.deref_bytes(c.asInstanceOf[Pointer[Array[Byte]]], CString.strlen(c))
      bs.map(_.toChar).mkString
    }
    def assign(p: Pointer[String], v: String) = {
      CString.strcpy(p.asInstanceOf[Pointer[Char]], v)
    }
    def size = CLang.sizeof_long()
  }
  implicit def pointerType[T] = new CType[Pointer[T]] {
    def addr(v: Pointer[T]): Pointer[Pointer[T]] =
      CLang.addr_long(v.addr).asInstanceOf[Pointer[Pointer[T]]]
    def deref(v: Pointer[Pointer[T]]): Pointer[T] =
      new Pointer(CLang.deref_long(v.asInstanceOf[Pointer[Long]]))
    def assign(p: Pointer[Pointer[T]], v: Pointer[T]) = CLang.assign_long(p.asInstanceOf[Pointer[Long]], v.addr)
    def size = CLang.sizeof_long()
  }
  implicit def structType[T <: CStruct: CStructInfo] = new CType[T] {
    def addr(v: T): Pointer[T] = CLang.addr_bytes(v.bytes, size).asInstanceOf[Pointer[T]]
    def deref(v: Pointer[T]): T = {
      val bytes = CLang.deref_bytes(v.asInstanceOf[Pointer[Array[Byte]]], size)
      implicitly[CStructInfo[T]].create(bytes)
    }
    def assign(p: Pointer[T], v: T) = CLang.assign_bytes(p.asInstanceOf[Pointer[Array[Byte]]], v.bytes, size)
    def size = implicitly[CStructInfo[T]].sizes.map(_._2).sum
  }
  implicit def functionType[T: CDereferenceable, U: CAddressable] = new CAddressable[T => U] {
    def addr(f: T => U): Pointer[T => U] = CLang.addr_func1(new CFunc1 {
      def apply(x: Long): Long = {
        val arg = implicitly[CDereferenceable[T]].deref(new Pointer(x))
        val res = f(arg)
        implicitly[CAddressable[U]].addr(res).addr
      }
    })
  }
  implicit def function2Type[T1: CDereferenceable, T2: CDereferenceable, U: CAddressable] = new CAddressable[(T1, T2) => U] {
    def addr(f: (T1, T2) => U): Pointer[(T1, T2) => U] = CLang.addr_func2(new CFunc2 {
      def apply(x: Long, y: Long): Long = {
        val arg1 = implicitly[CDereferenceable[T1]].deref(new Pointer(x))
        val arg2 = implicitly[CDereferenceable[T2]].deref(new Pointer(y))
        val res = f(arg1, arg2)
        implicitly[CAddressable[U]].addr(res).addr
      }
    })
  }
  implicit def function3Type[T1: CDereferenceable, T2: CDereferenceable, T3: CDereferenceable, U: CAddressable] = new CAddressable[(T1, T2, T3) => U] {
    def addr(f: (T1, T2, T3) => U): Pointer[(T1, T2, T3) => U] = CLang.addr_func3(new CFunc3 {
      def apply(x: Long, y: Long, z: Long): Long = {
        val arg1 = implicitly[CDereferenceable[T1]].deref(new Pointer(x))
        val arg2 = implicitly[CDereferenceable[T2]].deref(new Pointer(y))
        val arg3 = implicitly[CDereferenceable[T3]].deref(new Pointer(z))
        val res = f(arg1, arg2, arg3)
        implicitly[CAddressable[U]].addr(res).addr
      }
    })
  }
  implicit def voidType = new CAddressable[Unit] {
    def addr(v: Unit): Pointer[Unit] = new Pointer(0L)
  }

  implicit class PointerOps[T: CType](self: Pointer[T]) {
    def +(n: Int) = CLang.pointer_add(self, n)
    def -(n: Int) = CLang.pointer_sub(self, n)
    def update(i: Int, v: T) = CLang.pointer_assign(CLang.pointer_add(self, i), v)
  }
}

/**
 * The core C language
 *
 * Methods for pointer operations, loops, special values (NULL, EOF), casting
 * and sizeof.
 */
object CLang {
  System.loadLibrary("shallow")
  import CLangTypes._

  def deref_long(v: Pointer[Long]): Long = CLangNative.deref_long(v.addr)
  def deref_double(v: Pointer[Double]): Double = CLangNative.deref_double(v.addr)
  def deref_int(v: Pointer[Int]): Int = CLangNative.deref_int(v.addr)
  def deref_char(v: Pointer[Char]): Char = CLangNative.deref_char(v.addr)
  def deref_bytes(v: Pointer[Array[Byte]], n: Int): Array[Byte] = CLangNative.deref_bytes(v.addr, n)
  def addr_long(v: Long): Pointer[Long] = new Pointer(CLangNative.addr_long(v))
  def addr_double(v: Double): Pointer[Double] = new Pointer(CLangNative.addr_double(v))
  def addr_int(v: Int): Pointer[Int] = new Pointer(CLangNative.addr_int(v))
  def addr_char(v: Char): Pointer[Char] = new Pointer(CLangNative.addr_char(v))
  def addr_bytes(v: Array[Byte], n: Int): Pointer[Array[Byte]] = new Pointer(CLangNative.addr_bytes(v, n))
  def addr_func1[T1, U](v: CFunc1): Pointer[T1 => U] = new Pointer(CLangNative.addr_func1(v))
  def addr_func2[T1, T2, U](v: CFunc2): Pointer[(T1, T2) => U] = new Pointer(CLangNative.addr_func2(v))
  def addr_func3[T1, T2, T3, U](v: CFunc3): Pointer[(T1, T2, T3) => U] = new Pointer(CLangNative.addr_func3(v))
  def assign_long(p: Pointer[Long], v: Long): Unit = CLangNative.assign_long(p.addr, v)
  def assign_double(p: Pointer[Double], v: Double): Unit = CLangNative.assign_double(p.addr, v)
  def assign_int(p: Pointer[Int], v: Int): Unit = CLangNative.assign_int(p.addr, v)
  def assign_char(p: Pointer[Char], v: Char): Unit = CLangNative.assign_char(p.addr, v)
  def assign_bytes(p: Pointer[Array[Byte]], v: Array[Byte], n: Int): Unit = CLangNative.assign_bytes(p.addr, v, n)
  def sizeof_long(): Int = CLangNative.sizeof_long()
  def sizeof_double(): Int = CLangNative.sizeof_double()
  def sizeof_int(): Int = CLangNative.sizeof_int()
  def sizeof_char(): Int = CLangNative.sizeof_char()

  /**
   * Pointer addition
   *
   * The pointer will be increased by a certain number of elements. Each element
   * might be multiple bytes long (as specified by `sizeof[T]`).
   *
   * @param p the pointer to increase
   * @param n the number of elements to increase the pointer by
   */
  def pointer_add[T: CType](p: Pointer[T], n: Int): Pointer[T] = new Pointer[T](p.addr + n * sizeof[T])
  /**
   * Pointer subtraction
   *
   * The pointer will be decreased by a certain number of elements. Each
   * element might be multiple bytes long (as specified by `sizeof[T]`)
   *
   * @param p the pointer to decrease
   * @param n the number of elements to decrease the pointer by
   */
  def pointer_sub[T: CType](p: Pointer[T], n: Int): Pointer[T] = new Pointer[T](p.addr - n * sizeof[T])

  /** Breakable while loops */
  def __whileDo(cond: Boolean, body: => Unit): Unit = breakable(while (cond) { body })
  /** Used to break out of a breakable while loop */
  def break(): Unit = scala.util.control.Breaks.break

  /** The NULL pointer */
  def NULL[T] = new Pointer[T](0L)
  /** EOF, the value returned when fscanf is at the end of the file */
  def EOF(): Int = CLangNative.eof()
  /** Take the address of a value */
  def &[T: CAddressable](v: T): Pointer[T] = implicitly[CAddressable[T]].addr(v)
  /** Dereference a pointer */
  def *[T: CDereferenceable](v: Pointer[T]): T = implicitly[CDereferenceable[T]].deref(v)
  /**
   * Assign a value to a pointer
   * @param p the pointer to assign to
   * @param v the value to assign
   */
  def pointer_assign[T: CAssignable](p: Pointer[T], v: T): Unit = implicitly[CAssignable[T]].assign(p, v)
  /** Get the size of a type */
  def sizeof[T: CSizeable]: CSize = implicitly[CSizeable[T]].size
  /**
   * Select a field in a struct
   * @param struct the struct to select from
   * @param field the field to select
   */
  def ->[T <: CStruct: CStructInfo, U: CDereferenceable](struct: Pointer[T], field: String): U = {
    val info = implicitly[CStructInfo[T]]
    val addr = struct.addr + info(Symbol(field))
    *(new Pointer[U](addr))
  }

  /**
   * Select a field in a struct
   * @param struct the struct to select from
   * @param field the field to select
   */
  def ->[T <: CStruct: CStructInfo, U: CDereferenceable](struct: Pointer[T], field: Symbol): U = {
    val info = implicitly[CStructInfo[T]]
    val addr = struct.addr + info(field)
    *(new Pointer[U](addr))
  }

  /** C-like cast */
  def as[T: CType](x: Any): T = x match {
    case x: Pointer[_] if implicitly[CType[T]] == intType => x.addr.toInt.asInstanceOf[T]
    case x: Int => new Pointer(x.toLong).asInstanceOf[T]
    case x: Pointer[_] if implicitly[CType[T]] == doubleType => x.addr.toDouble.asInstanceOf[T]
    case x: Double => new Pointer(x.toLong).asInstanceOf[T]
    case _ => x.asInstanceOf[T]
  }

  def debugMsg(fd: Pointer[CFile], text: String, xs: Any*): Unit = ()
}

/**
 * <stdlib.h>
 *
 * Just malloc and free for allocating object on the heap.
 */
object CStdLib {
  import CLang._
  import CLangTypes._

  /**
   * Allocate bytes on the heap
   * @param count the number of bytes to allocate
   */
  def malloc[T](count: Int): Pointer[T] = new Pointer(Native.malloc(count))
  /** Free allocated space pointer to by a pointer */
  def free[T](ptr: Pointer[T]) = Native.free(ptr.addr)
}

/**
 * <stdio.h>
 *
 * Functions for using C files and streams, as defined in the `stdio.h` header.
 */
object CStdIO {
  import CLang._
  import CLangTypes._

  def stderr(): Pointer[CFile] = new Pointer(Native.stderr)
  def fopen(filename: Pointer[Char], mode: Pointer[Char]): Pointer[CFile] = new Pointer(Native.fopen(filename.addr, mode.addr))
  def fopen(filename: String, mode: Pointer[Char]): Pointer[CFile] = new Pointer(Native.fopen(filename, mode.addr))
  def fopen(filename: Pointer[Char], mode: String): Pointer[CFile] = new Pointer(Native.fopen(filename.addr, mode))
  def fopen(filename: String, mode: String): Pointer[CFile] = new Pointer(Native.fopen(filename, mode))
  def popen(f: Pointer[Char], mode: Pointer[Char]): Pointer[CFile] = new Pointer(Native.popen(f.addr, mode.addr))
  def popen(f: String, mode: Pointer[Char]): Pointer[CFile] = new Pointer(Native.popen(f, mode.addr))
  def popen(f: Pointer[Char], mode: String): Pointer[CFile] = new Pointer(Native.popen(f.addr, mode))
  def popen(f: String, mode: String): Pointer[CFile] = new Pointer(Native.popen(f, mode))
  def fscanf(f: Pointer[CFile], s: String, l: Pointer[Any]*): Int = ???
  def fprintf(f: Pointer[CFile], content: Pointer[Char]): Int = Native.fprintf(f.addr, content.addr)
  def fprintf(f: Pointer[CFile], content: String): Int = Native.fprintf(f.addr, content)
  def fread[T](ptr: Pointer[T], size: Int, nitems: Int, stream: Pointer[CFile]): Int = Native.fread(ptr.addr, size, nitems, stream.addr)
  def fwrite[T](ptr: Pointer[T], size: Int, nitems: Int, stream: Pointer[CFile]): Int = Native.fwrite(ptr.addr, size, nitems, stream.addr)
  def feof(f: Pointer[CFile]): Boolean = Native.feof(f.addr)
  def fclose(f: Pointer[CFile]): Int = Native.fclose(f.addr)
  def pclose(f: Pointer[CFile]): Int = Native.pclose(f.addr)
  def fseek(f: Pointer[CFile], offset: Long, whence: Int): Int = Native.fseek(f.addr, offset, whence)
  def fgetpos(f: Pointer[CFile], pos: Pointer[Long]): Int = Native.fgetpos(f.addr, pos.addr)
  def fsetpos(f: Pointer[CFile], pos: Pointer[Long]): Int = Native.fsetpos(f.addr, pos.addr)
  def sprintf(str: Pointer[Char], format: Pointer[Char], xs: Any*): Int = ???
  def sprintf(str: String, format: Pointer[Char], xs: Any*): Int = ???
  def sprintf(str: Pointer[Char], format: String, xs: Any*): Int = ???
  def sprintf(str: String, format: String, xs: Any*): Int = ???
}

/**
 * <sys/time.h>
 *
 * `gettimeofday` and the `timeval` struct. There is also a timeval subtraction
 * function which is not in `sys/time.h`.
 */
object CSysTime {
  import CLang._
  import CLangTypes._

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

  def gettimeofday(timep: Pointer[CTimeVal], tzp: Pointer[Any]): Int = Native.gettimeofday(timep.addr, tzp.addr)
  def timeval_subtract(result: Pointer[CTimeVal], tv1: Pointer[CTimeVal], tv2: Pointer[CTimeVal]): Long = CLangNative.timeval_subtract(result.addr, tv1.addr, tv2.addr)
}

/**
 * <string.h>
 *
 * Functions for dealing with strings as well as other useful memory-related
 * functions (`memcpy`, `memset`, etc.)
 */
object CString {
  import CLang._
  import CLangTypes._
  type CString = Pointer[Char]

  //void	*memchr(const void *, int, size_t);
  def memchr(s: Pointer[Byte], c: Int, n: CSize): Pointer[Byte] = new Pointer(Native.memchr(s.addr, c, n))
  //int	 memcmp(const void *, const void *, size_t);
  def memcmp(s1: Pointer[Byte], s2: Pointer[Byte], n: CSize): Int = Native.memcmp(s1.addr, s2.addr, n)
  //void	*memcpy(void *, const void *, size_t);
  def memcpy(dst: Pointer[Byte], src: Pointer[Byte], n: CSize): Pointer[Byte] = new Pointer(Native.memcpy(dst.addr, src.addr, n))
  //void	*memmove(void *, const void *, size_t);
  def memmove(dst: Pointer[Byte], src: Pointer[Byte], n: CSize): Pointer[Byte] = new Pointer(Native.memmove(dst.addr, src.addr, n))
  //void	*memset(void *, int, size_t);
  def memset(s: Pointer[Byte], c: Int, n: CSize): Pointer[Byte] = new Pointer(Native.memset(s.addr, c, n))
  //char	*strcat(char *, const char *);
  def strcat(s1: Pointer[Char], s2: Pointer[Char]): Pointer[Char] = new Pointer(Native.strcat(s1.addr, s2.addr))
  def strcat(s1: Pointer[Char], s2: String): Pointer[Char] = new Pointer(Native.strcat(s1.addr, s2))
  //char	*strchr(const char *, int);
  def strchr(s: Pointer[Char], c: Int): Pointer[Char] = new Pointer(Native.strchr(s.addr, c))
  def strchr(s: String, c: Int): Pointer[Char] = new Pointer(Native.strchr(s, c))
  //int	 strcmp(const char *, const char *);
  def strcmp(s1: Pointer[Char], s2: Pointer[Char]): Int = Native.strcmp(s1.addr, s2.addr)
  def strcmp(s1: String, s2: Pointer[Char]): Int = Native.strcmp(s1, s2.addr)
  def strcmp(s1: Pointer[Char], s2: String): Int = Native.strcmp(s1.addr, s2)
  def strcmp(s1: String, s2: String): Int = Native.strcmp(s1, s2)
  //int	 strcoll(const char *, const char *);
  def strcoll(s1: Pointer[Char], s2: Pointer[Char]): Int = Native.strcoll(s1.addr, s2.addr)
  def strcoll(s1: String, s2: Pointer[Char]): Int = Native.strcoll(s1, s2.addr)
  def strcoll(s1: Pointer[Char], s2: String): Int = Native.strcoll(s1.addr, s2)
  def strcoll(s1: String, s2: String): Int = Native.strcoll(s1, s2)
  //char	*strcpy(char *, const char *);
  def strcpy(dst: Pointer[Char], src: Pointer[Char]): Pointer[Char] = new Pointer(Native.strcpy(dst.addr, src.addr))
  def strcpy(dst: Pointer[Char], src: String): Pointer[Char] = new Pointer(Native.strcpy(dst.addr, src))
  //size_t	 strcspn(const char *, const char *);
  def strcspn(s1: Pointer[Char], s2: Pointer[Char]): CSize = Native.strcspn(s1.addr, s2.addr)
  def strcspn(s1: Pointer[Char], s2: String): CSize = Native.strcspn(s1.addr, s2)
  def strcspn(s1: String, s2: Pointer[Char]): CSize = Native.strcspn(s1, s2.addr)
  def strcspn(s1: String, s2: String): CSize = Native.strcspn(s1, s2)
  //char	*strerror(int) __DARWIN_ALIAS(strerror);
  def strerror(errnum: Int): Pointer[Char] = new Pointer(Native.strerror(errnum))
  //size_t	 strlen(const char *);
  def strlen(s: Pointer[Char]): CSize = Native.strlen(s.addr)
  def strlen(s: String): CSize = Native.strlen(s)
  //char	*strncat(char *, const char *, size_t);
  def strncat(s1: Pointer[Char], s2: Pointer[Char], n: CSize): Pointer[Char] = new Pointer(Native.strncat(s1.addr, s2.addr, n))
  def strncat(s1: Pointer[Char], s2: String, n: CSize): Pointer[Char] = new Pointer(Native.strncat(s1.addr, s2, n))
  //int	 strncmp(const char *, const char *, size_t);
  def strncmp(s1: Pointer[Char], s2: Pointer[Char], n: CSize): Int = Native.strncmp(s1.addr, s2.addr, n)
  def strncmp(s1: Pointer[Char], s2: String, n: CSize): Int = Native.strncmp(s1.addr, s2, n)
  def strncmp(s1: String, s2: Pointer[Char], n: CSize): Int = Native.strncmp(s1, s2.addr, n)
  def strncmp(s1: String, s2: String, n: CSize): Int = Native.strncmp(s1, s2, n)
  //char	*strncpy(char *, const char *, size_t);
  def strncpy(s1: Pointer[Char], s2: Pointer[Char], n: CSize): Pointer[Char] = new Pointer(Native.strncpy(s1.addr, s2.addr, n))
  def strncpy(s1: Pointer[Char], s2: String, n: CSize): Pointer[Char] = new Pointer(Native.strncpy(s1.addr, s2, n))
  //char	*strpbrk(const char *, const char *);
  def strpbrk(s1: Pointer[Char], s2: Pointer[Char]): Pointer[Char] = new Pointer(Native.strpbrk(s1.addr, s2.addr))
  def strpbrk(s1: Pointer[Char], s2: String): Pointer[Char] = new Pointer(Native.strpbrk(s1.addr, s2))
  def strpbrk(s1: String, s2: Pointer[Char]): Pointer[Char] = new Pointer(Native.strpbrk(s1, s2.addr))
  def strpbrk(s1: String, s2: String): Pointer[Char] = new Pointer(Native.strpbrk(s1, s2))
  //char	*strrchr(const char *, int);
  def strrchr(s: Pointer[Char], c: Int): Pointer[Char] = new Pointer(Native.strrchr(s.addr, c))
  def strrchr(s: String, c: Int): Pointer[Char] = new Pointer(Native.strrchr(s, c))
  //size_t	 strspn(const char *, const char *);
  def strspn(s1: Pointer[Char], s2: Pointer[Char]): CSize = Native.strspn(s1.addr, s2.addr)
  def strspn(s1: String, s2: Pointer[Char]): CSize = Native.strspn(s1, s2.addr)
  def strspn(s1: Pointer[Char], s2: String): CSize = Native.strspn(s1.addr, s2)
  def strspn(s1: String, s2: String): CSize = Native.strspn(s1, s2)
  //char	*strstr(const char *, const char *);
  def strstr(s1: Pointer[Char], s2: Pointer[Char]): Pointer[Char] = new Pointer(Native.strstr(s1.addr, s2.addr))
  def strstr(s1: Pointer[Char], s2: String): Pointer[Char] = new Pointer(Native.strstr(s1.addr, s2))
  def strstr(s1: String, s2: Pointer[Char]): Pointer[Char] = new Pointer(Native.strstr(s1, s2.addr))
  def strstr(s1: String, s2: String): Pointer[Char] = new Pointer(Native.strstr(s1, s2))
  //char	*strtok(char *, const char *);
  def strtok(s: Pointer[Char], sep: Pointer[Char]): Pointer[Char] = new Pointer(Native.strtok(s.addr, sep.addr))
  def strtok(s: Pointer[Char], sep: String): Pointer[Char] = new Pointer(Native.strtok(s.addr, sep))
  //size_t	 strxfrm(char *, const char *, size_t);
  def strxfrm(s1: Pointer[Char], s2: Pointer[Char], n: CSize): CSize = Native.strxfrm(s1.addr, s2.addr, n)
  def strxfrm(s1: Pointer[Char], s2: String, n: CSize): CSize = Native.strxfrm(s1.addr, s2, n)
}
