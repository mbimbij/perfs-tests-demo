package com.example.demo

;

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.util.Random
import scala.concurrent.duration.{DurationInt, DurationLong};

class LoadTest extends Simulation {

  val peakLoad = 10;
  val rampUpSeconds = 10;
  val totalDurationSeconds = 60;
  val maxThinkTimeMillis = 500;

  val httpProtocols = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("text/html,application/xhtml+xml,application/xml,application/json;q=0.9,*/*;q=0.8")
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")

  val random = new Random();

  val scn = scenario("actuator health").forever("loopCount") {
    exec(
      http("/actuator/health")
        .get("/actuator/health")
    ).pause(0, maxThinkTimeMillis.milliseconds)
  }

  setUp(scn.inject(
    rampUsers(peakLoad) during (rampUpSeconds seconds)
  ))
    .protocols(httpProtocols)
    .maxDuration(totalDurationSeconds seconds)
}
