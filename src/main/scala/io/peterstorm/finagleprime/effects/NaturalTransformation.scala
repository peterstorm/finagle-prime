package finagleprime.effects

import com.twitter.util.{Throw, Future, Return}
import cats.effect.{Async, Bracket, ContextShift}
import cats.~>
import cats.syntax.all._

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

  implicit def twFutureToF[H[_]: Async: ContextShift, A]: NaturalTransformation[Lambda[A => H[Future[A]]], H] =
    new NaturalTransformation[Lambda[A => H[Future[A]]], H] {
      def apply[A](fa: H[Future[A]]): H[A] = {
            fa.flatMap{ fu => 
              Bracket[H, Throwable].guarantee {
                Async[H].async[A] { cont => 
                  fu.respond { 
                    case Return(a) => cont(Right[Throwable, A](a)) 
                    case Throw(err) => cont(Left[Throwable, A](err))
                  }
                }
              }(ContextShift[H].shift)
            }
      }
    }
}

