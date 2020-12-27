package finagleprime

import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Timer}
import fs2.Stream

import finagleprime.delivery.Server
import finagleprime.endpoints.PrimeEndpoints
import finagleprime.services.PrimeService
import com.olegpy.meow.hierarchy._
import finagleprime.interpreters.NoMicroserviceInterpreter
import finagleprime.effects.ThriftClientBuilder
import com.twitter.finagle.Thrift
import finagleprime.thrift.ThriftPrimeService

object Main extends IOApp {
  val ctx = new Module[IO]

  private val program: Stream[IO, Unit] =
    for {
      _ <- Stream.resource(Server.create(ctx.httpApp))
      _ <- Stream.eval(ctx.thriftServer)
      _ <- Stream.eval(IO(println("Server started")))
      _ <- Stream.never[IO].covaryOutput[Unit]
    } yield ()

  def run(args: List[String]): IO[ExitCode] = program.compile.drain.as(ExitCode.Success)

}


