package scala.scalanative.safe

import language.experimental.captureChecking

import scalanative.unsigned._
import scala.annotation.Annotation
import scala.annotation.implicitNotFound
import scala.scalanative.runtime.{RawPtr, CMemoryPool, CMemoryPoolZone}

class SafeZoneHandle(handle: RawPtr) extends Annotation {}

/** SafeZone can not be a trait because method `alloc` should be an inline
 *  function (it calls Intrinsics functions).
 *  ```
 *  Expected usage:
 *     val sz = SafeZone.open()
 *   	1. T is struct, e.g. @struct class A(v0: Int, v1: Long) {}
 *   		val x: A = new (sz) A (0, 0L)
 *   	3. T is class. e.g. class A(v0: Int, v1: Long) {}
 *   		val x: A = new (sz) A (0, 0L)
 *   		Note that Array[_] is a special class.
 *   		val x: Array[Int] = new (sz) Array[Int](10) // x.length = 10
 *  ```
 */
@implicitNotFound("Given method requires an implicit zone.")
trait SafeZone {

  /** Frees allocations. This zone allocator is not reusable once closed. */
  def close(): Unit

  /** Return this zone allocator is open or not. */
  def isOpen: Boolean

  /** Return this zone allocator is closed or not. */
  def isClosed: Boolean

  /** Return the handle of this zone. */
  def handle: RawPtr
}

object SafeZone {

  /** Run given function with a fresh zone and destroy it afterwards. */
  final def apply[T](f: ({*} SafeZone) => T): T = {
    val safeZone = open()
    try f(safeZone)
    finally safeZone.close()
  }

  final def open(): {*} SafeZone = new MemoryPoolSafeZone(
    CMemoryPoolZone.open(CMemoryPool.defaultMemoryPoolHandle)
  )
}

final class MemoryPoolSafeZone(private[this] val zoneHandle: RawPtr)
    extends SafeZone {

  private[this] var flagIsOpen = true

  protected def checkOpen(): Unit = {
    if (!isOpen)
      throw new IllegalStateException(s"Zone ${this} is already closed.")
  }

  override def close(): Unit = {
    checkOpen()
    flagIsOpen = false
    CMemoryPoolZone.close(zoneHandle)
    CMemoryPoolZone.free(zoneHandle)
  }

  override def isOpen: Boolean = flagIsOpen

  override def isClosed: Boolean = !isOpen

  override def handle: RawPtr = zoneHandle
}
