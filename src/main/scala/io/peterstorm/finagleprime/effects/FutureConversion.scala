package finagleprime.effects

import com.twitter.util.{Throw, Future, Return}
import cats.effect.{Async, ContextShift, Bracket}
import cats.~>
import cats.syntax.all._


trait FutureConversion[F[_]] {

  def futureToF[A](fa: F[Future[A]]): F[A]

}

object FutureConversion {
  def apply[F[_]](implicit ev: FutureConversion[F]): FutureConversion[F] = ev

  implicit def instance[F[_]: Async: ContextShift, A]: FutureConversion[F] = new FutureConversion[F] {
    def futureToF[A](fa: F[Future[A]]): F[A] = {
      fa.flatMap{ fu => 
        Bracket[F, Throwable].guarantee {
          Async[F].async[A] { cb => 
            fu.respond { 
              case Return(a) => cb(Right[Throwable, A](a)) 
              case Throw(err) => cb(Left[Throwable, A](err))
            }
          }
        }(ContextShift[F].shift)
      }
    }
  }
}


