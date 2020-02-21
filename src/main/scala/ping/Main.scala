package co.innerproduct
package ping

import cats.effect.{Clock, ExitCode, IO, IOApp}
import cats.implicits._
import org.http4s.client.middleware.Metrics
import org.http4s.metrics.dropwizard.Dropwizard
import com.codahale.metrics.SharedMetricRegistries
import org.http4s._
import org.http4s.client._
import cats.effect.Blocker
import java.util.concurrent._

object Main extends IOApp {
  def putStrlLn(value: String) = IO(println(value))

  def run(args: List[String]) = {
    println("Starting client..")

    val blockingPool = Executors.newFixedThreadPool(5)
    val blocker = Blocker.liftExecutorService(blockingPool)
    val httpClient: Client[IO] = JavaNetClientBuilder[IO](blocker).create

    implicit val clock = Clock.create[IO]
    val registry = SharedMetricRegistries.getOrCreate("default")
    val requestMethodClassifier = (r: Request[IO]) => Some(r.method.toString.toLowerCase)
    val meteredClient = Metrics[IO](Dropwizard(registry, "prefix"), requestMethodClassifier)(httpClient)

    // meteredClient.expect[String](null)
    val timeout = sys.env("TIMEOUT");
    val iterations = sys.env("ITERATIONS");
    val url = sys.env("URL");

    println(s"timeout=$timeout, iterations=$iterations, url=$url")

    while (true) {
      val page: IO[String] = meteredClient.expect[String](Uri.uri("https://www.google.com/"))
      println(page.map(_.take(400)).unsafeRunSync())
      Thread.sleep(1000)
    }

    PingServer.stream.compile.drain.as(ExitCode.Success)
  }
}
