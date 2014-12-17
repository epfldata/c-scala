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

  case class LPointer[T](val addr: Long)
  trait CStruct { val bytes: Array[Byte] }
  abstract class CStructInfo[T <: CStruct] {
    def sizes: List[(Symbol, CSize)]
    def apply(s: Symbol): CSize = sizes.takeWhile(_._1 != s).map(_._2).sum
    def create(bytes: Array[Byte]): T
  }
  abstract class CFunc1 { def apply(x: Long): Long }
  abstract class CFunc2 { def apply(x: Long, y: Long): Long }
  abstract class CFunc3 { def apply(x: Long, y: Long, z: Long): Long }

  trait CAddressable[T] { def addr(v: T): LPointer[T] }
  trait CDereferenceable[T] { def deref(v: LPointer[T]): T }
  trait CAssignable[T] { def assign(p: LPointer[T], v: T): Unit }
  trait CSizeable[T] { def size: CSize }
  trait CHashable[T] { def hash(v: T): Int }
  trait CComparable[T] { def equals(v1: T, v2: T): Int }

  trait CType[T] extends CAddressable[T] with CDereferenceable[T] with CAssignable[T] with CSizeable[T]
  trait CKeyType[T] extends CType[T] with CHashable[T] with CComparable[T]

  implicit val intType = new CKeyType[Int] {
    def addr(v: Int): LPointer[Int] = CLang.addr_int(v)
    def deref(v: LPointer[Int]): Int = CLang.deref_int(v)
    def assign(p: LPointer[Int], v: Int) = CLang.assign_int(p, v)
    def size = CLang.sizeof_int()
    def hash(n: Int) = n
    def equals(n1: Int, n2: Int) = if (n1 == n2) 1 else 0
  }
  implicit val longType = new CType[Long] {
    def addr(v: Long): LPointer[Long] = CLang.addr_long(v)
    def deref(v: LPointer[Long]): Long = CLang.deref_long(v)
    def assign(p: LPointer[Long], v: Long) = CLang.assign_long(p, v)
    def size = CLang.sizeof_long()
  }
  implicit val doubleType = new CKeyType[Double] {
    def addr(v: Double): LPointer[Double] = CLang.addr_double(v)
    def deref(v: LPointer[Double]): Double = CLang.deref_double(v)
    def assign(p: LPointer[Double], v: Double) = CLang.assign_double(p, v)
    def size = CLang.sizeof_double()
    def hash(n: Double) = (n / 10).toInt
    def equals(n1: Double, n2: Double) = if (n1 == n2) 1 else 0
  }
  implicit val charType = new CType[Char] {
    def addr(v: Char): LPointer[Char] = CLang.addr_char(v)
    def deref(v: LPointer[Char]): Char = CLang.deref_char(v)
    def assign(p: LPointer[Char], v: Char) = CLang.assign_char(p, v)
    def size = CLang.sizeof_char()
  }
  implicit val stringType = new CType[String] {
    def addr(v: String): LPointer[String] = {
      val a = CStdLib.malloc[Char](v.size * CLang.sizeof[Char])
      CString.strcpy(a, v)
      a.asInstanceOf[LPointer[String]]
    }
    def deref(v: LPointer[String]): String = {
      var c = v.asInstanceOf[LPointer[Char]]
      val bs = CLang.deref_bytes(c.asInstanceOf[LPointer[Array[Byte]]], CString.strlen(c))
      bs.map(_.toChar).mkString
    }
    def assign(p: LPointer[String], v: String) = {
      CString.strcpy(p.asInstanceOf[LPointer[Char]], v)
    }
    def size = CLang.sizeof_long()
  }
  implicit def pointerType[T] = new CType[LPointer[T]] {
    def addr(v: LPointer[T]): LPointer[LPointer[T]] =
      CLang.addr_long(v.addr).asInstanceOf[LPointer[LPointer[T]]]
    def deref(v: LPointer[LPointer[T]]): LPointer[T] =
      new LPointer(CLang.deref_long(v.asInstanceOf[LPointer[Long]]))
    def assign(p: LPointer[LPointer[T]], v: LPointer[T]) = CLang.assign_long(p.asInstanceOf[LPointer[Long]], v.addr)
    def size = CLang.sizeof_long()
  }
  implicit def structType[T <: CStruct: CStructInfo] = new CType[T] {
    def addr(v: T): LPointer[T] = CLang.addr_bytes(v.bytes, size).asInstanceOf[LPointer[T]]
    def deref(v: LPointer[T]): T = {
      val bytes = CLang.deref_bytes(v.asInstanceOf[LPointer[Array[Byte]]], size)
      implicitly[CStructInfo[T]].create(bytes)
    }
    def assign(p: LPointer[T], v: T) = CLang.assign_bytes(p.asInstanceOf[LPointer[Array[Byte]]], v.bytes, size)
    def size = implicitly[CStructInfo[T]].sizes.map(_._2).sum
  }
  implicit def functionType[T: CDereferenceable, U: CAddressable] = new CAddressable[T => U] {
    def addr(f: T => U): LPointer[T => U] = CLang.addr_func1(new CFunc1 {
      def apply(x: Long): Long = {
        val arg = implicitly[CDereferenceable[T]].deref(new LPointer(x))
        val res = f(arg)
        implicitly[CAddressable[U]].addr(res).addr
      }
    })
  }
  implicit def function2Type[T1: CDereferenceable, T2: CDereferenceable, U: CAddressable] = new CAddressable[(T1, T2) => U] {
    def addr(f: (T1, T2) => U): LPointer[(T1, T2) => U] = CLang.addr_func2(new CFunc2 {
      def apply(x: Long, y: Long): Long = {
        val arg1 = implicitly[CDereferenceable[T1]].deref(new LPointer(x))
        val arg2 = implicitly[CDereferenceable[T2]].deref(new LPointer(y))
        val res = f(arg1, arg2)
        implicitly[CAddressable[U]].addr(res).addr
      }
    })
  }
  implicit def function3Type[T1: CDereferenceable, T2: CDereferenceable, T3: CDereferenceable, U: CAddressable] = new CAddressable[(T1, T2, T3) => U] {
    def addr(f: (T1, T2, T3) => U): LPointer[(T1, T2, T3) => U] = CLang.addr_func3(new CFunc3 {
      def apply(x: Long, y: Long, z: Long): Long = {
        val arg1 = implicitly[CDereferenceable[T1]].deref(new LPointer(x))
        val arg2 = implicitly[CDereferenceable[T2]].deref(new LPointer(y))
        val arg3 = implicitly[CDereferenceable[T3]].deref(new LPointer(z))
        val res = f(arg1, arg2, arg3)
        implicitly[CAddressable[U]].addr(res).addr
      }
    })
  }
  implicit def voidType = new CAddressable[Unit] {
    def addr(v: Unit): LPointer[Unit] = new LPointer(0L)
  }

  implicit class LPointerOps[T: CType](self: LPointer[T]) {
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

  def deref_long(v: LPointer[Long]): Long = CLangNative.deref_long(v.addr)
  def deref_double(v: LPointer[Double]): Double = CLangNative.deref_double(v.addr)
  def deref_int(v: LPointer[Int]): Int = CLangNative.deref_int(v.addr)
  def deref_char(v: LPointer[Char]): Char = CLangNative.deref_char(v.addr)
  def deref_bytes(v: LPointer[Array[Byte]], n: Int): Array[Byte] = CLangNative.deref_bytes(v.addr, n)
  def addr_long(v: Long): LPointer[Long] = new LPointer(CLangNative.addr_long(v))
  def addr_double(v: Double): LPointer[Double] = new LPointer(CLangNative.addr_double(v))
  def addr_int(v: Int): LPointer[Int] = new LPointer(CLangNative.addr_int(v))
  def addr_char(v: Char): LPointer[Char] = new LPointer(CLangNative.addr_char(v))
  def addr_bytes(v: Array[Byte], n: Int): LPointer[Array[Byte]] = new LPointer(CLangNative.addr_bytes(v, n))
  def addr_func1[T1, U](v: CFunc1): LPointer[T1 => U] = new LPointer(CLangNative.addr_func1(v))
  def addr_func2[T1, T2, U](v: CFunc2): LPointer[(T1, T2) => U] = new LPointer(CLangNative.addr_func2(v))
  def addr_func3[T1, T2, T3, U](v: CFunc3): LPointer[(T1, T2, T3) => U] = new LPointer(CLangNative.addr_func3(v))
  def assign_long(p: LPointer[Long], v: Long): Unit = CLangNative.assign_long(p.addr, v)
  def assign_double(p: LPointer[Double], v: Double): Unit = CLangNative.assign_double(p.addr, v)
  def assign_int(p: LPointer[Int], v: Int): Unit = CLangNative.assign_int(p.addr, v)
  def assign_char(p: LPointer[Char], v: Char): Unit = CLangNative.assign_char(p.addr, v)
  def assign_bytes(p: LPointer[Array[Byte]], v: Array[Byte], n: Int): Unit = CLangNative.assign_bytes(p.addr, v, n)
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
  def pointer_add[T: CType](p: LPointer[T], n: Int): LPointer[T] = new LPointer[T](p.addr + n * sizeof[T])
  /**
   * Pointer subtraction
   *
   * The pointer will be decreased by a certain number of elements. Each
   * element might be multiple bytes long (as specified by `sizeof[T]`)
   *
   * @param p the pointer to decrease
   * @param n the number of elements to decrease the pointer by
   */
  def pointer_sub[T: CType](p: LPointer[T], n: Int): LPointer[T] = new LPointer[T](p.addr - n * sizeof[T])

  /** Breakable while loops */
  def __whileDo(cond: Boolean, body: => Unit): Unit = breakable(while (cond) { body })
  /** Used to break out of a breakable while loop */
  def break(): Unit = scala.util.control.Breaks.break

  /** The NULL pointer */
  def NULL[T] = new LPointer[T](0L)
  /** EOF, the value returned when fscanf is at the end of the file */
  def EOF(): Int = CLangNative.eof()
  /** Take the address of a value */
  def &[T: CAddressable](v: T): LPointer[T] = implicitly[CAddressable[T]].addr(v)
  /** Dereference a pointer */
  def *[T: CDereferenceable](v: LPointer[T]): T = implicitly[CDereferenceable[T]].deref(v)
  /**
   * Assign a value to a pointer
   * @param p the pointer to assign to
   * @param v the value to assign
   */
  def pointer_assign[T: CAssignable](p: LPointer[T], v: T): Unit = implicitly[CAssignable[T]].assign(p, v)
  /** Get the size of a type */
  def sizeof[T: CSizeable]: CSize = implicitly[CSizeable[T]].size
  /**
   * Select a field in a struct
   * @param struct the struct to select from
   * @param field the field to select
   */
  def ->[T <: CStruct: CStructInfo, U: CDereferenceable](struct: LPointer[T], field: String): U = {
    val info = implicitly[CStructInfo[T]]
    val addr = struct.addr + info(Symbol(field))
    *(new LPointer[U](addr))
  }

  /**
   * Select a field in a struct
   * @param struct the struct to select from
   * @param field the field to select
   */
  def ->[T <: CStruct: CStructInfo, U: CDereferenceable](struct: LPointer[T], field: Symbol): U = {
    val info = implicitly[CStructInfo[T]]
    val addr = struct.addr + info(field)
    *(new LPointer[U](addr))
  }

  /** C-like cast */
  def as[T: CType](x: Any): T = x match {
    case x: LPointer[_] if implicitly[CType[T]] == intType => x.addr.toInt.asInstanceOf[T]
    case x: Int => new LPointer(x.toLong).asInstanceOf[T]
    case x: LPointer[_] if implicitly[CType[T]] == doubleType => x.addr.toDouble.asInstanceOf[T]
    case x: Double => new LPointer(x.toLong).asInstanceOf[T]
    case _ => x.asInstanceOf[T]
  }

  def debugMsg(fd: LPointer[CFile], text: String, xs: Any*): Unit = ()
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
  def malloc[T](count: Int): LPointer[T] = new LPointer(Native.malloc(count))
  /** Free allocated space pointer to by a pointer */
  def free[T](ptr: LPointer[T]) = Native.free(ptr.addr)
}

