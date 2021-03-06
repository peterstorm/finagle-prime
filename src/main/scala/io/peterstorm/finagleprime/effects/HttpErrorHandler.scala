package finagleprime.effects

import org.http4s._
import cats.{ApplicativeError, MonadError}
import cats.data.{Kleisli, OptionT}
import org.http4s.dsl.Http4sDsl
import cats.syntax.all._
import cats.implicits._

import finagleprime.domain.Errors._

trait HttpErrorHandler[F[_], E <: Throwable] {

  def handle(routes: HttpRoutes[F]): HttpRoutes[F]

}

object HttpErrorHandler {

  @inline final def apply[F[_], E <: Throwable](implicit ev: HttpErrorHandler[F, E]) = ev

}

abstract class RouteErrorHandler[F[_], E <: Throwable] extends HttpErrorHandler[F, E] with Http4sDsl[F] {

  def AE: ApplicativeError[F, E]
  def handler: E => F[Response[F]]

  def handle(routes: HttpRoutes[F]): HttpRoutes[F] = 
    Kleisli { req =>
      OptionT {
        AE.handleErrorWith(routes.run(req).value)(e => AE.map(handler(e))(Option(_)))
      }
    }
}


object PrimeHttpErrorHandler {

  def apply[F[_]: MonadError[*[_], PrimeError]]: HttpErrorHandler[F, PrimeError] =
    new RouteErrorHandler[F, PrimeError] {
      
      val AE = implicitly

      val handler: PrimeError => F[Response[F]] = {
        case InvalidArgument(arg) => BadRequest(s"Invalid argument: $arg Must be 1 or more")
        case UnknownPrimeError(msg)    => BadRequest(s"Unknown error: $msg")
      }

    }
}
