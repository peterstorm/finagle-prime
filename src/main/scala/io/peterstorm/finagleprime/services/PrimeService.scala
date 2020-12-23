package finagleprime.services

import finagleprime.algebras.PrimeAlgebra
import fs2._
import fs2.concurrent.Queue
import cats.Functor
import cats.implicits._
import cats.effect.Concurrent

class PrimeService[F[_]: Functor: Concurrent, A](algebra: PrimeAlgebra[F, A]) {

  def calculatePrimes(i: Int): Stream[F, A] =
    Stream.range(0, i)
      .evalMap(algebra.calculatePrime(_))

  def calculatePrimeBool(i: Int): Stream[F, Int] = 
    Stream.range(0, i)
      .evalFilter(algebra.calculateIsPrime(_))

  def calculatePrimePar(i: Int, numShards: Int): Stream[F, Int] =
    Stream.range(0, i).covary[F].evalFilterAsync(100)(n => algebra.calculateIsPrime(n))

  def test(i: Int, numShards: Int): Stream[F, Int] =
    Stream.eval(Queue.bounded[F, Int](100).replicateA(numShards).map(_.zipWithIndex.map(_.swap).toMap)).flatMap {
      store =>
        Stream.range(0, i).flatMap { n =>
          val queue = store(n % numShards)
          queue.dequeue.evalFilter(i => algebra.calculateIsPrime(i)).concurrently(Stream.eval(queue.enqueue1(n)))
        }
    }


}
