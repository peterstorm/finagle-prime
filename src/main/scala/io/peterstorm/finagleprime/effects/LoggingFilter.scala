package finagleprime.effects

import com.twitter.finagle.SimpleFilter
import com.twitter.finagle.Service
import com.twitter.util.Future
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.apache.thrift.transport.TMemoryInputTransport
import org.apache.thrift.protocol.TProtocolFactory
import cats.effect.Sync
import finagleprime.thrift.ThriftPrimeService._
import finagleprime.thrift.ThriftPrimeService

class LoggingFilter[F[_]: Sync](serviceName: String) extends SimpleFilter[CalculatePrimes.Args, CalculatePrimes.SuccessType] {

  implicit def unsafeLogger = Slf4jLogger.getLogger[F]

  def apply(request: CalculatePrimes.Args, service: Service[CalculatePrimes.Args,CalculatePrimes.SuccessType]): Future[CalculatePrimes.SuccessType] = {
    val arg = request.i
    Logger[F].info(s"$serviceName called with argument ${arg.toString}")
    service(request)
  }

}


