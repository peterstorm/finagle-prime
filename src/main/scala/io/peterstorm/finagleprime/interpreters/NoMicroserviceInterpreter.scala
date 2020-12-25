package finagleprime.interpreters

import finagleprime.algebras.PrimeAlgebra
import cats.Applicative
import cats.implicits._

class NoMicroserviceInterpreter[F[_]: Applicative] extends PrimeAlgebra[F, scala.Int] {

  def calculatePrime(int: scala.Int): F[scala.Int] = {
    if (isPrime(int)) Applicative[F].pure(int)
    else Applicative[F].pure(0)
  }

  def calculateIsPrime(int: scala.Int): F[Boolean] = {
    if (isPrime(int)) Applicative[F].pure(true)
    else Applicative[F].pure(false)
  }

  def calculatePrimes(int: scala.Int): F[List[Int]] = {
    List.range(0, int + 1)
      .filter(isPrime(_))
      .pure[F]
  }

  def isPrime(i: scala.Int): Boolean = {
    if (i <= 1) false
    else if (i == 2) true
    else !(2 until i).exists(n => i % n == 0)
  }
}
