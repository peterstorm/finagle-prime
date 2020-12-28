package finagleprime.services

import fs2._
import fs2.concurrent.Queue
import cats.Functor
import cats.syntax.all._
import cats.effect.{Concurrent, Sync}

import finagleprime.algebras.PrimeAlgebra
import finagleprime.domain.Errors._

class PrimeService[F[_]: Concurrent: Sync, A](algebra: PrimeAlgebra[F, A]) {

  def calculatePrimes(i: Int): F[Stream[F, A]] =
    if (i < 1) Sync[F].raiseError(InvalidArgument(i.toString()))
    else {
      val result = Stream.range(0, i + 1).evalMap(algebra.calculatePrime(_))
      Sync[F].pure(result)
    }

  def calculatePrime(i: Int): F[Stream[F, Int]] = {
    if (i < 1) Sync[F].raiseError(InvalidArgument(i.toString()))
    else {
      val result = Stream.range(1, i + 1).evalFilter(i => algebra.calculateIsPrime(i))
      Sync[F].pure(result)
    }
  }

  def calculatePrimeList(i: Int): F[Stream[F, Int]] = {
    if (i < 1) Sync[F].raiseError(InvalidArgument(i.toString()))
    else {
      val result = Stream.evals(algebra.calculatePrimes(i))
      Sync[F].pure(result)
    }
  }


  def calculatePrimePar(i: Int, numShards: Int): F[Stream[F, Int]] = {
    if (i < 1) Sync[F].raiseError(InvalidArgument(i.toString()))
    else {
      val result = Stream.range(1, i + 1).covary[F].evalFilterAsync(numShards)(n => algebra.calculateIsPrime(n))
      Sync[F].pure(result)
    }
  }


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
