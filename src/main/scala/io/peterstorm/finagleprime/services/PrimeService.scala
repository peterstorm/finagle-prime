package finagleprime.services

import fs2._
import fs2.concurrent.Queue
import cats.Functor
import cats.syntax.all._
import cats.effect.{Concurrent, Sync}
import com.olegpy.meow.hierarchy._

import finagleprime.algebras.PrimeAlgebra
import finagleprime.domain.Errors._

class PrimeService[F[_]: Concurrent: Sync, A](algebra: PrimeAlgebra[F, A]) {

  def calculatePrimes(i: Int): Stream[F, A] =
    Stream.range(0, i + 1)
      .evalMap(algebra.calculatePrime(_))

  def calculatePrimeBool(i: Int): Stream[F, Int] =
    Stream.range(1, i + 1)
          .evalFilter(i => algebra.calculateIsPrime(i))

  def test: Stream[F, Unit] =
    Stream.raiseError[F](InvalidArgument("test"))

  def test2(i: Int): F[String] =
    if (i < 1) Sync[F].raiseError(InvalidArgument(i.toString()))
    else Sync[F].pure(1.toString())

  def test3(i: Int): F[List[Int]] =
    if (i < 1) Sync[F].raiseError(InvalidArgument(i.toString()))
    else algebra.calculatePrimes(i)

  def calculateFirstPrime(i: Int): Stream[F, A] =
    Stream.eval(algebra.calculatePrime(i))

  def calculatePrimeList(i: Int): Stream[F, Int] =
     Stream.evals(algebra.calculatePrimes(i))

  def calculatePrimePar(i: Int, numShards: Int): Stream[F, Int] =
    Stream.range(1, i + 1).covary[F].evalFilterAsync(numShards)(n => algebra.calculateIsPrime(n))

  def calculatePrimePar2(i: Int, numShards: Int): Stream[F, Int] =
    Stream
      .eval(Queue.noneTerminated[F, Int])
      .repeatN(numShards.toLong)
      .foldMap(Vector(_))
      .flatMap { shards =>
        val close = shards.traverse_(_.enqueue1(None))
        val writer = Stream.range(1, i + 1)
          .evalMap {
            case v  =>
              shards(v % numShards).enqueue1(v.some)
          }
          .onFinalize(close)
        val readers =
          Stream
            .emits(shards)
            .map(_.dequeue.evalFilter(algebra.calculateIsPrime(_)))
            .parJoinUnbounded
        readers concurrently writer
      }
  }
