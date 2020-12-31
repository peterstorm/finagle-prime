package finagleprime.interpreters

import com.twitter.finagle.thrift.service.ThriftResponseClassifier
import com.twitter.finagle._
import com.twitter.util.Future
import cats.effect._
import cats.syntax.all._

import finagleprime.thrift._
import finagleprime.algebras.PrimeAlgebra
import finagleprime.effects.{NaturalTransformation, ThriftClientBuilder}
import com.twitter.util.Await

class ThriftMicroServiceInterpreter[F[_]: Sync: Async](client: ThriftPrimeService.MethodPerEndpoint)(implicit NT: NaturalTransformation[Lambda[A => F[Future[A]]], F]) extends PrimeAlgebra[F, Int] {

  def calculatePrime(int: Int): F[Int] =
    NT(Sync[F].delay(client.calculatePrimes(int).map(_.toList.head)))

  def calculateIsPrime(int: Int): F[Boolean] =
    NT(Sync[F].delay(client.calculatePrime(int)))

  def calculatePrimes(int: Int): F[List[Int]] =
    NT(Sync[F].delay(client.calculatePrimes(int).map(_.toList)))

  def isPrime(i: scala.Int): Boolean = {
    if (i <= 1) false
    else if (i == 2) true
    else !(2 until i).exists(n => i % n == 0)
  }

}



