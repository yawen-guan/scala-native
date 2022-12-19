import scala.language.experimental.captureChecking

@main def Test() =
  println("Hello Scala Next!")
  SafeZoneTests.run()

import scala.scalanative.safe._
import scala.scalanative.runtime.RawPtr

object SafeZoneTests {

  def basicTest(sz: {*} SafeZone): Unit = {
    class A (v: String) {
      override def toString(): String = s"{$v}"
    }
    val a = new {sz} A ("abc")
    println(s"[basicTest] a = $a")

    val ary = new {sz} Array[A](10)
    println(s"[basicTest] ary.length = ${ary.length}")
  }

  def run(): Unit = {
    println("\n<<< Test Safe Zone <<<")

    val sz: {*} SafeZone = SafeZone.open()
    println(s"sz.isOpen = ${sz.isOpen}")
    println(s"sz.isClosed = ${sz.isClosed}")

    basicTest(sz)

    sz.close()
    println(s"sz.isOpen = ${sz.isOpen}")
    println(s"sz.isClosed = ${sz.isClosed}")
    // sz.close() // report "Zone is already closed."

    println(">>> Test Safe Zone >>>\n")
  }
}
