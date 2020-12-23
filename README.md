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

