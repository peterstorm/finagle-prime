package finagleprime.effects

import cats.effect._
import cats.syntax.all._
import com.twitter.finagle.Thrift.Client
import com.twitter.util.Future
import com.twitter.util.Closable
import scala.reflect.ClassTag
import scala.language.reflectiveCalls
import com.twitter.finagle.service.ResponseClassifier
import com.twitter.util.Duration
import com.twitter.finagle.service.RetryBudget
import com.twitter.finagle.service.Backoff
import java.util.concurrent.TimeUnit
import com.twitter.finagle.Filter
import com.twitter.finagle.thrift.ThriftClientRequest

object ThriftClientBuilder {

  def apply[F[_]: Sync: Async](client: Client) =
    new ThriftClientBuilder[F](Sync[F].delay(client))

}

final private[finagleprime] case class ThriftClientBuilder[F[_]: Sync: Async](
  private val client: F[Client],
  )(implicit NT: NaturalTransformation[Future, F]){

    type ToClosable = {
        def asClosable: Closable
      }

    def withRetryBackoff(start: Long, maximum: Long): ThriftClientBuilder[F] =
      copy(client = client
        .map(_.withRetryBackoff(
          Backoff.decorrelatedJittered(
            Duration(start, TimeUnit.SECONDS), Duration(maximum, TimeUnit.SECONDS))))
        )

    def withRetryBudget(ttl: Long, minRetries: Int, retryPercent: Double): ThriftClientBuilder[F] =
      copy(client = client.map(_.withRetryBudget(RetryBudget(Duration(ttl, TimeUnit.SECONDS), minRetries, retryPercent))))

    def withResponseClassifier(rc: ResponseClassifier): ThriftClientBuilder[F] =
      copy(client = client.map(_.withResponseClassifier(rc)))

    def filtered(filter: Filter[ThriftClientRequest, Array[Byte], ThriftClientRequest, Array[Byte]]): ThriftClientBuilder[F] =
      copy(client = client.map(_.filtered(filter)))

    def build[Srv <: ToClosable: ClassTag](destination: String, label: String): F[Srv] =
      client.map(_.build[Srv](destination, label))

    def resource[Srv <: ToClosable: ClassTag](destination: String, label: String): Resource[F, Srv] =
      makeResource(client.map(_.build[Srv](destination, label)))

    private def makeResource[Srv <: ToClosable: ClassTag](service: F[Srv]): Resource[F, Srv] =
      Resource.make(service)(service => NT(service.asClosable.close(Duration(1, TimeUnit.SECONDS))))

}


