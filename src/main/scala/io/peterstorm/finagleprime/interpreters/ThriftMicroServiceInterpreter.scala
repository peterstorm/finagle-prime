package finagleprime.interpreters

import finagleprime.thrift._
import finagleprime.algebras.PrimeAlgebra
import finagleprime.effects.NaturalTransformation
import com.twitter.finagle._
import com.twitter.util.Future
import cats.Functor
import cats.effect._
import cats.effect.implicits._
import cats.syntax.all._
import finagleprime.effects.ThriftClientBuilder
import com.twitter.finagle.thrift.service.ThriftResponseClassifier

class ThriftMicroServiceInterpreter[F[_]: Sync: Async: ContextShift](implicit NT: NaturalTransformation[Future, F]) extends PrimeAlgebra[F, scala.Int] {

  private val client: Resource[F, ThriftPrimeService.MethodPerEndpoint] = 
    ThriftClientBuilder[F](Thrift.client)
      .withRetryBackoff(10, 10)
      .withRetryBudget(10, 10, 0.9)
      .withResponseClassifier(ThriftResponseClassifier.ThriftExceptionsAsFailures)
      .build[ThriftPrimeService.MethodPerEndpoint]("localhost:3005")

  def calculatePrime(int: Int): F[Int] = ???
    
  def calculateIsPrime(int: Int): F[Boolean] =
    client.use(c => NT(c.calculatePrime(int)))
    

  def calculatePrimes(int: Int): F[List[Int]] =
    client.use(c => NT(c.calculatePrimes(int)).map(_.toList))

  def isPrime(i: scala.Int): Boolean = {
    if (i <= 1) false
    else if (i == 2) true
    else !(2 until i).exists(n => i % n == 0)
  }

}



