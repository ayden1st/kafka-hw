package kafka.ayden1st.alpakka

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source, ZipN}
import com.typesafe.config.{Config, ConfigFactory}
import akka.kafka.{ConsumerSettings, Subscriptions}
import org.apache.kafka.common.serialization.StringDeserializer
import ch.qos.logback.classic.{Level, Logger}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContextExecutor

object ConsumerApp {
  LoggerFactory
    .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    .asInstanceOf[Logger]
    .setLevel(Level.ERROR)

  val config: Config = ConfigFactory.load()
  private val consumerConfig = config.getConfig("akka.kafka.consumer")
  private val consumerSettings:ConsumerSettings[String, String] = ConsumerSettings(consumerConfig, new StringDeserializer, new StringDeserializer)

  private val consume = Consumer
    .plainSource(consumerSettings, Subscriptions.topics("akka"))

  implicit val system: ActorSystem = ActorSystem("fusion")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  private val graph =
    GraphDSL.create(){ implicit builder: GraphDSL.Builder[NotUsed] =>

      import GraphDSL.Implicits._

      val input = builder.add(consume)
      val value = builder.add(Flow[ConsumerRecord[String, String]].map(_.value().toInt))
      val mod1 = builder.add(Flow[Int].map(x=>x*10))
      val mod2 = builder.add(Flow[Int].map(x=>x*2))
      val mod3 = builder.add(Flow[Int].map(x=>x*3))
      val output = builder.add(Sink.foreach(println))

      val broadcast = builder.add(Broadcast[Int](3))
      val zip3broadcast = ZipN[Int](3)
      val zip = builder.add(zip3broadcast)

      input ~> value ~> broadcast.in

      broadcast.out(0) ~> mod1 ~> zip.in(0)
      broadcast.out(1) ~> mod2 ~> zip.in(1)
      broadcast.out(2) ~> mod3 ~> zip.in(2)

      zip.out ~> output

      ClosedShape
    }

  def main(args: Array[String]) : Unit ={
    RunnableGraph.fromGraph(graph).run()
  }
}
