package finagleprime

import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Timer}
import fs2.Stream

import finagleprime.delivery.Server
import finagleprime.endpoints.PrimeEndpoints
import finagleprime.services.PrimeService
import finagleprime.interpreters.NoMicroserviceInterpreter

object Main extends IOApp {
  val ctx = new Module[IO, Int]

  private val program: Stream[IO, Unit] =
    for {
      server <- Stream.resource(Server.create(ctx.httpApp))
      _ <- Stream.eval(IO(println("Server started")))
      _ <- Stream.never[IO].covaryOutput[Unit]
    } yield ()

  def run(args: List[String]): IO[ExitCode] = program.compile.drain.as(ExitCode.Success)

}


