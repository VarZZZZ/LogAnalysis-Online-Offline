# LogAnalysis-Online-Offline

## 大数据网站日志分析-离线-实时处理
### 所用技术
Spark+HBase+Redis+Kafka+Flume

###Spark-集群运行命令

$SPARK_HOME/bin/spark-submit \
--class      \
--master yarn \
--name LogETLApp \
--conf "spark.serializer=org.apache.spark.serializer.KryoSerializer" \
--jars $(echo $HBASE_HOME/lib/*.jar | tr ' ' ',') \
--packages org.scalaj:scalaj-http_2.11:2.4.1 \
--jar 




### mysql 
create table if not exists browser_stat(
    day varchar(10) not null,
    browser varchar(100) not null,
    cnt int
    ) engine=innodb default charset=utf8;
    


### 过程
RDD通过JDBC可以写入Mysql
DataFrame dataSet的数据通过format("jdbc")直接写入MySQL是否更好？