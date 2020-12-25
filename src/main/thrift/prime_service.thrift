namespace * finagleprime.thrift

service ThriftPrimeService {
  bool calculatePrime(1:i32 i)
  list<i32> calculatePrimes(1:i32 i)
}
