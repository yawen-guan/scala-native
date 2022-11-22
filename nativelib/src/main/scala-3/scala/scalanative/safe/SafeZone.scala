package scala.scalanative.safe

import scalanative.unsigned._
import scala.annotation.implicitNotFound
import scala.scalanative.unsafe.Tag
import scala.scalanative.runtime.{
  Intrinsics,
  RawPtr,
  CMemoryPool,
  CMemoryPoolZone
}

/** SafeZone can not be a trait because method `alloc` should be an inline
 *  function (it calls Intrinsics functions).
 */
@implicitNotFound("Given method requires an implicit zone.")
class SafeZone private (private[this] val zoneHandle: RawPtr) {

  private[this] var flagIsOpen = true

  /** Return an object of type T allocated in zone. T can be primitive types,
   *  struct, class.
   *
   *  ```
   *  Expected usage:
   *  	1. T is primitive type, e.g. Int
   *  		val x: Int = sz.alloc[Int](10)
   *  	2. T is struct, e.g. @struct class A(v0: Int, v1: Long) {}
   *  		val x: A = sz.alloc[A](0, 0L)
   *  	3. T is class. e.g. class A(v0: Int, v1: Long) {}
   *  		val x: A = sz.alloc[A](0, 0L)
   *  		Note that Array[_] is a special class.
   *  		val x: Array[Int] = sz.alloc[Array[Int]](10) // x.length = 10
   *  ```
   *  Currently it's a mock interface which doesn't accept constructor
   *  parameters.
   */
  inline def alloc[T](): T = {
    checkOpen()
    Intrinsics.zonealloc[T](zoneHandle).asInstanceOf[T]
  }

  /** Frees allocations. This zone allocator is not reusable once closed. */
  def close(): Unit = {
    checkOpen()
    flagIsOpen = false
    CMemoryPoolZone.close(zoneHandle)
    CMemoryPoolZone.free(zoneHandle)
  }

  /** Return this zone allocator is open or not. */
  def isOpen: Boolean = flagIsOpen

  /** Return this zone allocator is closed or not. */
  def isClosed: Boolean = !isOpen

  protected def checkOpen(): Unit = {
    if (!isOpen)
      throw new IllegalStateException(s"Zone ${this} is already closed.")
  }
}

object SafeZone {

  /** Run given function with a fresh zone and destroy it afterwards. */
  final def apply[T](f: SafeZone => T): T = {
    val safeZone = open()
    try f(safeZone)
    finally safeZone.close()
  }

  // TODO: free the default memory pool.
  final def open(): SafeZone = new SafeZone(
    CMemoryPoolZone.open(CMemoryPool.defaultMemoryPoolHandle)
  )

}
