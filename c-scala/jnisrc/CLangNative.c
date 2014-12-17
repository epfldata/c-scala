#include <jni.h>
#include "CLangNative.h"
#include <sys/time.h>
#include <arpa/inet.h>
#include <stdio.h>
/*
 * Class:     ch_epfl_data_cscala_CLangNative__
 * Method:    deref_long
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_ch_epfl_data_cscala_CLangNative_00024_deref_1long
  (JNIEnv *env, jobject self, jlong ptr) {
  	globalenv = env;
  	jlong* longptr = (jlong*)ptr;
  	return *longptr;
}

/*
 * Class:     ch_epfl_data_cscala_CLangNative__
 * Method:    deref_double
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_ch_epfl_data_cscala_CLangNative_00024_deref_1double
  (JNIEnv *env, jobject self, jlong ptr) {
  	globalenv = env;
  	jdouble* doubleptr = (jdouble*)ptr;
  	return *doubleptr;
}

/*
 * Class:     ch_epfl_data_cscala_CLangNative__
 * Method:    deref_int
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_ch_epfl_data_cscala_CLangNative_00024_deref_1int
  (JNIEnv *env, jobject self, jlong ptr) {
  	globalenv = env;
  	jint* intptr = (jint*)ptr;
  	return *intptr;
}

/*
 * Class:     ch_epfl_data_cscala_CLangNative__
 * Method:    deref_char
 * Signature: (J)C
 */
JNIEXPORT jchar JNICALL Java_ch_epfl_data_cscala_CLangNative_00024_deref_1char
  (JNIEnv *env, jobject self, jlong ptr) {
  	globalenv = env;
  	jchar* charptr = (jchar*)ptr;
  	return *charptr;
}

/*
 * Class:     ch_epfl_data_cscala_CLangNative__
 * Method:    deref_bytes
 * Signature: (JI)[B
 */
JNIEXPORT jbyteArray JNICALL Java_ch_epfl_data_cscala_CLangNative_00024_deref_1bytes
  (JNIEnv *env, jobject self, jlong ptr, jint n) {
  	globalenv = env;
  	jbyte* byteptr = (jbyte*)ptr;
  	jbyteArray bytearray = (*env)->NewByteArray(env, n);
  	(*env)->SetByteArrayRegion(env, bytearray, 0, n, byteptr);
  	return bytearray;
}

/*
 * Class:     ch_epfl_data_cscala_CLangNative__
 * Method:    sizeof_int
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_ch_epfl_data_cscala_CLangNative_00024_sizeof_1int
  (JNIEnv *env, jobject self) {
  	globalenv = env;
  	return sizeof(int);
}

/*
 * Class:     ch_epfl_data_cscala_CLangNative__
 * Method:    sizeof_long
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_ch_epfl_data_cscala_CLangNative_00024_sizeof_1long
  (JNIEnv *env, jobject self) {
  	globalenv = env;
  	return sizeof(long);
}

/*
 * Class:     ch_epfl_data_cscala_CLangNative__
 * Method:    sizeof_double
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_ch_epfl_data_cscala_CLangNative_00024_sizeof_1double
  (JNIEnv * env, jobject self) {
  	globalenv = env;
  	return sizeof(double);
}

/*
 * Class:     ch_epfl_data_cscala_CLangNative__
 * Method:    sizeof_char
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_ch_epfl_data_cscala_CLangNative_00024_sizeof_1char
  (JNIEnv *env, jobject self) {
  	globalenv = env;
  	return sizeof(char);
}

/*
 * Class:     ch_epfl_data_cscala_CLangNative__
 * Method:    addr_long
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_ch_epfl_data_cscala_CLangNative_00024_addr_1long
  (JNIEnv *env, jobject self, jlong val) {
  	globalenv = env;
  	jlong* ptr = malloc(sizeof(jlong));
  	memcpy(ptr, &val, sizeof(jlong));
  	return ptr;
}
/*
 * Class:     ch_epfl_data_cscala_CLangNative__
 * Method:    addr_double
 * Signature: (D)J
 */
