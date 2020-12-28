package finagleprime.endpoints

import org.http4s._
import org.http4s.dsl._
import org.http4s.headers._
import fs2._
import cats.implicits._
import cats.Applicative
import cats.effect.{Sync, Concurrent}
import finagleprime.services.PrimeService
import finagleprime.domain.Errors._

final class PrimeEndpoints[F[_]: Concurrent: Sync, A](service: PrimeService[F, A]) extends Http4sDsl[F] {

  val endpointsNoMS: HttpRoutes[F] = HttpRoutes.of[F] {

    case GET -> Root / "prime" / IntVar(i) =>
      Ok(stream(i))

    case GET -> Root / "primebool" / IntVar(i) =>
      Ok(streamBool(i))

    case GET -> Root / "primelist" / IntVar(i) =>
      Ok(streamList(i))

    case GET -> Root / "primepar" / IntVar(i) =>
      Ok(streamPar(i))

    case GET -> Root / "primepar2" / IntVar(i) =>
      Ok(streamPar2(i))
  }

  val endpointsThriftNaive: HttpRoutes[F] = HttpRoutes.of[F] {

    case GET -> Root / "primethrift" / IntVar(i) =>
      Ok(streamBool(i))

    case GET -> Root / "primethriftlist" / IntVar(i) =>
      Ok(streamList(i))

  }

 val endpointsThriftFunctional: HttpRoutes[F] = HttpRoutes.of[F] {

    case GET -> Root / "primethriftfunctional" / IntVar(i) =>
      Ok(streamBool(i))

    case GET -> Root / "primethriftlistfunctional" / IntVar(i) =>
      Ok(streamList(i))

    case GET -> Root / "primethriftparfunctional" / IntVar(i) =>
      Ok(streamPar(i))

    case GET -> Root / "primethriftpar2functional" / IntVar(i) =>
      Ok(streamPar2(i))

    case GET -> Root / "test" =>
      Ok(service.test)

    case GET -> Root / "test2" / IntVar(i) =>
      Ok(service.test2(i))

    case GET -> Root / "test3" / IntVar(i) =>
      Ok(streamTest3(i))

    case GET -> Root / "primefirst" / IntVar(i) =>
      Ok(streamFirst(i))
  }

  def streamTest3(i: Int): Stream[F, Byte] =
    Stream
      .evals(service.test3(i))
      .map(_.toString)
      .intersperse("\n")
      .through(text.utf8Encode)

  def streamFirst(i: Int): Stream[F, Byte] =
    service
      .calculateFirstPrime(i)
      .map(_.toString)
      .through(text.utf8Encode)

  def stream(i: Int): Stream[F, Byte] =
    service
      .calculatePrimes(i)
      .map(_.toString)
      .filterNot(s => s.equals("0"))
      .intersperse("\n")
      .through(text.utf8Encode)

  def streamBool(i: Int): Stream[F, Byte] =
    service.calculatePrimeBool(i)
      .map(_.toString)
      .intersperse("\n")
      .through(text.utf8Encode)

  def streamList(i: Int): Stream[F, Byte] =
    service.calculatePrimeList(i)
      .map(_.toString)
      .intersperse("\n")
      .through(text.utf8Encode)

  def streamPar(i: Int): Stream[F, Byte] =
    service
      .calculatePrimePar(i, 20)
      .map(_.toString)
      .intersperse("\n")
      .through(text.utf8Encode)

  def streamPar2(i: Int): Stream[F, Byte] =
    service
      .calculatePrimePar2(i, 3)
      .map(_.toString)
      .intersperse("\n")
      .through(text.utf8Encode)


}
