package finagleprime

import cats.Applicative
import cats.effect.{Concurrent, Sync}
import cats.syntax.all._
import com.olegpy.meow.hierarchy._
import com.twitter.finagle.Thrift
import com.twitter.util.Future
import finagleprime.algebras.PrimeAlgebra
import finagleprime.domain.Errors._
import finagleprime.effects.{HttpErrorHandler, NaturalTransformation, PrimeHttpErrorHandler}
import finagleprime.endpoints.PrimeEndpoints
import finagleprime.interpreters._
import finagleprime.services.PrimeService
import finagleprime.thrift._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.{HttpApp, HttpRoutes}

class Module[F[_]: Applicative: Concurrent: Sync](client: ThriftPrimeService.MethodPerEndpoint)(implicit NT: NaturalTransformation[Lambda[A => F[Future[A]]], F]) {

  private val primeServiceNoMS: PrimeService[F, Int] = new PrimeService(new NoMicroserviceInterpreter)

  private val primeServiceThriftNaive: PrimeService[F, Int] = new PrimeService(new ThriftMicroServiceInterpreterNaive)

  private val primeServiceThrift: PrimeService[F, Int] = new PrimeService(new ThriftMicroServiceInterpreter(client))

  private val endpointsNoMs: HttpRoutes[F] = new PrimeEndpoints(primeServiceNoMS).endpointsNoMS

  private val endpointsThriftNaive: HttpRoutes[F] = new PrimeEndpoints(primeServiceThriftNaive).endpointsThriftNaive

  private val endpointsThriftFunctional: HttpRoutes[F] = new PrimeEndpoints(primeServiceThrift).endpointsThriftFunctional

  val endpoints = endpointsNoMs <+> endpointsThriftNaive <+> endpointsThriftFunctional

  def routeHandler(implicit RH: HttpErrorHandler[F, PrimeError]): HttpRoutes[F] =
    RH.handle(endpoints)

  val httpApp: HttpApp[F] = Router("/api/v1/" -> routeHandler(PrimeHttpErrorHandler[F])).orNotFound

}


