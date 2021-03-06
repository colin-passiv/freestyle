package freestyle.http

import org.scalatest.{AsyncWordSpec, Matchers}

import cats.Id
import com.twitter.util.Future
import io.catbird.util._
import io.finch.{stringToMatcher => _, _}
import shapeless.::

import freestyle._
import freestyle.implicits._
import freestyle.http.finch._

class FinchTests extends AsyncWordSpec with Matchers {

  "Finch Freestyle integration" should {
    import algebra._

    def sumOk(a: Int, b: Int): FreeS[Calc.Op, Output[Int]] =
      sumParOk(a, b)

    def sumParOk(a: Int, b: Int): FreeS.Par[Calc.Op, Output[Int]] =
      Calc[Calc.Op].sum(a, b).map(Ok(_))

    "allow a FreeS program to be used with Finch given a Future handler" in {
      import futureHandlers._

      val endpoint0 = get(/) { sumOk(1, 2) }
      val endpoint1 = post(int) { i: Int =>
        sumOk(1, i)
      }
      val endpoint2 = post(int :: int) { (a: Int, b: Int) =>
        sumOk(a, b)
      }

      endpoint0(Input.get("/")).awaitValueUnsafe() shouldBe Some(3)
      endpoint1(Input.post("/2")).awaitValueUnsafe() shouldBe Some(3)
      endpoint2(Input.post("/1/2")).awaitValueUnsafe() shouldBe Some(3)
    }

    "allow a FreeS.Par program to be used with Finch given a Future handler" in {
      import futureHandlers._

      val endpoint0 = get(/) { sumParOk(1, 2) }
      val endpoint1 = post(int) { i: Int =>
        sumParOk(1, i)
      }
      val endpoint2 = post(int :: int) { (a: Int, b: Int) =>
        sumParOk(a, b)
      }

      endpoint0(Input.get("/")).awaitValueUnsafe() shouldBe Some(3)
      endpoint1(Input.post("/2")).awaitValueUnsafe() shouldBe Some(3)
      endpoint2(Input.post("/1/2")).awaitValueUnsafe() shouldBe Some(3)
    }

    "allow a FreeS program to be used with Finch given an Id handler" in {
      import idHandlers._

      val endpoint0 = get(/) { sumOk(1, 2) }
      val endpoint1 = post(int) { i: Int =>
        sumOk(1, i)
      }
      val endpoint2 = post(int :: int) { (a: Int, b: Int) =>
        sumOk(a, b)
      }

      endpoint0(Input.get("/")).awaitValueUnsafe() shouldBe Some(3)
      endpoint1(Input.post("/2")).awaitValueUnsafe() shouldBe Some(3)
      endpoint2(Input.post("/1/2")).awaitValueUnsafe() shouldBe Some(3)
    }

    "allow a FreeS.Par program to be used with Finch given an Id handler" in {
      import idHandlers._

      val endpoint0 = get(/) { sumParOk(1, 2) }
      val endpoint1 = post(int) { i: Int =>
        sumParOk(1, i)
      }
      val endpoint2 = post(int :: int) { (a: Int, b: Int) =>
        sumParOk(a, b)
      }

      endpoint0(Input.get("/")).awaitValueUnsafe() shouldBe Some(3)
      endpoint1(Input.post("/2")).awaitValueUnsafe() shouldBe Some(3)
      endpoint2(Input.post("/1/2")).awaitValueUnsafe() shouldBe Some(3)
    }

  }
}

object algebra {
  @free
  trait Calc[F[_]] {
    def sum(a: Int, b: Int): FreeS.Par[F, Int]
  }
}

object futureHandlers {
  import algebra._

  implicit val calcFutureHandler: Calc.Handler[Future] =
    new Calc.Handler[Future] {
      def sum(a: Int, b: Int): Future[Int] = Future.value(a + b)
    }
}

object idHandlers {
  import algebra._

  implicit val calcIdHandler: Calc.Handler[Id] =
    new Calc.Handler[Id] {
      def sum(a: Int, b: Int): Id[Int] = a + b
    }
}
