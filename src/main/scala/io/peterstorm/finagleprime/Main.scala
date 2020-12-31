package finagleprime

import cats.effect.{ConcurrentEffect, ExitCode, IO, IOApp, Timer}
import fs2.Stream
import com.twitter.finagle.Thrift

import finagleprime.delivery.Server
import finagleprime.delivery.ThriftServer
import finagleprime.delivery.ThriftClient

object Main extends IOApp {

  private val program: Stream[IO, Unit] =
    for {
      client <- Stream.resource(ThriftClient.create[IO])
      module = new Module[IO](client)
      _ <- Stream.resource(Server.create(module.httpApp))
      _ <- Stream.resource(ThriftServer.create[IO])
      _ <- Stream.eval(IO(println("Server started")))
      _ <- Stream.never[IO].covaryOutput[Unit]
    } yield ()

  def run(args: List[String]): IO[ExitCode] =
    program.compile.drain.as(ExitCode.Success)

}
