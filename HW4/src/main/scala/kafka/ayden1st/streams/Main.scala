package kafka.ayden1st.streams

import kafka.ayden1st.common._
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.kstream.{TimeWindows, Windowed}
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.serialization.Serdes._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream._

import java.util.Properties
import java.time.Duration

object Main {
  def main(args: Array[String]): Unit = {
    val log = Log.log

    val topic: String= "events"
    val windowSize: TimeWindows = TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5))

    Utils.deleteAllTopic("/admin.properties")
    Thread.sleep(5000)
    Utils.createTopic("/admin.properties", topic, 1, 1)

    val builder: StreamsBuilder = new StreamsBuilder
    val input:  KStream[Windowed[String], Long] = builder.stream[String, String](topic)
      .groupBy((k, _) => k)
      .windowedBy(windowSize)
      .count()
      .toStream
    input.peek((k, v) => log.info("key: {}, value: {}", k, v))
      .to("output")

    log.info("{}", builder.build().describe())

    val props_streams: Properties = Load.loadProperties("/streams.properties")
    val kafkaStreams = new KafkaStreams(builder.build(), props_streams)
    log.info("app started")
    kafkaStreams.start()

    sys.ShutdownHookThread {
      kafkaStreams.close(Duration.ofSeconds(10))
      log.info("app stopped")
    }
  }
}
