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

class CLangTest extends FlatSpec with ShouldMatchers {
  System.loadLibrary("shallow")

  "Shallow C" should "get and dereference pointers with & and *" in {
    val s = 1
    val p = &(s)
    *(p) should be(1)
  }

  it should "assign to pointers with pointer_assign" in {
    val s = 1
    val p = &(s)
    pointer_assign(p, 2)
    *(p) should be(2)
  }

  it should "perform pointer arithmetic" in {
    val arr1 = malloc[Int](2 * sizeof[Int])
    pointer_assign(arr1, 1)
    val arr2 = pointer_add(arr1, 1)
    pointer_assign(arr2, 2)

    *(arr1) should be(1)
    *(arr2) should be(2)
    *(pointer_sub(arr2, 1)) should be(1)
  }

  it should "get the size of various types" in {
    sizeof[Int] should be(4)
    sizeof[Long] should be(8)
    sizeof[Char] should be(1)
    sizeof[CTimeVal] should be(16)
  }

  it should "get and dereference struct pointers" in {
    val struct = new CTimeVal(12, 45)
    val p = &(struct)
    ->[CTimeVal, Long](p, 'tv_sec) should be(12)
    ->[CTimeVal, Long](p, 'tv_usec) should be(45)
    *(p).bytes should be(struct.bytes)
  }

  it should "provide breakable while loops" in {
    val x = malloc[Int](1 * sizeof[Int])
    pointer_assign(x, 0)
    __whileDo(true, {
      if (*(x) == 3) break
      else pointer_assign(x, *(x) + 1)
    })
    *(x) should be(3)
  }

  it should "allow addressing and dereferencing strings" in {
    *(&("hello")) should be("hello")
  }

  "Shallow string.h" should "concatenate strings with strcat" in {
    val s1 = malloc[Char](5 * sizeof[Char])
    strcpy(s1, "hel")
    strcmp(s1, "hello") should not be (0)
    strcat(s1, "lo")
    strcmp(s1, "hello") should be(0)
    free(s1)
  }

  it should "span strings with strspn" in {
    val s1 = malloc[Char](5 * sizeof[Char])
    strcpy(s1, "hello")
    strspn(s1, "ieh") should be(2)
    strspn(s1, "ehl") should be(4)
    free(s1)
  }

  it should "find the length of a string with strlen" in {
    strlen("hello") should be(5)
    val s1 = malloc[Char](7 * sizeof[Char])
    strcpy(s1, "hello")
    strlen(s1) should be(5)
    strcpy(s1, "1234567")
    strlen(s1) should be(7)
    free(s1)
  }

  it should "find a substring with strstr" in {
    val s1 = malloc[Char](13 * sizeof[Char])
    strcpy(s1, "hello, world!")
    strstr(s1, "world").addr should be(s1.addr + 7)
    free(s1)
  }

  it should "find a character with strchr" in {
    val s1 = malloc[Char](15 * sizeof[Char])
    strcpy(s1, "abcdefghijklmno")
    strchr(s1, 'g').addr should be(s1.addr + 6)
    free(s1)
  }

  "Shallow sys/time.h" should "create times" in {
    val s1 = malloc[CTimeVal](sizeof[CTimeVal])
    val s2 = malloc[CTimeVal](sizeof[CTimeVal])
    gettimeofday(s1, NULL)
    Thread.sleep(2000)
    gettimeofday(s2, NULL)
    val t1: Long = ->[CTimeVal, Long](s1, 'tv_sec)
    val t2: Long = ->[CTimeVal, Long](s2, 'tv_sec)
    (t2 - t1) should be(2)
  }

  "Shallow stdio.h" should "perform file operations" in {
    val file = fopen("test.txt", "w+")
    val str = malloc[Char](5 * sizeof[Char])
    strcpy(str, "hello")
    fwrite(str, 1, 5, file) should be(5)

    fseek(file, 0, 0) should be(0)

    val read = malloc[Char](5 * sizeof[Char])
    fread(read, 1, 5, file) should be(5)

    strcmp(read, "hello") should be(0)

    fclose(file) should be(0)

    new File("test.txt").delete()
  }

  it should "make stderr available for writing" in {
    val str = malloc[Char](5 * sizeof[Char])
    strcpy(str, "hello")
    fwrite(str, 1, 5, stderr) should be(5)
  }
}
