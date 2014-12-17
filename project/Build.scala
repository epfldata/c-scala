import sbt._
import sbt.classpath.ClasspathUtilities
import Keys._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

object CScalaBuild extends Build {
  def generateNative(classpath: Seq[Attributed[File]], baseDir: File, classDir: File, stream: TaskStreams) = {
    val processLogger = new ProcessLogger {
      var inError = false
      def buffer[T](f: => T) = f
      def error(s: => String) = {
        if (s.toLowerCase.contains("error:")) inError = true
        if (s.toLowerCase.contains("warning:")) inError = false
        if (inError) stream.log.error(s) else ()
      }
      def info(s: => String) = ()
    }
    val jnisrcDir = baseDir / "jnisrc"
    val additionalFiles = Seq(jnisrcDir / "CLangNative.h", jnisrcDir / "CLangNative.c")
    val additionalMod = additionalFiles.map(_.lastModified).max
    val headerFile = jnisrcDir / "Native.h"
    val nativeLibrary = baseDir / "libshallow.jnilib"
    val oldHeaders = if (headerFile.exists) IO.read(headerFile) else ""
    // Generate headers for the native methods
    if (Process(s"javah -jni -o $headerFile ch.epfl.data.cscala.Native$$", Some(classDir)) ! processLogger != 0) throw new Incomplete(None)
    val newHeaders = IO.read(headerFile)

    if (!nativeLibrary.exists
        || oldHeaders != newHeaders
        || additionalMod > nativeLibrary.lastModified) {
      // User HeaderParser to generate the library's implementation
      stream.log.info("Generating native library...")
      val loader: ClassLoader = ClasspathUtilities.toLoader(classpath.map(_.data).map(_.getAbsoluteFile))
      val clazz = loader.loadClass("ch.epfl.data.cscala.generator.HeaderParser")
      val method = clazz.getMethod("main", classOf[Array[String]])
      val args = Array[String](headerFile.toString)
      method.invoke(null, args)

      // Compile the library
      val files = additionalFiles ++ Seq(headerFile, jnisrcDir / "Native.c")
      val glibLibs = Process("pkg-config --cflags glib-2.0 --libs glib-2.0", Some(jnisrcDir)) !! processLogger
      val jniLibs = "-I /System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers -I /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX10.9.sdk/System/Library/Frameworks/JavaVM.framework/Versions/A/Headers "
      stream.log.info("Compiling C code...")
      if (Process(s"clang $glibLibs $jniLibs -Wreturn-type -c ${files.mkString(" ")}", Some(jnisrcDir)) ! processLogger != 0) throw new Incomplete(None)
      stream.log.info("Creating object files...")
      if (Process(s"clang $glibLibs -dynamiclib -o libshallow.jnilib Native.o CLangNative.o", Some(jnisrcDir)) ! processLogger != 0) throw new Incomplete(None)
      IO.delete(List(jnisrcDir / "Native.o", jnisrcDir / "Native.h.gch", jnisrcDir / "Native.c", jnisrcDir / "CLangNative.h.gch", jnisrcDir / "CLangNative.o"))
      IO.move(jnisrcDir / "libshallow.jnilib", nativeLibrary)
    }
  }

  def formattingPreferences = {
    import scalariform.formatter.preferences._
    FormattingPreferences()
    .setPreference(RewriteArrowSymbols, false)
    .setPreference(AlignParameters, true)
    .setPreference(AlignSingleLineCaseStatements, true)
  }

  lazy val formatSettings = SbtScalariform.scalariformSettings ++ Seq(
    ScalariformKeys.preferences in Compile := formattingPreferences,
    ScalariformKeys.preferences in Test    := formattingPreferences
  )

  lazy val defaults = Project.defaultSettings ++ Seq(
    scalaVersion := "2.11.2",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "2.2.0" % "test",
      "org.scala-lang" % "scala-compiler" % "2.11.2" % "optional"
    )
  )

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = defaults
  ) aggregate (c_scala)

  lazy val c_scala = Project(
    id = "c-scala",
    base = file("c-scala"),
    settings = defaults ++ Seq(
      name := "c-scala",
      // Fork in test to avoid crashing sbt if the JNI segfaults
      fork in Test := true,
      compile in Compile <<= (dependencyClasspath in Compile,
                              compile in Compile,
                              classDirectory in Compile,
                              streams,
                              baseDirectory) map { (classpath, analysis, dir, stream, baseDir) =>
        generateNative(classpath, baseDir, dir, stream)
        analysis
      }
    )) dependsOn(generator)

  lazy val generator = Project(
    id = "generator",
    base = file("generator"),
    settings = defaults ++ Seq(
      name := "generator"
    )
  )
}
