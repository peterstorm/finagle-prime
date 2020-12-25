package finagleprime.services

import fs2._
import fs2.concurrent.Queue
import cats.Functor
import cats.implicits._
import cats.effect.Concurrent

import finagleprime.algebras.PrimeAlgebra

class PrimeService[F[_]: Concurrent, A](algebra: PrimeAlgebra[F, A]) {

  def calculatePrimes(i: Int): Stream[F, A] =
    Stream.range(0, i + 1)
      .evalMap(algebra.calculatePrime(_))

  def calculatePrimeBool(i: Int): Stream[F, Int] = 
    Stream.range(0, i + 1)
      .evalFilter(algebra.calculateIsPrime(_))

  def calculatePrimeList(i: Int): Stream[F, Int] =
    Stream.evals(algebra.calculatePrimes(i))

  def calculatePrimePar(i: Int, numShards: Int): Stream[F, Int] =
    Stream.range(0, i + 1).covary[F].evalFilterAsync(100)(n => algebra.calculateIsPrime(n))

  def calculatePrimePar2(i: Int, numShards: Int): Stream[F, Int] =
    Stream
      .eval(Queue.noneTerminated[F, Int]) // unbounded for simplicity
      .repeatN(numShards.toLong)
      .foldMap(Vector(_))
      .flatMap { shards =>
        val close = shards.traverse_(_.enqueue1(None))

        val writer = Stream.range(0, i + 1)
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
