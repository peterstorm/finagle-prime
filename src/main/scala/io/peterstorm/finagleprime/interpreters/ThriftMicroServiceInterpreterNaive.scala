package finagleprime.interpreters

import com.twitter.finagle.Thrift
import com.twitter.util.Duration._
import com.twitter.util.Await
import cats.Applicative

import finagleprime.thrift._
import finagleprime.algebras.PrimeAlgebra

class ThriftMicroServiceInterpreterNaive[F[_]: Applicative] extends PrimeAlgebra[F, scala.Int] {

  private val thriftClient: ThriftPrimeService.MethodPerEndpoint =
    Thrift
      .client
      .build[ThriftPrimeService.MethodPerEndpoint]("localhost:3005")

  def calculatePrime(int: Int): F[Int] = ???

  def calculateIsPrime(int: Int): F[Boolean] = {
    val result = thriftClient.calculatePrime(int)
    val awaited = Await.result(result).booleanValue()
    Applicative[F].pure(awaited)
  }

  def calculatePrimes(int: Int): F[List[Int]] = {
    val result = thriftClient.calculatePrimes(int)
    val awaited = Await.result(result)
    Applicative[F].pure(awaited.toList)
        
  }
  
  def isPrime(i: scala.Int): Boolean = {
    if (i <= 1) false
    else if (i == 2) true
    else !(2 until i).exists(n => i % n == 0)
  }

}


