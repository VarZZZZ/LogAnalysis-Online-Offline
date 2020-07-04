package batch

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SQLContext, SparkSession}

/**
 * @Author: ly
 * @Date: 2020/6/29 20:08
 * @Version 1.0  
 *
 *  测试HBase和Spark整合使用的兼容性
 */
object TestApp {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    val sc = new SparkContext(conf)
    sc.addJar("/home/hadoop/module/te.jar")

    val rdd = sc.parallelize(List(1,2,3,4))
    rdd.collect().foreach(println)
    sc.stop()
  }

}
