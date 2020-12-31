package finagleprime.delivery

import com.twitter.finagle.Thrift
import com.twitter.util.Future
import com.twitter.finagle.thrift.service.ThriftResponseClassifier
import finagleprime.thrift._
import cats.effect.{Async, Resource, Sync}

import finagleprime.effects.{ThriftClientBuilder, NaturalTransformation}

object ThriftClient {

  def create[F[_]: Sync: Async](implicit NT: NaturalTransformation[Lambda[A => F[Future[A]]], F]): Resource[F, ThriftPrimeService.MethodPerEndpoint] =
    ThriftClientBuilder[F](Thrift.client)
      .withResponseClassifier(ThriftResponseClassifier.ThriftExceptionsAsFailures)
      .withRetryBackoff(2, 32)
      .withRetryBudget(5, 5, 0.1)
      .withRequestTimeout(300)
      .withLoadBalancer(100, 100)
      .withFailureAccrualPoliy(20, 5, 20)
      .withNoFailFast
      .resource[ThriftPrimeService.MethodPerEndpoint]("localhost:3005", "prime_service")
}

