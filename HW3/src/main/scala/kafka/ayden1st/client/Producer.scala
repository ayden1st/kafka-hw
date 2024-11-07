package main.scala.kafka.ayden1st.client

import main.scala.kafka.ayden1st.common.{Load, Log}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import java.util.Properties
import scala.util.Try

object Producer {
  def main(args: Array[String]): Unit = {
    val topic1= "topic1"
    val topic2= "topic2"

    val log = Log.log

    val props: Properties = Load.loadProperties("/producer.properties")
    log.debug(props.toString)
    val producer = new KafkaProducer[String,String](props)

    producer.initTransactions()
    producer.beginTransaction()
    log.debug("begin transaction")
    for (i <- 1 to 5) {
      producer.send(new ProducerRecord[String, String](topic1, i.toString, topic1 + "-message-" + i))
      producer.send(new ProducerRecord[String, String](topic2, i.toString, topic2 + "-message-" + i))
    }
    producer.commitTransaction()
    log.debug("commit transaction")

    producer.beginTransaction()
    log.debug("begin transaction")
    for (i <- 6 to 7) {
      producer.send(new ProducerRecord[String, String](topic1, i.toString, topic1 + "-message-" + i))
      producer.send(new ProducerRecord[String, String](topic2, i.toString, topic2 + "-message-" + i))
    }
    producer.abortTransaction()
    log.debug("abort transaction")

    Try {
      producer.flush()
      producer.close()
      log info "Successfully produce the complete."
    }.recover {
      case error: InterruptedException => log.error("failed to flush and close the producer", error)
      case error => log.error("An unexpected error occurs while producing the show collection", error)
    }
  }
}
