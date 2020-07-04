package batch

import java.sql.DriverManager

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.{Result, Scan}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.protobuf.ProtobufUtil
import org.apache.hadoop.hbase.util.{Base64, Bytes}
import org.apache.spark.sql.SparkSession

import scala.util.{Failure, Success, Try}

/**
 * @Author: ly
 * @Date: 2020/7/3 19:54
 * @Version 1.0  
 *
 */
object AnalysisToMysql {
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

    hbaseRDD.cache()

    import spark.implicits._

    // 统计每个国家每个省份的访问量  ==》 TOP10 ; 排序和mapreduce辅助排序一致
    val cp_df = hbaseRDD.map(x => {
      val country = Bytes.toString(x._2.getValue("o".getBytes, "country".getBytes))
      val province = Bytes.toString(x._2.getValue("o".getBytes, "province".getBytes))
      CountryProvince(country, province)
    }).toDF
    cp_df.createOrReplaceTempView("country_province")
    spark.sql("select country,province, count(*) as ct from country_province group by country,province order by ct desc").show()
    //+-------+--------+---+
    //|   中国|  福建省|  4|
    //| 乌克兰| unknown|  1|
    //|   中国|  北京市|  2|
    //|   中国|  浙江省|  1|
    //|   中国|  广东省|  2|
    //+-------+--------+---+

    println("---------------------------------------")
    val resultRDD = hbaseRDD.map(x => {
      val browsername = Bytes.toString(x._2.getValue("o".getBytes, "browsername".getBytes))
      //Browser(browsername)
      (browsername, 1)
    }).reduceByKey(_ + _)

    resultRDD.collect().foreach(println)

    // 如果分区数多，那么会建立过多的connection;
    // 那减小一些比较好
    resultRDD.coalesce(2).foreachPartition(part=>{
      Try{
        val connection = {
          Class.forName("com.mysql.jdbc.Driver")
          val url = "jdbc:mysql://master:3306/spark?characterEncoding=UTF8"
          val user="root"
          val password = "1234"
          DriverManager.getConnection(url,user,password)
        }
        val preAutoCommit = connection.getAutoCommit
        connection.setAutoCommit(false)
        val sql = "insert into browser_stat(day,browser,cnt) values(?,?,?)"
        val pstmt = connection.prepareStatement(sql)

        pstmt.addBatch(s"delete * from browser_stat where day=$day") // mysql中没设置主键，也不必要设置(也可以设置主键)，数据重跑时直接删除

        part.foreach(x=>{
          pstmt.setString(1,day)
          pstmt.setString(2,x._1)
          pstmt.setInt(3,x._2)

          pstmt.addBatch()
        })
        pstmt.executeBatch()
        connection.commit()
        (connection,preAutoCommit) // 需要再连接断开后，还原preAutoCommit
      }match{
        case Success((connection,preAutoCommit)) =>
          connection.setAutoCommit(preAutoCommit)
          if(null != connection) connection.close()
        case Failure(exception)=> throw exception
      }
    })


    spark.stop()
  }

  case class CountryProvince(country:String,province:String)
  case class Browser(browsername:String)


}
