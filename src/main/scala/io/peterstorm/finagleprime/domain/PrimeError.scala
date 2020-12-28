package finagleprime.domain

object Errors {
  sealed trait PrimeError extends Exception
  case class InvalidArgument(message: String) extends PrimeError
  case class UnknownPrimeError(message: String) extends PrimeError
}


