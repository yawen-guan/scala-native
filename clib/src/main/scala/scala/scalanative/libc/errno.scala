package scala.scalanative
package libc

import scalanative.unsafe._

@extern object errno extends errno

@extern trait errno {
  @name("scalanative_errno")
  def errno: CInt = extern
  @name("scalanative_set_errno")
  def errno_=(value: CInt): Unit = extern
  @name("scalanative_edom")
  def EDOM: CInt = extern
  @name("scalanative_eilseq")
  def EILSEQ: CInt = extern
  @name("scalanative_erange")
  def ERANGE: CInt = extern
}
