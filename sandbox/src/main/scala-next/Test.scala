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

    val ary: {sz} Array[A] = new {sz} Array[A](10)
    println(s"[basicTest] ary.length = ${ary.length}")
    
    ary(0) = new A("def") // arraystore
    val a0: A = ary(0) // should it be {sz} A?

    val ary2 = Array("abc", "def")
    println(s"[basicTest] ary2.length = ${ary2.length}")

    class Point(var x: Int, var y: Int){
      override def toString: String = s"($x, $y)"
    }

    val point: {*} Point = new Point(2, 3)
    val x: Int = point.x // should it be {sz} Int?
  }

  // def nestedTest(): Unit = {
  //   class A (v: Int) {
  //     override def toString(): String = s"A{$v}"
  //   }

  //   class B (v: Int, sz: {*} SafeZone) {
  //     val a = new {sz} A(v)
  //     override def toString(): String = s"B{$v, $a}"
  //   }
    
  //   SafeZone { implicit sz => 
  //     val b = new B(0, sz)
  //     println(s"[nestedTest] b = $b")
  //   }
  // }

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

    // nestedTest()

    println(">>> Test Safe Zone >>>\n")
  }
}
