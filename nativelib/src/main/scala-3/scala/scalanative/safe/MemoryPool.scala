// package scala.scalanative.safe

// import scalanative.runtime.{CMemoryPool, CMemoryPoolZone, RawPtr, Intrinsics}

// object MemoryPool {
//   lazy val defaultMemoryPoolHandle = CMemoryPool.open()

//   // TODO: free the default memory pool.
//   def freeDefaultMemoryPool(): Unit = {
//     CMemoryPool.free(defaultMemoryPoolHandle)
//   }
// }

// final class MemoryPoolSafeZone(private[this] val poolHandle: RawPtr)
//     extends SafeZone {

//   private[this] val zoneHandle = CMemoryPoolZone.open(poolHandle)
//   private[this] var flagIsOpen = true

//   private def checkOpen(): Unit =
//     if (!isOpen)
//       throw new IllegalStateException(s"Zone ${this} is already closed.")

//   override inline def alloc[T](): T = {
//     checkOpen()

//     Intrinsics.zonealloc(zoneHandle).asInstanceOf[T]
//   }

//   override def close(): Unit = {
//     checkOpen()

//     flagIsOpen = false
//     CMemoryPoolZone.close(zoneHandle)
//     CMemoryPoolZone.free(zoneHandle)
//   }
//   override def isOpen: Boolean = flagIsOpen
//   override def isClosed: Boolean = !isOpen

//   override def getZoneHandle: RawPtr = zoneHandle
// }

// object MemoryPoolSafeZone {
//   def open(poolHandle: RawPtr): MemoryPoolSafeZone = {
//     new MemoryPoolSafeZone(poolHandle)
//   }
// }
