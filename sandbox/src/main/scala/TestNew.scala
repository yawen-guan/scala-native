import scala.scalanative.runtime.struct

/** Test New Keyword.
 *
 *  _: T = new T(args), T can be Struct or Class type.
 */
object TestNew {

  /** Failed. Called NirGenExpr.genApplyNewStruct.
   *  ```
   *  [error] Found 4 errors on @"M8TestNew$D10testStructuEO" :
   *  [error]   0000  def @"M8TestNew$D10testStructuEO" : (@"T8TestNew$") => unit {
   *  [error]   0001  %2(%1 : @"T8TestNew$"):
   *  [error]   0002    %3 = insert zero[{int, long}], int 0, 0
   *  [error]   0003    %4 = insert %3 : {int, long}, long 0, 1
   *  [error]   0004    %5 = module @"T13scala.Predef$"
   *  [error]   0005    %10 = ieq[@"T16java.lang.Object"] "a0 = ", null
   *  [error]   0006    if %10 : bool then %6 else %7
   *  [error]   0007  %6:
   *  [error]   0008    jump %8("null")
   *  [error]   0009  %7:
   *  [error]   0010    jump %8("a0 = ")
   *  [error]   0011  %8(%9 : @"T16java.lang.String"):
   *  [error]   0012    %15 = ieq[@"T16java.lang.Object"] %4 : {int, long}, null
   *  [error]   0013    if %15 : bool then %11 else %12
   *  [error]   0014  %11:
   *  [error]   0015    jump %13("null")
   *  [error]   0016  %12:
   *  [error]   0017    %16 = method %4 : {int, long}, "D8toStringL16java.lang.StringEO"
   *  [error]   0018    %17 = call[(@"T16java.lang.Object") => @"T16java.lang.String"] %16 : ptr(%4 : {int, long})
   *  [error]   0019    jump %13(%17 : @"T16java.lang.String")
   *  [error]   0020  %13(%14 : @"T16java.lang.String"):
   *  [error]   0021    %18 = call[(@"T16java.lang.String", @"T16java.lang.Object") => @"T16java.lang.String"] @"M16java.lang.StringD6concatL16java.lang.StringL16java.lang.StringEO" : ptr(%9 : @"T16java.lang.String", %14 : @"T16java.lang.String")
   *  [error]   0022    %19 = call[(@"T13scala.Predef$", @"T16java.lang.Object") => unit] @"M13scala.Predef$D7printlnL16java.lang.ObjectuEO" : ptr(%5 : !?@"T13scala.Predef$", %18 : @"T16java.lang.String")
   *  [error]   0023    ret %19 : unit
   *  [error]   0024  }
   *  [error]   in inst #12 :
   *  [error]     expected @"T16java.lang.Object", but got {int, long}
   *  [error]   in inst #17 :
   *  [error]     expected @"T16java.lang.Object", but got {int, long}
   *  [error]   in inst #17 :
   *  [error]     can't resolve method on {int, long}
   *  [error]   in inst #18 / arg #1 :
   *  [error]     expected @"T16java.lang.Object", but got {int, long}
   *  [error] 4 errors found
   *  ```
   */
  // def testStruct(): Unit = {
  //   @struct class A0(v0: Int, v1: Long)
  //   val a0 = new A0(0, 0L)
  //   println(s"a0 = $a0")
  // }

  // def testOthers(): Unit = {
  //   // trait
  //   trait A {}
  //   val a = new A()
  //   /**
  //    *  ```
  //    *  [error] 71 |    val a = new A()
  //    *  [error]    |                ^
  //    *  [error]    |                A is a trait; it cannot be instantiated
  //    *  ```
  //    */

  //   // primitives
  //   new Int()
  //   /**
  //    *  ```
  //    *  [error] 74 |    new Int()
  //    *  [error]    |        ^^^
  //    *  [error]    |        Int is abstract; it cannot be instantiated
  //    *  ```
  //    */
  // }

  def testBasicClass(): Unit = {
    class A(v0: Int = 0, v1: Long = 0) {
      override def toString(): String = s"{$v0, $v1}"
    }

    val a = new A(1, 2L)
    println(s"a = $a")
  }

  /**
   *  ```
   *   classalloc A2
   *   call A2.constructor
   *   -> classalloc A1
   *   	 call A1.constructor
   *   	 -> classalloc A0
   *   			call A0.constructor
   *  ```
   */
  def testClassWithRefField(): Unit = {
    class A0(v0: Int = 0) {
      override def toString(): String = s"{}"
    }
    class A1(v0: A0 = new A0()) {
      override def toString(): String = s"{$v0}"
    }
    class A2(v0: A1 = new A1()) {
      override def toString(): String = s"{$v0}"
    }

    val a2 = new A2()
    println(s"a2 = $a2")
  }

  def testArrayClass(): Unit = {
    val ary0 = new Array[Int](10)
    println(s"ary0.length = ${ary0.length}")

    class A(v0: Int = 0) {}
    val ary1 = new Array[A](10)
    println(s"arg1.length = ${ary1.length}")
  }

  def test(): Unit = {
    println("\n<<< Test New <<<")

    // testStruct()
    // testOthers()

    // Test class
    testBasicClass()
    testClassWithRefField()
    testArrayClass()

    println(">>> Test New >>>\n")
  }
}
