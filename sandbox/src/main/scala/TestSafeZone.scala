import scala.scalanative.safe._

object TestSafeZone {
  def test(): Unit = {
    println("\n<<< Test Safe Zone <<<")

    SafeZone { implicit sz =>
      val x: Int = alloc[Int]()
      println(s"x = $x")
    }

    println(">>> Test Safe Zone >>>\n")
  }
}
