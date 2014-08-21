# Protean Sample Service

A contrived example of a fairly exhaustive range of testable endpionts.


## Usage

### Starting the service

    lein deps
    lein run

 runs on port 3002

### Accessing resources




### Integration testing the service simulation

N.B. The following merely aggregates the responses from hitting the simulated resources - nothing too useful at the time of writing.

    curl -v -X POST -H "Content-Type: application/json" --data '{"locs":["thingsservice"],"seed":{"drink":"ale"}}' 'http://localhost:3001/test'


### Automatically integration testing the service

The below will automatically (incrementally) integration test the service.  Host and port should be configured to point to an instance of Protean (admin port).  At the time of writing this is a naive test mechanism which produces a JSON payload... JUnit output is in the pipeline.

Testing specific resources:

    curl -v -X POST -H "Content-Type: application/json" --data '{"port":3002,"locs":["beers token starches/pick yeasts/pick flavourings/pick"],"seed":{"drink":"ale"}}' 'http://localhost:3001/test'

Testing the entire service:

    curl -v -X POST -H "Content-Type: application/json" --data '{"port":3002,"locs":["beers"],"seed":{"drink":"ale"}}' 'http://localhost:3001/test'


## Contributing

All contributions ideas/pull requests/bug reports are welcome, we hope you find it useful.


## License

Protean is licensed with Apache License v2.0.
