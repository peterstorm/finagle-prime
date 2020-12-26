package finagleprime.effects

import com.twitter.util.{Throw, Future, Return}
import cats.effect.{Async, ContextShift, IO}
import cats.~>

trait NaturalTransformation[F[_], G[_]] extends ~>[F, G]

object NaturalTransformation {

  implicit def tFutureToF[H[_]: Async]: NaturalTransformation[Future, H] = 
    new NaturalTransformation[Future,  H] { 
      def apply[A](future: Future[A]): H[A] = Async[H].async { cont =>
        future.respond {
          case Return(a)    => cont(Right[Throwable, A](a))
          case Throw(error) => cont(Left[Throwable, A](error))
        }
      }
  }
}

