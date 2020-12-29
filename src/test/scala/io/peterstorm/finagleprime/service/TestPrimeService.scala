package finagleprime.test.service

import cats.effect.{IO, IOApp, ExitCode}
import cats.implicits._
import cats.syntax.all._
import finagleprime.algebras.PrimeAlgebra
import finagleprime.services.PrimeService
import finagleprime.interpreters.NoMicroserviceInterpreter
import finagleprime.interpreters.ThriftMicroServiceInterpreter

object TestPrimeService extends IOApp {

  val service: PrimeService[IO, Int] = new PrimeService(new NoMicroserviceInterpreter)

  val functionalService: PrimeService[IO, Int] = new PrimeService(new ThriftMicroServiceInterpreter)

    def run(args: List[String]): IO[ExitCode] = IO.unit.as(ExitCode.Success)
}
