package streaming.util

import com.alibaba.fastjson.JSON
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * @Author: ly
 * @Date: 2020/7/4 15:52
 * @Version 1.0  
 *
 */
object StreamingApp {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[2]").setAppName("StreamingApp")
    val ssc = new StreamingContext(sparkConf,Seconds(5)) // 采集周期5s，周期内会有多个rdd组成dstream

    val stream = KafkaUtils.createDirectStream(ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](ParamsConf.topic, ParamsConf.kafkaParams))

//    stream.map(x=>x.value()).print()

    /**
     * 统计每天付费成功的总订单数
     * 统计每天付费成功的总订单金额
     */
    stream.foreachRDD(rdd=>{
      // x 是key -value的，value是json，key是topic
      val data = rdd.map(x=>JSON.parseObject(x.value()))

      // 每天付费成功的总订单数
      data.map(x=>{
        val time = x.getString("time")
        val day = time.substring(0,8)
        val flag = x.getString("flag")
        val flagRes = if(flag.equals("1")) 1 else 0
        (day,flagRes)
      }).reduceByKey(_+_).collect().foreach(println)

      // 每天付费成功的总订单金额
      data.map(x=>{
        val time = x.getString("time")
        val day = time.substring(0,8)
        val flag = x.getString("flag")
        val fee = if(flag.equals("1")) x.getString("fee").toLong else 0
        (day,fee)
      }).reduceByKey(_+_).collect().foreach(println)
    })


    ssc.start()
    ssc.awaitTermination()
  }

}
