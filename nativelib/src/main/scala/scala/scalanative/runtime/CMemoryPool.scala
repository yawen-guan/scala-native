package scala.scalanative.runtime

import scalanative.unsafe._

@extern
object _CMemoryPool {
  @name("memorypool_open")
  def open(): RawPtr = extern

  @name("memorypool_free")
  def free(rawpool: RawPtr): Unit = extern
}

object CMemoryPool {
  lazy val defaultMemoryPoolHandle = _CMemoryPool.open()

  def freeDefaultMemoryPool(): Unit = {
    _CMemoryPool.free(defaultMemoryPoolHandle)
  }
}

@extern
object CMemoryPoolZone {
  @name("memorypoolzone_open")
  def open(rawpool: RawPtr): RawPtr = extern

  @name("memorypoolzone_close")
  def close(rawzone: RawPtr): Unit = extern

  @name("memorypoolzone_free")
  def free(rawzone: RawPtr): Unit = extern
}
