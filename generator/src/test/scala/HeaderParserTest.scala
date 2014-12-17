package ch.epfl.data
package cscala
package generator
package test

import scala.util.parsing.input._

import org.scalatest.{ FlatSpec, ShouldMatchers }

class HeaderParserTest extends FlatSpec with ShouldMatchers {
  import HeaderParser._

  "Header parser" should "parse a JNI declaration" in {
    val input =
      """/*
        | * Class:     ch_epfl_data_cscala_Native__
        | * Method:    strspn
        | * Signature: (JJ)I
        | */
        |JNIEXPORT jint JNICALL Java_ch_epfl_data_cscala_Native_00024_strspn__JJ
        |  (JNIEnv *, jobject, jlong, jlong);""".stripMargin
    val parsed = parseAll(declaration, input)
    parsed match {
      case Success(Declaration("jint", "strspn", _,
        Argument("JNIEnv *") :: Argument("jobject") :: _), _) => ()
      case _ => assert(false, parsed)
    }
  }

  it should "create a stub with the right body" in {
    val input =
      """/*
        | * Class:     ch_epfl_data_cscala_Native__
        | * Method:    strspn
        | * Signature: (JJ)I
        | */
        |JNIEXPORT jint JNICALL Java_ch_epfl_data_cscala_Native_00024_strspn__JJ
        |  (JNIEnv *, jobject, jlong, jlong);""".stripMargin
    val Success(decl, _) = parseAll(declaration, input)
    val expected =
      """JNIEXPORT jint JNICALL Java_ch_epfl_data_cscala_Native_00024_strspn__JJ(JNIEnv * arg0, jobject arg1, jlong arg2, jlong arg3) {
        |  globalenv = arg0;
        |  return strspn(arg2, arg3);
        |}""".stripMargin
    makeStub(decl) should be(expected)
  }

  it should "extract the chars from jstrings" in {
    val input =
      """/*
        | * Class:     ch_epfl_data_cscala_Native__
        | * Method:    strspn
        | * Signature: (Ljava/lang/String;J)I
        | */
        |JNIEXPORT jint JNICALL Java_ch_epfl_data_cscala_Native_00024_strspn__Ljava_lang_String_2J
        |  (JNIEnv *, jobject, jstring, jlong);""".stripMargin
    val Success(decl, _) = parseAll(declaration, input)
    val expected =
      """JNIEXPORT jint JNICALL Java_ch_epfl_data_cscala_Native_00024_strspn__Ljava_lang_String_2J(JNIEnv * arg0, jobject arg1, jstring arg2, jlong arg3) {
        |  globalenv = arg0;
        |  return strspn((*arg0)->GetStringUTFChars(arg0, arg2, NULL), arg3);
        |}""".stripMargin
    makeStub(decl) should be(expected)
  }

  it should "generate an entire source file" in {
    val input =
      """#include <jni.h>
        |
        |#ifndef _Included_ch_epfl_data_cscala_Native__
        |#define _Included_ch_epfl_data_cscala_Native__
        |#ifdef __cplusplus
        |extern "C" {
        |#endif
        |/*
        | * Class:     ch_epfl_data_cscala_Native__
        | * Method:    malloc
        | * Signature: (I)J
        | */
        |JNIEXPORT jlong JNICALL Java_ch_epfl_data_cscala_Native_00024_malloc
        |  (JNIEnv *, jobject, jint);
        |
        |/*
        | * Class:     ch_epfl_data_cscala_Native__
        | * Method:    strcpy
        | * Signature: (JJ)J
        | */
        |JNIEXPORT jlong JNICALL Java_ch_epfl_data_cscala_Native_00024_strcpy__JJ
        |  (JNIEnv *, jobject, jlong, jlong);
        |
        |#ifdef __cplusplus
        |}
        |#endif
        |#endif""".stripMargin
    val expected =
      """#include "Native.h"
        |#include <stdlib.h>
        |#include <string.h>
        |#include <glib.h>
        |#include "CLangNative.h"
        |JNIEXPORT jlong JNICALL Java_ch_epfl_data_cscala_Native_00024_malloc(JNIEnv * arg0, jobject arg1, jint arg2) {
        |  globalenv = arg0;
        |  return malloc(arg2);
        |}
        |JNIEXPORT jlong JNICALL Java_ch_epfl_data_cscala_Native_00024_strcpy__JJ(JNIEnv * arg0, jobject arg1, jlong arg2, jlong arg3) {
        |  globalenv = arg0;
        |  return strcpy(arg2, arg3);
        |}""".stripMargin

    makeSource(input, "Native.h") should be(expected)
  }
}
