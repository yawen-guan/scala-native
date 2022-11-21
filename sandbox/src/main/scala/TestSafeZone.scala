import scala.scalanative.safe._

object TestSafeZone {
  def test(): Unit = {
    println("\n<<< Test Safe Zone <<<")

    class A(var v0: Int = 0, var v1: Long = 0L) {
      def increaseV0: Unit = v0 += 1
      def increaseV1: Unit = v1 += 1
      override def toString(): String = s"{$v0, $v1}"
    }

    // SafeZone { implicit sz =>
    //   val x: Int = alloc[Int]()
    //   println(s"x = $x")
    // }

    val sz: SafeZone = SafeZone.open()
    println(s"sz.isOpen = ${sz.isOpen}")
    println(s"sz.isClosed = ${sz.isClosed}")

    sz.close()

    println(s"sz.isOpen = ${sz.isOpen}")
    println(s"sz.isClosed = ${sz.isClosed}")
    // sz.close() // report "Zone is already closed."

    println(">>> Test Safe Zone >>>\n")
  }
}
