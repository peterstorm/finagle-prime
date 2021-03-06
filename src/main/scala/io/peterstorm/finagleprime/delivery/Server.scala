package finagleprime.delivery

import org.http4s.server.Server
import org.http4s.{HttpApp}
import org.http4s.server.blaze.BlazeServerBuilder
import cats.effect._

object Server {

    def create[F[_]: ConcurrentEffect: Timer](app: HttpApp[F]): Resource[F, Server[F]] =
      BlazeServerBuilder[F]
        .bindHttp(3004, "0.0.0.0")
        .withHttpApp(app)
        .resource
}
