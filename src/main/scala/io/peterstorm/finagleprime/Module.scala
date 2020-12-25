package finagleprime

import finagleprime.algebras.PrimeAlgebra
import finagleprime.services.PrimeService
import finagleprime.endpoints.PrimeEndpoints
import finagleprime.interpreters.NoMicroserviceInterpreter
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.implicits._
import cats.Applicative
import cats.syntax.all._
import cats.effect.Concurrent
import org.http4s.server.Router
import finagleprime.interpreters.ThriftMicroServiceInterpreterNaive
import cats.effect.Resource
import com.twitter.finagle.Thrift
import finagleprime.thrift._
import com.twitter.util.Future

class Module[F[_]: Applicative: Concurrent, A] {

  private val noMicroService: PrimeAlgebra[F, Int] = new NoMicroserviceInterpreter

  private val thriftMicroServiceNaive: PrimeAlgebra[F, Int] = new ThriftMicroServiceInterpreterNaive

  private val primeServiceNoMS: PrimeService[F, Int] = new PrimeService(noMicroService)

  private val primeServiceThriftNaive: PrimeService[F, Int] = new PrimeService(thriftMicroServiceNaive)

  private val endpointsNoMs: HttpRoutes[F] = new PrimeEndpoints(primeServiceNoMS).endpointsNoMS

  private val endpointsThriftNaive: HttpRoutes[F] = new PrimeEndpoints(primeServiceThriftNaive).endpointsThriftNaive

  private val endpoints = endpointsNoMs <+> endpointsThriftNaive

  val thriftServer = 
    Concurrent[F].delay {
      Thrift.server.serveIface("localhost:3005", new ThriftPrimeService.MethodPerEndpoint {
        def calculatePrime(i: Int): Future[Boolean] = {
            if (isPrime(i)) Future.value(true)
            else Future.value(false)
        }

        def calculatePrimes(i: Int) = {
          val result = List.range(0, i + 1).filter(isPrime(_))
          Future.value(result.toSeq)
        }

        def isPrime(i: Int): Boolean = {
            if (i <= 1) false
            else if (i == 2) true
            else !(2 until i).exists(n => i % n == 0)
        }

      })
    }

  val httpApp: HttpApp[F] = Router("/api/v1/" -> endpoints).orNotFound

}


