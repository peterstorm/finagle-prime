package finagleprime.services

import finagleprime.algebras.PrimeAlgebra
import fs2._
import cats.Functor

class PrimeService[F[_]: Functor, A](algebra: PrimeAlgebra[F, A]) {

  def calculatePrimes(i: Int): Stream[F, A] =
    Stream.range(0, i)
      .evalMap(algebra.calculatePrime(_))

  def calculatePrimeBool(i: Int): Stream[F, Int] = 
    Stream.range(0, i)
      .evalFilter(algebra.calculateIsPrime(_))

}
