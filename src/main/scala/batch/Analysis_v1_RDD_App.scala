package batch

import batch.LogETLApp.createTable
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.CellUtil
import org.apache.hadoop.hbase.client.{Result, Scan}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.protobuf.ProtobufUtil
import org.apache.hadoop.hbase.util.{Base64, Bytes}
import org.apache.spark.sql.SparkSession

/**
 * @Author: ly
 * @Date: 2020/6/30 19:35
 * @Version 1.0  
 *
 *  利用Spark RDD 来处理
 */
object Analysis_v1_RDD_App {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.hadoop.validateOutputSpecs", value = false)
      .master("local[2]").appName("LogETLApp")
      .getOrCreate()

    val day = "20190130"

    val conf = new Configuration()
    conf.set("hbase.rootdir", "hdfs://master:9000/HBase")
    conf.set("hbase.zookeeper.quorum", "master:2181,slave1:2181,slave2:2181")
    val tableName = "access_" + day
    conf.set(TableInputFormat.INPUT_TABLE, tableName)

    val scan = new Scan()

    scan.addFamily(Bytes.toBytes("o"))

    scan.addColumn(Bytes.toBytes("o"), Bytes.toBytes("country"))
    scan.addColumn(Bytes.toBytes("o"), Bytes.toBytes("province"))
    scan.addColumn(Bytes.toBytes("o"),Bytes.toBytes("browsername"))
    conf.set(TableInputFormat.SCAN,
      Base64.encodeBytes(ProtobufUtil.toScan(scan).toByteArray))

    val hbaseRDD = spark.sparkContext.newAPIHadoopRDD(
      conf,
      classOf[TableInputFormat],
      classOf[ImmutableBytesWritable],
      classOf[Result]
    )
//    hbaseRDD.take(10).foreach(x=>{
//      val rowKey = Bytes.toString(x._1.get())
//      for(cell <- x._2.rawCells()){
//        val cf = Bytes.toString(CellUtil.cloneFamily(cell))
//        val qualifier = Bytes.toString(CellUtil.cloneQualifier(cell))
//        val value = Bytes.toString(CellUtil.cloneValue(cell))
//
//        println(s"$rowKey : $cf : $qualifier : $value")
//      }
//    })

    hbaseRDD.cache()

    // 统计每个国家每个省份的访问量  ==》 TOP10 ; 排序和mapreduce辅助排序一致
    hbaseRDD.map(x =>{
      val country = Bytes.toString(x._2.getValue("o".getBytes,"country".getBytes))
      val province = Bytes.toString(x._2.getValue("o".getBytes,"province".getBytes))
      ((country,province),1)
    }).reduceByKey(_+_)
      .map(x=>(x._2,x._1)).sortByKey(ascending = false)   // (k ,v)= >(v,k)
      .map(x=>(x._2,x._1)).take(10).foreach(println)
    //((中国,福建省),4)
    //((中国,广东省),2)
    //((中国,北京市),2)
    //((乌克兰,unknown),1)
    //((中国,浙江省),1)

    println("------------------------------")
    // 统计不同浏览器的访问量
    hbaseRDD.map(x=>{
      val browsername = Bytes.toString(x._2.getValue("o".getBytes,"browsername".getBytes))
      (browsername,1)
    }).reduceByKey(_+_)
      .map(x=>(x._2,x._1)).sortByKey(ascending = false)   // (k ,v)= >(v,k)
      .map(x=>(x._2,x._1)).take(10).foreach(println)
    //(Sogou Explorer,4)
    //(unknown,3)
    //(Chrome,2)
    //(sogou spider,1)


    hbaseRDD.unpersist(true)
    spark.stop()
  }

}
