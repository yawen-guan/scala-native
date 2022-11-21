import scala.scalanative.safe.SafeZone
import scala.scalanative.runtime.RawPtr

object Test {
  def main(args: Array[String]): Unit = {
    println("Hello, World!")
    TestNew.test()
    TestSafeZone.test()
  }
}