JNIEXPORT jlong JNICALL Java_ch_epfl_data_cscala_CLangNative_00024_addr_1double
  (JNIEnv *env, jobject self, jdouble val) {
  	globalenv = env;
  	jdouble* ptr = malloc(sizeof(jdouble));
  	memcpy(ptr, &val, sizeof(jdouble));
  	return ptr;
}

/*
 * Class:     ch_epfl_data_cscala_CLangNative__
 * Method:    addr_int
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_ch_epfl_data_cscala_CLangNative_00024_addr_1int
  (JNIEnv *env, jobject self, jint val) {
  	globalenv = env;
  	jint* ptr = malloc(sizeof(jint));
  	memcpy(ptr, &val, sizeof(jint));
  	return ptr;
}

/*
 * Class:     ch_epfl_data_cscala_CLangNative__
 * Method:    addr_char
 * Signature: (C)J
 */
JNIEXPORT jlong JNICALL Java_ch_epfl_data_cscala_CLangNative_00024_addr_1char
  (JNIEnv *env, jobject self, jchar val) {
  	globalenv = env;
  	jchar* ptr = malloc(sizeof(jchar));
  	memcpy(ptr, &val, sizeof(jchar));
  	return ptr;
}

/*
 * Class:     ch_epfl_data_cscala_CLangNative__
 * Method:    addr_bytes
 * Signature: ([BI)J
 */
JNIEXPORT jlong JNICALL Java_ch_epfl_data_cscala_CLangNative_00024_addr_1bytes
  (JNIEnv *env, jobject self, jbyteArray bytearray, jint n) {
  	globalenv = env;
  	jbyte* bytes = (*env)->GetByteArrayElements(env, bytearray, NULL);
  	return bytes;
}

/*
 * Class:     ch_epfl_data_cscala_CLangNative__
 * Method:    assign_long
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_ch_epfl_data_cscala_CLangNative_00024_assign_1long
  (JNIEnv *env, jobject self, jlong ptr, jlong val) {
  	globalenv = env;
  	jlong* longptr = (jlong*)ptr;
  	*longptr = val;
}

/*
 * Class:     ch_epfl_data_cscala_CLangNative__
 * Method:    assign_double
 * Signature: (JD)V
 */
JNIEXPORT void JNICALL Java_ch_epfl_data_cscala_CLangNative_00024_assign_1double
  (JNIEnv *env, jobject self, jlong ptr, jdouble val) {
  	globalenv = env;
  	jdouble* doubleptr = (jdouble*)ptr;
  	*doubleptr = val;
}

/*
 * Class:     ch_epfl_data_cscala_CLangNative__
 * Method:    assign_int
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_ch_epfl_data_cscala_CLangNative_00024_assign_1int
  (JNIEnv *env, jobject self, jlong ptr, jint val) {
  	globalenv = env;
  	jint* intptr = (jint*)ptr;
  	*intptr = val;
}

/*
 * Class:     ch_epfl_data_cscala_CLangNative__
 * Method:    assign_char
 * Signature: (JC)V
 */
JNIEXPORT void JNICALL Java_ch_epfl_data_cscala_CLangNative_00024_assign_1char
  (JNIEnv *env, jobject self, jlong ptr, jchar val) {
  	globalenv = env;
  	jchar* charptr = (jchar*)ptr;
  	*charptr = val;
}

/*
 * Class:     ch_epfl_data_cscala_CLangNative__
 * Method:    assign_bytes
 * Signature: (J[BI)V
 */
JNIEXPORT void JNICALL Java_ch_epfl_data_cscala_CLangNative_00024_assign_1bytes
  (JNIEnv *env, jobject self, jlong ptr, jbyteArray bytearray, jint n) {
  	globalenv = env;
  	jbyte* bytes = (*env)->GetByteArrayElements(env, bytearray, NULL);
  	memcpy(ptr, bytes, n);
}

/*
 * Class:     ch_epfl_data_cscala_CLangNative__
 * Method:    addr_func1
 * Signature: (Lch/epfl/data/cscala/CLang/CFunc1;)J
 */
