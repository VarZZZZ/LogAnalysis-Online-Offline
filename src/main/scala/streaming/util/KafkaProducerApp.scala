package streaming.util

import java.util
import java.util.{Date, Properties, UUID}

import com.alibaba.fastjson.JSONObject
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.util.Random

/**
 * @Author: ly
 * @Date: 2020/7/4 14:42
 * @Version 1.0
 *
 */
object KafkaProducerApp {
  def main(args: Array[String]): Unit = {
    val prop = new Properties()

    prop.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer")
    prop.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer")
    prop.put("bootstrap.servers",ParamsConf.brokers)
    prop.put("request.required.acks","1")

    val topic = ParamsConf.topic
    val producer = new KafkaProducer[String,String](prop)


    val random = new Random()
    val dateFormat = FastDateFormat.getInstance("yyyyMMddmmss")

    for(i <- 1 to 100){
      val time = dateFormat.format(new Date())+""
      val userId = random.nextInt(1000)+""
      val courseId = random.nextInt(500)+""
      val fee = random.nextInt(400)+""
      val res = Array("0", "1")
      val flag = res(random.nextInt(2))+"" // 是否付款了
      val orderId = UUID.randomUUID().toString
      val map = new util.HashMap[String, Object]()
      map.put("time",time)
      map.put("userid",userId)
      map.put("courseid",courseId)
      map.put("fee",fee)
      map.put("flag",flag)
      map.put("orderid",orderId)
      val json = new JSONObject(map)


     producer.send(new ProducerRecord[String,String](topic,i+"",json.toString))
    }

    println("LA kafka生产数据成功")


  }
}
