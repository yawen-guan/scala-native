package scala.scalanative

import language.experimental.captureChecking
import scalanative.unsigned._
import scala.annotation.implicitNotFound
import scala.scalanative.runtime.{RawPtr, CZone}
import scala.scalanative.runtime.SafeZoneAllocator.allocate

@implicitNotFound("Given method requires an implicit zone.")
trait SafeZone {

  /** Return this zone is open or not. */
  def isOpen: Boolean

  /** Return this zone is closed or not. */
  def isClosed: Boolean = !isOpen

  /** Require this zone is open. */
  def checkOpen(): Unit = {
    if (!isOpen)
      throw new IllegalStateException(s"Zone ${this} is already closed.")
  }

  /** Frees allocations. This zone is not reusable once closed. */
  private[scalanative] def close(): Unit

  /** Return the handle of this zone. */
  private[scalanative] def handle: RawPtr

  /** Allocates an object in this zone. The expression of obj must be an instance creation expression. */
  infix inline def alloc[T <: AnyRef](inline obj: T): {this} T = allocate(this, obj)
}

final class MemorySafeZone (private[scalanative] val handle: RawPtr) extends SafeZone {

  private[this] var flagIsOpen = true

  override def close(): Unit = {
    checkOpen()
    flagIsOpen = false
    CZone.close(handle)
  }

  override def isOpen: Boolean = flagIsOpen
}


object SafeZone {
  /** Run given function with a fresh zone and destroy it afterwards. */
  final def apply[T](f: ({*} SafeZone) => T): T = {
    val sz: {*} SafeZone = new MemorySafeZone(CZone.open())
    try f(sz)
    finally sz.close()
  }

  /* Allocates an object in the implicit zone. The expression of obj must be an instance creation expression. */
  inline def alloc[T <: AnyRef](inline obj: T)(using inline sz: {*} SafeZone): {sz} T = allocate(sz, obj)
}