jobject* globalfunc1;
jlong pointerFunction1(jlong arg) {
  jclass cls = (*globalenv)->GetObjectClass(globalenv, *globalfunc1);
  jmethodID mid = (*globalenv)->GetMethodID(globalenv, cls, "apply", "(J)J");
  jlong* res = (*globalenv)->CallLongMethod(globalenv, *globalfunc1, mid, &arg);
  return *res;
}

JNIEXPORT jlong JNICALL Java_ch_epfl_data_cscala_CLangNative_00024_addr_1func1
  (JNIEnv *env, jobject self, jobject func) {
  	globalenv = env;
  	jobject global = (*env)->NewGlobalRef(env, func);
  	globalfunc1 = malloc(sizeof(jobject));
  	memcpy(globalfunc1, &global, sizeof(jobject));
    return &pointerFunction1;
}

/*
 * Class:     ch_epfl_data_cscala_CLangNative__
 * Method:    addr_func2
 * Signature: (Lch/epfl/data/cscala/CLang/CFunc2;)J
 */
jobject* globalfunc2;
jlong pointerFunction2(jlong arg1, jlong arg2) {
  jclass cls = (*globalenv)->GetObjectClass(globalenv, *globalfunc2);
  jmethodID mid = (*globalenv)->GetMethodID(globalenv, cls, "apply", "(JJ)J");
  jlong* res = (*globalenv)->CallLongMethod(globalenv, *globalfunc2, mid, &arg1, &arg2);
  return *res;
}

JNIEXPORT jlong JNICALL Java_ch_epfl_data_cscala_CLangNative_00024_addr_1func2
  (JNIEnv *env, jobject self, jobject func) {
  	globalenv = env;
  	jobject global = (*env)->NewGlobalRef(env, func);
  	globalfunc2 = malloc(sizeof(jobject));
  	memcpy(globalfunc2, &global, sizeof(jobject));
    return &pointerFunction2;
}

/*
 * Class:     ch_epfl_data_cscala_CLangNative__
 * Method:    addr_func3
 * Signature: (Lch/epfl/data/cscala/CLang/CFunc3;)J
 */
jobject* globalfunc3;
void pointerFunction3(jlong arg1, jlong arg2, jlong arg3) {
  jclass cls = (*globalenv)->GetObjectClass(globalenv, *globalfunc3);
  jmethodID mid = (*globalenv)->GetMethodID(globalenv, cls, "apply", "(JJJ)J");
  jlong* res = (*globalenv)->CallLongMethod(globalenv, *globalfunc3, mid, &arg1, &arg2, &arg3);
}
JNIEXPORT jlong JNICALL Java_ch_epfl_data_cscala_CLangNative_00024_addr_1func3
  (JNIEnv *env, jobject self, jobject func) {
  	globalenv = env;
  	jobject global = (*env)->NewGlobalRef(env, func);
  	globalfunc3 = malloc(sizeof(jobject));
  	memcpy(globalfunc3, &global, sizeof(jobject));
    return &pointerFunction3;
}

/*
 * Class:     ch_epfl_data_cscala_CLangNative__
 * Method:    eof
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_ch_epfl_data_cscala_CLangNative_00024_eof
  (JNIEnv *env, jobject self) {
  	globalenv = env;
    return EOF;
}

/*
 * Class:     ch_epfl_data_cscala_CLangNative__
 * Method:    timeval_subtract
 * Signature: (JJJ)J
 */
JNIEXPORT jlong JNICALL Java_ch_epfl_data_cscala_CLangNative_00024_timeval_1subtract
  (JNIEnv *env, jobject self, jlong resultl, jlong t2l, jlong t1l) {
  	globalenv = env;
  	struct timeval *result = resultl;
  	struct timeval *t1 = t1l;
  	struct timeval *t2 = t2l;
   	int diff = (t2->tv_usec + 1000000 * t2->tv_sec) - (t1->tv_usec + 1000000 * t1->tv_sec);
    result->tv_sec = diff / 1000000;
    result->tv_usec = diff % 1000000;
    return ((result->tv_sec * 1000) + (result->tv_usec/1000));
}

