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
import finagleprime.interpreters.ThriftMicroServiceInterpreter
import cats.effect.ContextShift

class Module[F[_]: Applicative: Concurrent: ContextShift] {

  private val primeServiceNoMS: PrimeService[F, Int] = new PrimeService(new NoMicroserviceInterpreter)

  private val primeServiceThriftNaive: PrimeService[F, Int] = new PrimeService(new ThriftMicroServiceInterpreterNaive)

  private val primeServiceThrift: PrimeService[F, Int] = new PrimeService(new ThriftMicroServiceInterpreter)

  private val endpointsNoMs: HttpRoutes[F] = new PrimeEndpoints(primeServiceNoMS).endpointsNoMS

  private val endpointsThriftNaive: HttpRoutes[F] = new PrimeEndpoints(primeServiceThriftNaive).endpointsThriftNaive

  private val endpointsThriftFunctional: HttpRoutes[F] = new PrimeEndpoints(primeServiceThriftNaive).endpointsThriftFunctional

  private val endpoints = endpointsNoMs <+> endpointsThriftNaive <+> endpointsThriftFunctional

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


