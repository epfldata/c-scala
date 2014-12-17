package ch.epfl.data
package cscala
package generator

import java.io.{ FileOutputStream, PrintStream }
import scala.io.Source
import scala.util.parsing.combinator.RegexParsers

case class Argument(typeName: String)
case class Declaration(returnType: String, methodName: String, functionName: String, arguments: List[Argument])

object HeaderParser extends RegexParsers {
  def preamble: Parser[_] = rep1(
    "/\\* .* \\*/".r
      | "#.*".r
      | "extern .*".r)
  def postamble: Parser[_] = rep(
    "#.*".r
      | "}".r)
  def declarations: Parser[List[Declaration]] = rep(declaration)
  def declaration: Parser[Declaration] = {
    "/*" ~
      "* Class:" ~ ident ~
      "* Method:" ~ ident ~
      "* Signature:" ~ ".*".r ~
      "*/" ~
      "JNIEXPORT" ~ ident ~ "JNICALL" ~ ident ~ "(" ~ arguments ~ ");" ^^ {
        case _ ~ _ ~ _ ~ _ ~ methodName ~ _ ~ _ ~ _ ~ _ ~ returnType ~ _ ~ functionName ~ _ ~ args ~ _ => Declaration(returnType, methodName, functionName, args)
      }
  }
  def arguments: Parser[List[Argument]] = rep1sep(argument, ",")
  def argument: Parser[Argument] = ident ~ opt("*") ^^ {
    case typeName ~ Some(_) => Argument(typeName + " *")
    case typeName ~ None    => Argument(typeName)
  }
  def ident: Parser[String] = "\\w+".r

  def argumentStub(arg: Argument, pos: Int) = s"${arg.typeName} arg$pos"
  def makeStub(sig: Declaration) = {
    val args = sig.arguments.zipWithIndex.map {
      case (Argument(tp), i) => s"$tp arg$i"
    } mkString (", ")
    val callArgs = (2 until sig.arguments.length).map {
      case i if sig.arguments(i).typeName == "jstring" =>
        s"(*arg0)->GetStringUTFChars(arg0, arg$i, NULL)"
      case i => s"arg$i"
    }.mkString(", ")
    val callArgsWithParens = if (callArgs.nonEmpty) s"($callArgs)" else ""
    s"""JNIEXPORT ${sig.returnType} JNICALL ${sig.functionName}($args) {
       |  globalenv = arg0;
       |  return ${sig.methodName}$callArgsWithParens;
       |}""".stripMargin
  }

  def makeSource(headers: String, headerName: String): String = {
    val Success(source, _) = parseAll(preamble ~ declarations ~ postamble, headers).map {
      case _ ~ sigs ~ _ => sigs.map(makeStub(_)).mkString("\n")
    }
    s"""#include "$headerName"
       |#include <stdlib.h>
       |#include <string.h>
       |#include <glib.h>
       |#include "CLangNative.h"
       |$source""".stripMargin
  }

  def main(args: Array[String]) = {
    require(args.length == 1)
    require(args.head.endsWith(".h"))
    val headerFile = args.head
    val sourceFile = headerFile.take(headerFile.lastIndexOfSlice(".h")) + ".c"

    val source = makeSource(Source.fromFile(headerFile).mkString, headerFile)
    val outputStream = new PrintStream(new FileOutputStream(sourceFile))
    outputStream.print(source)
    outputStream.close()
  }
}
