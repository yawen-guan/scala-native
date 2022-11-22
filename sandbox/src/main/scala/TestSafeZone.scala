import scala.scalanative.safe._
import scala.scalanative.runtime.{Intrinsics, RawPtr}

object TestSafeZone {

  def testPrimitives(): Unit = {}

  def testClass(): Unit = {}

  def testClassWithoutConstructorParams(sz: SafeZone): Unit = {

    /** Basic */
    class A {
      var v0: Int = 0
      var v1: Long = 0L
      def increaseV0: Unit = v0 += 1
      def increaseV1: Unit = v1 += 1
      override def toString(): String = s"{$v0, $v1}"
    }

    val a = sz.alloc[A]()
    println(s"a = $a")
    a.increaseV0
    println(s"a = $a")
    a.increaseV1
    println(s"a = $a")

  }

  // def testClassWithConstructorParams(sz: SafeZone): Unit = {

  //   /** Basic */
  //   class A(var v0: Int = 0, var v1: Long = 0L) {
  //     def increaseV0: Unit = v0 += 1
  //     def increaseV1: Unit = v1 += 1
  //     override def toString(): String = s"{$v0, $v1}"
  //   }

  //   val a = sz.alloc[A](0, 0L)
  //   println(s"a = $a")
  //   a.increaseV0
  //   println(s"a = $a")

  //   /** Class with Ref Field */
  //   class A1(sz: SafeZone) {
  //     val v0: A = sz.alloc[A]()
  //     override def toString(): String = s"{$v0}"
  //   }

  //   val a1 = sz.alloc[A1]()
  //   println(s"a1 = $a1")
  // }

  def test(): Unit = {
    println("\n<<< Test Safe Zone <<<")

    // SafeZone { implicit sz =>
    //   val x: Int = alloc[Int]()
    //   println(s"x = $x")
    // }

    val sz: SafeZone = SafeZone.open()
    println(s"sz.isOpen = ${sz.isOpen}")
    println(s"sz.isClosed = ${sz.isClosed}")

    testClassWithoutConstructorParams(sz)

    sz.close()
    println(s"sz.isOpen = ${sz.isOpen}")
    println(s"sz.isClosed = ${sz.isClosed}")
    // sz.close() // report "Zone is already closed."

    println(">>> Test Safe Zone >>>\n")
  }
}
