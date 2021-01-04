## finagle-prime

### Brainstorming  phase

After some research, I have decided to try and implement it the following way:
- use tagless final / mtl style, as I feel it's good for abstration, and it's the way I enjoy working
- use scrooge, to generate code from the thrift IDL. Made by twitter, and if it's good enough for them, it's good enough for us!
- use various libraries I love, like cats, cats-effect, fs2, http4s
- if possible, try to wrap the creation of thrift client and servers in a functional eDSL. I read that there was both Thrift and ThriftMux, would be nice to abstract that choice.

### Project structure

- use http4s for the proxy-service
  - calls the prime-number-service
  - will probably implement different endpoints, moving forward, going from naive to semi complete
- prime-number-service
  - construct via a tagless final algebra
  - algebra should be polymorphic in the return type and effect, to open the door for different implementations and backends
- possibly add an eDSL to construct thrift client and servers.

### Folders
I know there are probably too many folders, for such a small project, but I like to think ahead! And I like the organization of 'foldering' :D

- [algebras](https://github.com/peterstorm/finagle-prime/tree/master/src/main/scala/io/peterstorm/finagleprime/algebras). `PrimeAlgebra`, which is the trait/typeclass we base the whole project on.
- [delivery](https://github.com/peterstorm/finagle-prime/tree/master/src/main/scala/io/peterstorm/finagleprime/delivery).  Creation of the `ThriftServer`, `ThriftClient` and http `Server`.
- [domain](https://github.com/peterstorm/finagle-prime/tree/master/src/main/scala/io/peterstorm/finagleprime/domain). `PrimeError` sum type, which we use to raise business errors with. In a more complicated app, I would have subfolders, haha :D
- [effects](https://github.com/peterstorm/finagle-prime/tree/master/src/main/scala/io/peterstorm/finagleprime/effects). In here you'll find the simple `ThriftClientBuilder`, some typeclasses for converting between Twitter Future and our generic `F` (`FutureConversion`, which is not used, and `NaturalTransformation`). The `HttpErrorErrorHandler` that is a middlewhere for our API, that can "catch" any business errors we have raised in our code. And a failed attempt at a `LogginFilter`, which I did't get to work, because I went with the `MethodPerEndpoint` API, and not the `ServicePerEndpoint` API.
- [endpoints](https://github.com/peterstorm/finagle-prime/tree/master/src/main/scala/io/peterstorm/finagleprime/endpoints). The routes for the API, `PrimeEndpoints`.
- [interpreters](https://github.com/peterstorm/finagle-prime/tree/master/src/main/scala/io/peterstorm/finagleprime/interpreters). Our three interpreters of the `PrimeAlgebra`. A `NoMicroserviceInterpreter` that is pure. A `ThriftMicroServiceInterpreterNaive` that does not comply to the functional paradigme. And `ThriftMicroServiceInterpreter` that goes all functional.
- [services](https://github.com/peterstorm/finagle-prime/tree/master/src/main/scala/io/peterstorm/finagleprime/services). The `PrimeService` that uses the various implenentations of the `PrimeAlgebra`. Is then used in the `PrimeEndpoints` to deliver the prime numbers.
- [root](https://github.com/peterstorm/finagle-prime/tree/master/src/main/scala/io/peterstorm/finagleprime). `Module` ties together all the interpreters, services and endpoints. `Main` creates the resource save thrift server, client and http server.

### How to run
If you have `nix`, it's easy:
```
> nix-shell
> sbt
sbt-finagle-prime> run
```

If not, it's compiled on JDK 11, and you need `sbt`
```
> sbt
sbt-finagle-prime> run
```

### Documentation and guides used:

- https://diwakergupta.github.io/thrift-missing-guide/
- https://twitter.github.io/scrooge/index.html
- https://twitter.github.io/finagle/guide/index.html
- https://engineering.creditkarma.com/how-to-make-your-finagle-services-resilient
