package finagleprime.endpoints

import org.http4s._
import org.http4s.dsl._
import org.http4s.circe._
import org.http4s.headers._
import fs2._
import finagleprime.services.PrimeService
import cats.implicits._
import cats.Applicative
import cats.Defer

final class PrimeEndpoints[F[_]: Defer: Applicative, A](service: PrimeService[F, A]) extends Http4sDsl[F] {

  val endpoints: HttpRoutes[F] = HttpRoutes.of[F] {

    case GET -> Root / "prime" / IntVar(i) =>
      if (i < 1) BadRequest(s"Number $i must be 1 or higher")
      else Ok(stream(i))

    case GET -> Root / "primebool" / IntVar(i) =>
      if (i < 1) BadRequest(s"Number $i must be 1 or higher")
      else Ok(streamBool(i))

    case GET -> Root / "primepar" / IntVar(i) =>
      if (i < 1) BadRequest(s"Number $i must be 1 or higher")
      else Ok(streamPar(i))

  }


  def stream(i: Int): Stream[F, Byte] =
    service
      .calculatePrimes(i)
      .map(_.toString)
      .filterNot(s => s.equals("0"))
      .intersperse("\n")
      .through(text.utf8Encode)

  def streamBool(i: Int): Stream[F, Byte] =
    service
      .calculatePrimeBool(i)
      .map(_.toString)
      .intersperse("\n")
      .through(text.utf8Encode)

  def streamPar(i: Int): Stream[F, Byte] =
    service
      .calculatePrimePar(i, 3)
      .map(_.toString)
      .intersperse("\n")
      .through(text.utf8Encode)

}
