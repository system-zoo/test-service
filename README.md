#test-service#

This is a very basic service which can be used for simulating a service at different error/fail rates with variable amounts of latency.

###Usage###

`docker run -p 80:80 -e "LATENCY=1000" -e "FAIL_RATE=.5" -e "BAD_RESPONSE_RATE=.5" systemzoo/test-service`

Parameters
 - **LATENCY** - the maximum amount of time in ms to delay responding to the request. A random value will be picked between 0 and latency
 - **FAIL_RATE** - the rate at which we should return a 500 code, 0 = never / 1 = always
 - **BAD_RESPONSE_RATE** - the rate at which we should return back a response inconsistent with the request, 0 = never / 1 = always
 
###Endpoints###

- GET /       -> returns back either 200 or 500
- GET /$int   -> returns back either 200 or 500 with a body of either $int or $int + 1

###Logging###

For each request recieved the server with log a json message to stdout:

```
2015-06-20 20:29:12,386 INFO  com.systemzoo.TestServiceActor  - {"code":500,"duration":528, "request":1, "response":2}
2015-06-20 20:34:12,757 INFO  com.systemzoo.TestServiceActor  - {"code":200,"duration":0}
```
