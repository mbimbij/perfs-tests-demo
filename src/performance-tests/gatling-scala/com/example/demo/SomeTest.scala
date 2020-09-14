package com.example.demo

;

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt;

class SomeTest extends Simulation {
  val httpProtocols = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("text/html,application/xhtml+xml,application/xml,application/json;q=0.9,*/*;q=0.8")
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")

  val browse = repeat(2, "n") {
    exec(http("/actuator/health n°{n}")
      .get("/actuator/health")
    )
  }

  val scn = scenario("actuator health").exec(browse)

  setUp(
    scn
      .inject(
        constantConcurrentUsers(10) during(60 seconds)
      )
  ).protocols(httpProtocols)
}
