package finagleprime.effects

import com.twitter.finagle.SimpleFilter
import com.twitter.util.Future
import com.twitter.finagle.Service

class ExceptionFilter extends SimpleFilter[Array[Byte], Array[Byte]] {

    def apply(req: Array[Byte], service: Service[Array[Byte], Array[Byte]]): Future[Array[Byte]] = {
      service(req).handle {
        case _: IllegalArgumentException => req
      }
    }
}