/**
 * <stdio.h>
 *
 * Functions for using C files and streams, as defined in the `stdio.h` header.
 */
object CStdIO {
  import CLang._
  import CLangTypes._

  def stderr(): LPointer[CFile] = new LPointer(Native.stderr)
  def fopen(filename: LPointer[Char], mode: LPointer[Char]): LPointer[CFile] = new LPointer(Native.fopen(filename.addr, mode.addr))
  def fopen(filename: String, mode: LPointer[Char]): LPointer[CFile] = new LPointer(Native.fopen(filename, mode.addr))
  def fopen(filename: LPointer[Char], mode: String): LPointer[CFile] = new LPointer(Native.fopen(filename.addr, mode))
  def fopen(filename: String, mode: String): LPointer[CFile] = new LPointer(Native.fopen(filename, mode))
  def popen(f: LPointer[Char], mode: LPointer[Char]): LPointer[CFile] = new LPointer(Native.popen(f.addr, mode.addr))
  def popen(f: String, mode: LPointer[Char]): LPointer[CFile] = new LPointer(Native.popen(f, mode.addr))
  def popen(f: LPointer[Char], mode: String): LPointer[CFile] = new LPointer(Native.popen(f.addr, mode))
  def popen(f: String, mode: String): LPointer[CFile] = new LPointer(Native.popen(f, mode))
  def fscanf(f: LPointer[CFile], s: String, l: LPointer[Any]*): Int = ???
  def fprintf(f: LPointer[CFile], content: LPointer[Char]): Int = Native.fprintf(f.addr, content.addr)
  def fprintf(f: LPointer[CFile], content: String): Int = Native.fprintf(f.addr, content)
  def fread[T](ptr: LPointer[T], size: Int, nitems: Int, stream: LPointer[CFile]): Int = Native.fread(ptr.addr, size, nitems, stream.addr)
  def fwrite[T](ptr: LPointer[T], size: Int, nitems: Int, stream: LPointer[CFile]): Int = Native.fwrite(ptr.addr, size, nitems, stream.addr)
  def feof(f: LPointer[CFile]): Boolean = Native.feof(f.addr)
  def fclose(f: LPointer[CFile]): Int = Native.fclose(f.addr)
  def pclose(f: LPointer[CFile]): Int = Native.pclose(f.addr)
  def fseek(f: LPointer[CFile], offset: Long, whence: Int): Int = Native.fseek(f.addr, offset, whence)
  def fgetpos(f: LPointer[CFile], pos: LPointer[Long]): Int = Native.fgetpos(f.addr, pos.addr)
  def fsetpos(f: LPointer[CFile], pos: LPointer[Long]): Int = Native.fsetpos(f.addr, pos.addr)
  def sprintf(str: LPointer[Char], format: LPointer[Char], xs: Any*): Int = ???
  def sprintf(str: String, format: LPointer[Char], xs: Any*): Int = ???
  def sprintf(str: LPointer[Char], format: String, xs: Any*): Int = ???
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

