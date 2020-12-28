package finagleprime

import cats.effect.{ConcurrentEffect, ExitCode, IO, IOApp, Timer}
import fs2.Stream
import com.twitter.finagle.Thrift

import finagleprime.delivery.Server
import finagleprime.delivery.ThriftServer

object Main extends IOApp {

  val ctx = new Module[IO]

  private val program: Stream[IO, Unit] =
    for {
      _ <- Stream.resource(Server.create(ctx.httpApp))
      _ <- Stream.resource(ThriftServer.create[IO])
      _ <- Stream.eval(IO(println("Server started")))
      _ <- Stream.never[IO].covaryOutput[Unit]
    } yield ()

  def run(args: List[String]): IO[ExitCode] =
    program.compile.drain.as(ExitCode.Success)

}
