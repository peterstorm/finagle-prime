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
import cats.effect.{Concurrent, Sync}
import org.http4s.server.Router
import finagleprime.interpreters.ThriftMicroServiceInterpreterNaive
import cats.effect.Resource
import com.twitter.finagle.Thrift
import com.olegpy.meow.hierarchy._
import finagleprime.thrift._
import com.twitter.util.Future
import finagleprime.interpreters.ThriftMicroServiceInterpreter
import cats.effect.ContextShift
import finagleprime.domain.Errors._
import finagleprime.effects.HttpErrorHandler
import finagleprime.effects.PrimeHttpErrorHandler
import finagleprime.effects.PrimeHttpErrorHandler1

class Module[F[_]: Applicative: Concurrent: Sync] {

  private val primeServiceNoMS: PrimeService[F, Int] = new PrimeService(new NoMicroserviceInterpreter)

  private val primeServiceThriftNaive: PrimeService[F, Int] = new PrimeService(new ThriftMicroServiceInterpreterNaive)

  private val primeServiceThrift: PrimeService[F, Int] = new PrimeService(new ThriftMicroServiceInterpreter)

  private val endpointsNoMs: HttpRoutes[F] = new PrimeEndpoints(primeServiceNoMS).endpointsNoMS

  private val endpointsThriftNaive: HttpRoutes[F] = new PrimeEndpoints(primeServiceThriftNaive).endpointsThriftNaive

  private val endpointsThriftFunctional: HttpRoutes[F] = new PrimeEndpoints(primeServiceThrift).endpointsThriftFunctional

  private val endpoints = endpointsNoMs <+> endpointsThriftNaive <+> endpointsThriftFunctional

  def routeHandler1(implicit RH: HttpErrorHandler[F, Exception]): HttpRoutes[F] =
    RH.handle(endpoints)

  def routeHandler(implicit RH: HttpErrorHandler[F, PrimeError]): HttpRoutes[F] =
    RH.handle(endpoints)

  val thriftServer = 
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

  val httpApp: HttpApp[F] = Router("/api/v1/" -> routeHandler(PrimeHttpErrorHandler[F])).orNotFound

}


