package scala.scalanative

import scala.reflect.internal.util.{BatchSourceFile, NoFile, SourceFile}
import scala.reflect.internal.util.Position
import scala.scalanative.compat.ParserCompat.parser
import scala.tools.nsc.{CompilerCommand, Global, Settings}
import scala.tools.nsc.io.AbstractFile
import java.nio.file.{Files, Path}
import java.io.File
import scala.scalanative.compiler.CompatReporter

/** Helper class to compile snippets of code.
 */
class NIRCompiler(outputDir: Path) extends api.NIRCompiler {

  def this() = this(Files.createTempDirectory("scala-native-target"))

  override def compile(code: String): Array[Path] = {
    val source = new BatchSourceFile(NoFile, code)
    compile(Seq(source)).toArray
  }

  override def compile(base: Path): Array[Path] = {
    val sources = getFiles(base.toFile, _.getName endsWith ".scala")
    val sourceFiles = sources map { s =>
      val abstractFile = AbstractFile.getFile(s)
      new BatchSourceFile(abstractFile)
    }
    compile(sourceFiles).toArray
  }

  private def compile(sources: Seq[SourceFile]): Seq[Path] = {
    val global = getCompiler(options = ScalaNative)
    import global._
    val run = new Run
    run.compileSources(sources.toList)
    getFiles(outputDir.toFile, _ => true).map(_.toPath)
  }

  /** List of the files contained in `base` that sastisfy `filter`
   */
  private def getFiles(base: File, filter: File => Boolean): Seq[File] =
    (if (filter(base)) Seq(base) else Seq()) ++
      (Option(base.listFiles()) getOrElse Array.empty flatMap (getFiles(
        _,
        filter
      )))

  private def reportError(error: String) =
    throw new api.CompilationFailedException(error)

  /** Reporter that ignores INFOs and WARNINGs, but directly aborts the
   *  compilation on ERRORs.
   */
  private class TestReporter(override val settings: Settings)
      extends CompatReporter {
    override def add(pos: Position, msg: String, severity: Severity): Unit =
      severity match {
        case ERROR => reportError(msg)
        case _     => ()
      }
  }

  /** Represents a basic compiler option (the string given to the command line
   *  invocation of scalac)
   */
  private implicit class CompilerOption(s: String) {
    override def toString: String = s
  }

  /** An option to add a compiler plugin
   */
  private class CompilerPlugin(val jarPath: String, val classpath: List[String])
      extends CompilerOption(
        s"-Xplugin:$jarPath" + (if (classpath.nonEmpty)
                                  classpath
                                    .mkString(" -cp ", File.pathSeparator, "")
                                else "")
      )

  /** Option to add the scala-native compiler plugin
   */
  private case object ScalaNative
      extends CompilerPlugin(
        jarPath = sys props "scalanative.nscplugin.jar",
        classpath = List(sys props "scalanative.nativeruntime.cp")
      )

  /** Returns an instance of `Global` configured according to the given options.
   */
  private def getCompiler(options: CompilerOption*): Global = {
    // I don't really know how I can reset the compiler after a run, nor what else
    // should also be reset, so for now this method creates new instances of everything,
    // which is not so cool.
    //
    // Also, using `command.settings.outputDirs.setSingleOutput` I get strange classpath problems.
    // What's even stranger, is that everything works fine using `-d`!
    val outPath = outputDir.toAbsolutePath
    val arguments = parser.tokenize(s"-d $outPath " + (options mkString " "))
    val command = new CompilerCommand(arguments.toList, reportError _)
    val reporter = new TestReporter(command.settings)

    new Global(command.settings, reporter)
  }

}
