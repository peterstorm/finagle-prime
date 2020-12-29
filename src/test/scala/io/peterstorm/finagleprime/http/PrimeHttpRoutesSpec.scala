package finagleprime.http

import cats.effect.{IO, IOApp, ExitCode}
import cats.syntax.all._
import cats.kernel.Monoid
import com.olegpy.meow.hierarchy._
import org.http4s.{HttpRoutes, Request, Status, Uri}
import org.scalatest._
import matchers._
import org.scalatest.flatspec.AnyFlatSpec
import fs2._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalacheck.Gen
import org.scalacheck.Shrink.shrinkAny
import finagleprime.IOAssertion
import finagleprime.http.ResponseBodyUtils._
import finagleprime.test.service.TestPrimeService
import finagleprime.endpoints.PrimeEndpoints
import finagleprime.effects.{PrimeHttpErrorHandler, HttpErrorHandler}
import finagleprime.domain.Errors.PrimeError
import finagleprime.delivery.ThriftServer

class PrimeEndpointsSpec extends AnyFlatSpec with ScalaCheckPropertyChecks with should.Matchers with IOApp {

  def run(args: List[String]): IO[ExitCode] = IO.unit.as(ExitCode.Success)

  def routeHandler(implicit RH: HttpErrorHandler[IO, PrimeError]): HttpRoutes[IO] =
    RH.handle(httpRoutes)

  def funcRouteHandler(implicit RH: HttpErrorHandler[IO, PrimeError]): HttpRoutes[IO] =
    RH.handle(functionalHttpRoutes)

  val service = TestPrimeService.service

  val functionalService = TestPrimeService.functionalService

  val httpRoutes: HttpRoutes[IO] = new PrimeEndpoints(service).endpointsNoMS

  val functionalHttpRoutes: HttpRoutes[IO] = new PrimeEndpoints(functionalService).endpointsThriftFunctional

  val routes = routeHandler(PrimeHttpErrorHandler[IO])

  val functionalRoutes = funcRouteHandler(PrimeHttpErrorHandler[IO])

  val ints = Gen.chooseNum(1,1000)
  val negativeInts = Gen.chooseNum(-1000, 0)

  "/prime/n" should "return status code 200 for all integers > 0" in {
    forAll (ints) { (n: Int) => 
      IOAssertion {
        val request = Request[IO](uri = Uri(path = s"/prime/$n"))
        routes(request).value.flatMap { task =>
          task.fold(IO(fail("Empty response")) *> IO.unit) { response =>
            IO(response.status should be (Status.Ok))
          }
        }
      }
    }
  }


  "/prime/n" should "return status code 400 for all integers < 1" in {
    forAll (negativeInts) { (n: Int) => 
      IOAssertion {
        val request = Request[IO](uri = Uri(path = s"/prime/$n"))
        routes(request).value.flatMap { task =>
          task.fold(IO(fail("Empty response")) *> IO.unit) { response =>
            IO(response.status should be (Status.BadRequest))
          }
        }
      }
    }
  }

  "/primethriftlistfunctional/n" should "return the same primes as just calling the pure service" in {
    forAll (ints) { (n: Int) =>
      IOAssertion {
        val request = Request[IO](uri = Uri(path = s"/primethriftlistfunctional/$n"))
        functionalRoutes(request).value.flatMap { task =>
          task.fold(IO(fail("failed")) *> IO.unit) { response =>
            IO(response.body.through(text.utf8Decode).compile.toList.map(l => l.foldLeft(Monoid[String].empty)(Monoid[String].combine)).map(l => l.filterNot(c => c.equals(','))).unsafeRunSync() should be (service.calculatePrimeList(n).unsafeRunSync().compile.toList.map(l => l.map(_.toString)).map(l => l.foldLeft(Monoid[String].empty)(Monoid[String].combine)).unsafeRunSync()))
          }
        }
      }
    }
  }

  def calculatePrimes(int: scala.Int): List[Int] = {
    List.range(0, int + 1)
      .filter(isPrime(_))
  }

  def isPrime(i: scala.Int): Boolean = {
    if (i <= 1) false
    else if (i == 2) true
    else !(2 until i).exists(n => i % n == 0)
  }


  "Calling the pure service"  should "return the same list as calling the pure function behind the service" in IOAssertion {
    val result = service.calculatePrimeList(10).unsafeRunSync().compile.toList.unsafeRunSync()
    IO(result should be (calculatePrimes(10)))
  }



}