  def gettimeofday(timep: LPointer[CTimeVal], tzp: LPointer[Any]): Int = Native.gettimeofday(timep.addr, tzp.addr)
  def timeval_subtract(result: LPointer[CTimeVal], tv1: LPointer[CTimeVal], tv2: LPointer[CTimeVal]): Long = CLangNative.timeval_subtract(result.addr, tv1.addr, tv2.addr)
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
  type CString = LPointer[Char]

  //void	*memchr(const void *, int, size_t);
  def memchr(s: LPointer[Byte], c: Int, n: CSize): LPointer[Byte] = new LPointer(Native.memchr(s.addr, c, n))
  //int	 memcmp(const void *, const void *, size_t);
  def memcmp(s1: LPointer[Byte], s2: LPointer[Byte], n: CSize): Int = Native.memcmp(s1.addr, s2.addr, n)
  //void	*memcpy(void *, const void *, size_t);
  def memcpy(dst: LPointer[Byte], src: LPointer[Byte], n: CSize): LPointer[Byte] = new LPointer(Native.memcpy(dst.addr, src.addr, n))
  //void	*memmove(void *, const void *, size_t);
  def memmove(dst: LPointer[Byte], src: LPointer[Byte], n: CSize): LPointer[Byte] = new LPointer(Native.memmove(dst.addr, src.addr, n))
  //void	*memset(void *, int, size_t);
  def memset(s: LPointer[Byte], c: Int, n: CSize): LPointer[Byte] = new LPointer(Native.memset(s.addr, c, n))
  //char	*strcat(char *, const char *);
  def strcat(s1: LPointer[Char], s2: LPointer[Char]): LPointer[Char] = new LPointer(Native.strcat(s1.addr, s2.addr))
  def strcat(s1: LPointer[Char], s2: String): LPointer[Char] = new LPointer(Native.strcat(s1.addr, s2))
  //char	*strchr(const char *, int);
  def strchr(s: LPointer[Char], c: Int): LPointer[Char] = new LPointer(Native.strchr(s.addr, c))
  def strchr(s: String, c: Int): LPointer[Char] = new LPointer(Native.strchr(s, c))
  //int	 strcmp(const char *, const char *);
  def strcmp(s1: LPointer[Char], s2: LPointer[Char]): Int = Native.strcmp(s1.addr, s2.addr)
  def strcmp(s1: String, s2: LPointer[Char]): Int = Native.strcmp(s1, s2.addr)
  def strcmp(s1: LPointer[Char], s2: String): Int = Native.strcmp(s1.addr, s2)
  def strcmp(s1: String, s2: String): Int = Native.strcmp(s1, s2)
  //int	 strcoll(const char *, const char *);
  def strcoll(s1: LPointer[Char], s2: LPointer[Char]): Int = Native.strcoll(s1.addr, s2.addr)
  def strcoll(s1: String, s2: LPointer[Char]): Int = Native.strcoll(s1, s2.addr)
  def strcoll(s1: LPointer[Char], s2: String): Int = Native.strcoll(s1.addr, s2)
  def strcoll(s1: String, s2: String): Int = Native.strcoll(s1, s2)
  //char	*strcpy(char *, const char *);
  def strcpy(dst: LPointer[Char], src: LPointer[Char]): LPointer[Char] = new LPointer(Native.strcpy(dst.addr, src.addr))
  def strcpy(dst: LPointer[Char], src: String): LPointer[Char] = new LPointer(Native.strcpy(dst.addr, src))
  //size_t	 strcspn(const char *, const char *);
  def strcspn(s1: LPointer[Char], s2: LPointer[Char]): CSize = Native.strcspn(s1.addr, s2.addr)
  def strcspn(s1: LPointer[Char], s2: String): CSize = Native.strcspn(s1.addr, s2)
  def strcspn(s1: String, s2: LPointer[Char]): CSize = Native.strcspn(s1, s2.addr)
  def strcspn(s1: String, s2: String): CSize = Native.strcspn(s1, s2)
  //char	*strerror(int) __DARWIN_ALIAS(strerror);
  def strerror(errnum: Int): LPointer[Char] = new LPointer(Native.strerror(errnum))
  //size_t	 strlen(const char *);
  def strlen(s: LPointer[Char]): CSize = Native.strlen(s.addr)
  def strlen(s: String): CSize = Native.strlen(s)
  //char	*strncat(char *, const char *, size_t);
  def strncat(s1: LPointer[Char], s2: LPointer[Char], n: CSize): LPointer[Char] = new LPointer(Native.strncat(s1.addr, s2.addr, n))
  def strncat(s1: LPointer[Char], s2: String, n: CSize): LPointer[Char] = new LPointer(Native.strncat(s1.addr, s2, n))
  //int	 strncmp(const char *, const char *, size_t);
  def strncmp(s1: LPointer[Char], s2: LPointer[Char], n: CSize): Int = Native.strncmp(s1.addr, s2.addr, n)
  def strncmp(s1: LPointer[Char], s2: String, n: CSize): Int = Native.strncmp(s1.addr, s2, n)
  def strncmp(s1: String, s2: LPointer[Char], n: CSize): Int = Native.strncmp(s1, s2.addr, n)
  def strncmp(s1: String, s2: String, n: CSize): Int = Native.strncmp(s1, s2, n)
  //char	*strncpy(char *, const char *, size_t);
  def strncpy(s1: LPointer[Char], s2: LPointer[Char], n: CSize): LPointer[Char] = new LPointer(Native.strncpy(s1.addr, s2.addr, n))
  def strncpy(s1: LPointer[Char], s2: String, n: CSize): LPointer[Char] = new LPointer(Native.strncpy(s1.addr, s2, n))
  //char	*strpbrk(const char *, const char *);
  def strpbrk(s1: LPointer[Char], s2: LPointer[Char]): LPointer[Char] = new LPointer(Native.strpbrk(s1.addr, s2.addr))
  def strpbrk(s1: LPointer[Char], s2: String): LPointer[Char] = new LPointer(Native.strpbrk(s1.addr, s2))
  def strpbrk(s1: String, s2: LPointer[Char]): LPointer[Char] = new LPointer(Native.strpbrk(s1, s2.addr))
  def strpbrk(s1: String, s2: String): LPointer[Char] = new LPointer(Native.strpbrk(s1, s2))
  //char	*strrchr(const char *, int);
  def strrchr(s: LPointer[Char], c: Int): LPointer[Char] = new LPointer(Native.strrchr(s.addr, c))
  def strrchr(s: String, c: Int): LPointer[Char] = new LPointer(Native.strrchr(s, c))
  //size_t	 strspn(const char *, const char *);
  def strspn(s1: LPointer[Char], s2: LPointer[Char]): CSize = Native.strspn(s1.addr, s2.addr)
  def strspn(s1: String, s2: LPointer[Char]): CSize = Native.strspn(s1, s2.addr)
  def strspn(s1: LPointer[Char], s2: String): CSize = Native.strspn(s1.addr, s2)
  def strspn(s1: String, s2: String): CSize = Native.strspn(s1, s2)
  //char	*strstr(const char *, const char *);
  def strstr(s1: LPointer[Char], s2: LPointer[Char]): LPointer[Char] = new LPointer(Native.strstr(s1.addr, s2.addr))
  def strstr(s1: LPointer[Char], s2: String): LPointer[Char] = new LPointer(Native.strstr(s1.addr, s2))
  def strstr(s1: String, s2: LPointer[Char]): LPointer[Char] = new LPointer(Native.strstr(s1, s2.addr))
  def strstr(s1: String, s2: String): LPointer[Char] = new LPointer(Native.strstr(s1, s2))
  //char	*strtok(char *, const char *);
  def strtok(s: LPointer[Char], sep: LPointer[Char]): LPointer[Char] = new LPointer(Native.strtok(s.addr, sep.addr))
  def strtok(s: LPointer[Char], sep: String): LPointer[Char] = new LPointer(Native.strtok(s.addr, sep))
  //size_t	 strxfrm(char *, const char *, size_t);
  def strxfrm(s1: LPointer[Char], s2: LPointer[Char], n: CSize): CSize = Native.strxfrm(s1.addr, s2.addr, n)
  def strxfrm(s1: LPointer[Char], s2: String, n: CSize): CSize = Native.strxfrm(s1.addr, s2, n)
}
