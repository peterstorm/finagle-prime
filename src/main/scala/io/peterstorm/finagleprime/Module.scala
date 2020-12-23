package finagleprime

import finagleprime.algebras.PrimeAlgebra
import finagleprime.services.PrimeService
import finagleprime.endpoints.PrimeEndpoints
import finagleprime.interpreters.NoMicroserviceInterpreter
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.implicits._
import cats.Applicative
import cats.effect.Concurrent

class Module[F[_]: Applicative: Concurrent, A] {

  private val noMicroService: PrimeAlgebra[F, Int] = new NoMicroserviceInterpreter()

  private val primeService: PrimeService[F, Int] = new PrimeService(noMicroService)

  private val endpoints: HttpRoutes[F] = new PrimeEndpoints(primeService).endpoints

  val httpApp: HttpApp[F] = endpoints.orNotFound

}


