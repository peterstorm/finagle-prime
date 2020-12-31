package finagleprime.effects

import cats.effect._
import cats.syntax.all._
import com.twitter.finagle.Thrift.Client
import com.twitter.util.Future
import com.twitter.util.Closable
import com.twitter.finagle.service.ResponseClassifier
import com.twitter.util.Duration
import com.twitter.finagle.service.RetryBudget
import com.twitter.finagle.service.Backoff
import com.twitter.finagle.Filter
import com.twitter.finagle.thrift.ThriftClientRequest
import com.twitter.finagle.loadbalancer.Balancers
import scala.reflect.ClassTag
import scala.language.reflectiveCalls
import java.util.concurrent.TimeUnit

import finagleprime.effects.NaturalTransformation
import com.twitter.finagle.liveness.FailureAccrualFactory
import com.twitter.finagle.liveness.FailureAccrualPolicy

object ThriftClientBuilder {

  def apply[F[_]: Sync: Async](client: Client)(implicit NT: NaturalTransformation[Lambda[A => F[Future[A]]], F])=
    new ThriftClientBuilder[F](client.pure[F])

}

final private[finagleprime] case class ThriftClientBuilder[F[_]: Sync: Async](private val client: F[Client])(implicit NT: NaturalTransformation[Lambda[A => F[Future[A]]], F]) {

    type ToClosable = {
        def asClosable: Closable
      }

    def withRetryBackoff(start: Long, maximum: Long): ThriftClientBuilder[F] =
      copy(client = client.map(_.withRetryBackoff(
          Backoff.decorrelatedJittered(
            Duration(start, TimeUnit.SECONDS), Duration(maximum, TimeUnit.SECONDS))))
        )

    def withConfig(f: Client => Client): ThriftClientBuilder[F] =
      copy(client = client.map(f))

    def withRetryBudget(ttl: Long, minRetries: Int, retryPercent: Double): ThriftClientBuilder[F] =
      copy(client = client.map(_.withRetryBudget(RetryBudget(Duration(ttl, TimeUnit.SECONDS), minRetries, retryPercent))))

    def withResponseClassifier(rc: ResponseClassifier): ThriftClientBuilder[F] =
      copy(client = client.map(_.withResponseClassifier(rc)))

    def withRequestTimeout(toMs: Long): ThriftClientBuilder[F] =
      copy(client = client.map(_.withRequestTimeout(Duration(toMs, TimeUnit.MILLISECONDS))))

    def withLoadBalancer(maxEffort: Int, decayTime: Long):  ThriftClientBuilder[F] =
      copy(client = client.map(_.withLoadBalancer(Balancers.p2cPeakEwma(decayTime = Duration(decayTime, TimeUnit.SECONDS), maxEffort = maxEffort))))

    def withNoFailFast: ThriftClientBuilder[F] =
      copy(client = client.map(_.withSessionQualifier.noFailFast))

    def withFailureAccrualPoliy(numFailures: Int, start: Long, maximum: Long): ThriftClientBuilder[F] =
      copy(client = client.map(_.configured(FailureAccrualFactory.Param(() => FailureAccrualPolicy.consecutiveFailures(numFailures, markDeadFor = Backoff.decorrelatedJittered(Duration(start, TimeUnit.SECONDS), Duration(maximum, TimeUnit.SECONDS)))))))

    def filtered(filter: Filter[ThriftClientRequest, Array[Byte], ThriftClientRequest, Array[Byte]]): ThriftClientBuilder[F] =
      copy(client = client.map(_.filtered(filter)))

    def build[Srv: ClassTag](destination: String, label: String): F[Srv] =
      client.map(_.build[Srv](destination, label))

    def resource[Srv <: ToClosable: ClassTag](destination: String, label: String): Resource[F, Srv] =
      makeResource(client.map(_.build[Srv](destination, label)))

    private def makeResource[Srv <: ToClosable: ClassTag](service: F[Srv]): Resource[F, Srv] =
      Resource.make(service)(service => NT(Sync[F].delay(service.asClosable.close)))

}
