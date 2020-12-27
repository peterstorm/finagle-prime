namespace * finagleprime.thrift

exception ArgumentException {
    1: string message;
}


service ThriftPrimeService {
  bool calculatePrime(1:i32 i) throws (1: ArgumentException ex)
  list<i32> calculatePrimes(1:i32 i) throws (1: ArgumentException ex)
}
