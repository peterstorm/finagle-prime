package finagleprime.delivery

import cats.effect._
import com.twitter.finagle.Thrift
import com.twitter.util.Future

import finagleprime.effects.NaturalTransformation
import finagleprime.thrift._

object  ThriftServer {

  def create[F[_]: Concurrent](implicit NT: NaturalTransformation[Future, F]) = 
    Resource.make { 
      Concurrent[F].delay {
        Thrift.server.serveIface("localhost:3005", new ThriftPrimeService.MethodPerEndpoint {
          def calculatePrime(i: Int): Future[Boolean] = {
            if(i < 1) Future.exception(new ArgumentException("Illegal argument, less than 1"))
            else {
              if (isPrime(i)) Future.value(true)
              else Future.value(false)
            }
          }

          def calculatePrimes(i: Int) = {
            if(i < 1) Future.exception(new ArgumentException("Illegal argument, less than 1"))
            else {
              val result = List.range(0, i + 1).filter(isPrime(_))
              Future.value(result.toSeq)
            }
          }

          def isPrime(i: Int): Boolean = {
              if (i <= 1) false
              else if (i == 2) true
              else !(2 until i).exists(n => i % n == 0)
          }
        })
      } 
    }(server => Concurrent[F].delay(NT(server.close())))

}
