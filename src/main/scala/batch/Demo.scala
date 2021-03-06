package batch

import java.util.Locale

import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

/**
 * @Author: ly
 * @Date: 2020/6/29 20:52
 * @Version 1.0  
 *
 */
object Demo {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("LogApp").master("local").getOrCreate()


    System.setProperty("icode","77A5630C2BD7279E")
    // 用val logDF无法修改
    var logDF = spark.read.format("com.imooc.bigdata.spark.pk").option("path","D:\\project\\LogAnalysis-Online-Offline\\src\\main\\resources\\data\\test-access.log")
      .load()

    //logDF.show(false)
    import org.apache.spark.sql.functions._
    def formatTime() = udf((time:String)=>{
      FastDateFormat.getInstance("yyyyMMddHHmm").format(FastDateFormat.getInstance("dd/MMM/yyyy:HH:mm:ss Z",Locale.ENGLISH)
        .parse(time.substring(time.indexOf("[")+1,time.lastIndexOf("]"))).getTime)
    })
    logDF = logDF.withColumn("formattime",formatTime()(logDF("time")))
    logDF.show(false)
    spark.stop()

  }

}
