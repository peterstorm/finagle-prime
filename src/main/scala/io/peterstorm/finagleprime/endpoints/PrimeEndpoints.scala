package finagleprime.endpoints

import org.http4s._
import org.http4s.dsl._
import fs2._
import cats.implicits._
import cats.effect.{Sync, Concurrent}

import finagleprime.services.PrimeService
import finagleprime.domain.Errors._

final class PrimeEndpoints[F[_]: Concurrent: Sync, A](service: PrimeService[F, A]) extends Http4sDsl[F] {

  val endpointsNoMS: HttpRoutes[F] = HttpRoutes.of[F] {

    case GET -> Root / "prime" / IntVar(i) =>
      streamSinglePrime(i) >>= (Ok(_))

    case GET -> Root / "primelist" / IntVar(i) =>
      streamList(i) >>= (Ok(_))

    case GET -> Root / "primepar" / IntVar(i) =>
      streamPar(i) >>= (Ok(_))

    case GET -> Root / "primepar2" / IntVar(i) =>
      Ok(streamPar2(i))
  }

  val endpointsThriftNaive: HttpRoutes[F] = HttpRoutes.of[F] {

    case GET -> Root / "primethrift" / IntVar(i) =>
      streamSinglePrime(i) >>= (Ok(_))

    case GET -> Root / "primethriftlist" / IntVar(i) =>
      streamList(i) >>= (Ok(_))

  }

 val endpointsThriftFunctional: HttpRoutes[F] = HttpRoutes.of[F] {

    case GET -> Root / "primethriftfunctional" / IntVar(i) =>
      streamSinglePrime(i) >>= (Ok(_))

    case GET -> Root / "primethriftlistfunctional" / IntVar(i) =>
      streamList(i) >>= (Ok(_))

    case GET -> Root / "primethriftparfunctional" / IntVar(i) =>
      streamPar(i) >>= (Ok(_))

    case GET -> Root / "primethriftpar2functional" / IntVar(i) =>
      Ok(streamPar2(i))

  }

  def streamSinglePrime(i: Int): F[Stream[F, Byte]] =
    service
      .calculatePrime(i)
      .map(s => s.map(_.toString).intersperse(", ").through(text.utf8Encode))

  def streamList(i: Int): F[Stream[F, Byte]] =
    service
      .calculatePrimeList(i)
      .map(s => s.map(_.toString).intersperse(", ").through(text.utf8Encode))

  def streamPar(i: Int): F[Stream[F, Byte]] =
    service
      .calculatePrimePar(i, 20)
      .map(s => s.map(_.toString).intersperse(", ").through(text.utf8Encode))

  def streamPar2(i: Int): Stream[F, Byte] =
    service
      .calculatePrimePar2(i, 3)
      .map(_.toString)
      .intersperse("\n")
      .through(text.utf8Encode)


}
