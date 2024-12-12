package kafka.ayden1st.alpakka

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Producer
import akka.kafka.ProducerSettings
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, Materializer}
import ch.qos.logback.classic.{Level, Logger}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object ProducerApp extends App {
  LoggerFactory
    .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    .asInstanceOf[Logger]
    .setLevel(Level.ERROR)

  implicit val system: ActorSystem = ActorSystem("producer-sys")
  implicit val mat: Materializer  = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  private val config = ConfigFactory.load()
  private val producerConfig = config.getConfig("akka.kafka.producer")
  private val producerSettings:ProducerSettings[String, String] = ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)

  private val produce: Future[Done] =
    Source(1 to 100)
      .map(value=> new ProducerRecord[String, String]("akka", value.toString))
      .runWith(Producer.plainSink(producerSettings))

  produce onComplete {
    case Success(_) => println("Done"); system.terminate()
    case Failure(err) => println(err.toString); system.terminate()
  }
}