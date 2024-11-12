package main.scala.kafka.ayden1st.client

import main.scala.kafka.ayden1st.common.{Load, Log}
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}

import java.time.Duration
import java.util.Properties
import scala.jdk.CollectionConverters.IterableHasAsJava
import scala.util.Try

object Consumer {
  def main(args: Array[String]): Unit = {
    val topic1 = "topic1"
    val topic2 = "topic2"

    val log = Log.log

    val props: Properties = Load.loadProperties("/consumer.properties")
    log.debug(props.toString)

    val consumer = new KafkaConsumer[String, String](props)
    consumer.subscribe(List(topic1, topic2).asJavaCollection)

    try {
      while (true) {
        val records: ConsumerRecords[String, String] = consumer.poll(Duration.ofSeconds(1))
        log info s"Just polled the ${records.count()}."
        records.forEach { msg => log.info(s"${msg.partition}\t${msg.offset}\t${msg.key}\t${msg.value}") }
      }
    } catch {
      case e: Exception =>
        log.error(e.getLocalizedMessage)
        sys.exit(-1)
    } finally {
      Try(consumer.close())
        .recover { case error => log.error("Failed to close the kafka consumer", error) }
    }
    sys.exit(0)
  }

}