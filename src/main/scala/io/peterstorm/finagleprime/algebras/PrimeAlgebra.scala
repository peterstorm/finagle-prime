package finagleprime.algebras

trait PrimeAlgebra[F[_], A] {

  def calculatePrime(int: Int): F[A]

  def calculateIsPrime(int: Int): F[Boolean]

}
