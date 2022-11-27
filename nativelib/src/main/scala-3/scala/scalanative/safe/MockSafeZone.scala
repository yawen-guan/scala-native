package scala.scalanative.safe

import scala.scalanative.unsafe._

final class MockSafeZone(zone: Zone) extends SafeZone {

  def alloc[T]()(using tag: Tag[T]): T = {
    val ptr: Ptr[T] = scala.scalanative.unsafe.alloc[T](1)(using tag, zone)
    !ptr
  }

  def close(): Unit = {}

  def isClosed: Boolean = false
}

object MockSafeZone {
  def open(): MockSafeZone = new MockSafeZone(Zone.open())
}
