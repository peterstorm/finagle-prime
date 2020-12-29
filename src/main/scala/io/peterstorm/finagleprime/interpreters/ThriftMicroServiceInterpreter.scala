package finagleprime.interpreters

import com.twitter.finagle.thrift.service.ThriftResponseClassifier
import com.twitter.finagle._
import com.twitter.util.Future
import cats.effect._
import cats.syntax.all._

import finagleprime.thrift._
import finagleprime.algebras.PrimeAlgebra
import finagleprime.effects.{NaturalTransformation, ThriftClientBuilder}

class ThriftMicroServiceInterpreter[F[_]: Sync: Async](implicit NT: NaturalTransformation[Lambda[A => F[Future[A]]], F]) extends PrimeAlgebra[F, Int] {

  private val resourceClient: Resource[F, ThriftPrimeService.MethodPerEndpoint] =
    ThriftClientBuilder[F](Thrift.client)
      .withResponseClassifier(ThriftResponseClassifier.ThriftExceptionsAsFailures)
      .withRetryBackoff(2, 32)
      .withRetryBudget(5, 5, 0.1)
      .withRequestTimeout(300)
      .withLoadBalancer(100, 100)
      .withFailureAccrualPoliy(20, 5, 20)
      .withNoFailFast
      .resource[ThriftPrimeService.MethodPerEndpoint]("localhost:3005", "prime_service")

  def calculatePrime(int: Int): F[Int] =
    resourceClient.use(c => NT(Sync[F].delay(c.calculatePrimes(int).map(_.toList.head))))

  def calculateIsPrime(int: Int): F[Boolean] =
    resourceClient.use(c => NT(Sync[F].delay(c.calculatePrime(int))))

  def calculatePrimes(int: Int): F[List[Int]] =
    resourceClient.use(c => NT(Sync[F].delay(c.calculatePrimes(int).map(_.toList))))

  def isPrime(i: scala.Int): Boolean = {
    if (i <= 1) false
    else if (i == 2) true
    else !(2 until i).exists(n => i % n == 0)
  }

}



