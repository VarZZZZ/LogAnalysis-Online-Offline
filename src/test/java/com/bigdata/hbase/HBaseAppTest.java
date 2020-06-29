package com.bigdata.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HBaseAppTest {

    Connection connection = null;
    Table table = null;
    Admin admin = null;

    String tableName = "hbase_api_test";

    @Before
    public void setUp() throws Exception{
        Configuration conf = new Configuration();
        conf.set("hbase.rootdir","hdfs://master:9000/HBase");
        conf.set("hbase.zookeeper.quorum","master:2181,slave1:2181,slave2:2181");
        try{
            connection = ConnectionFactory.createConnection(conf);
            admin = connection.getAdmin();

            Assert.assertNotNull(connection);
            Assert.assertNotNull(admin);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Test
    public void getConnection(){
    }
    @Test
    public void queryTableInfos() throws IOException {
        HTableDescriptor[] tables = admin.listTables();
        for(HTableDescriptor t:tables){
            System.out.println(t.getNameAsString());

            HColumnDescriptor[] columnFamilies = t.getColumnFamilies();
            for (HColumnDescriptor columnFamily : columnFamilies) {
                System.out.println("\t"+columnFamily.getNameAsString());
            }
        }

    }
    @Test
    public void createTable() throws IOException {
        TableName table = TableName.valueOf(tableName);
        TableName[] tableNames = admin.listTableNames();
        for(TableName t:tableNames){
            System.out.println(t);
        }
        if(admin.tableExists(table)){
            System.out.println(tableName+"   exists");
        }else{
            HTableDescriptor descriptor = new HTableDescriptor(table);
            descriptor.addFamily(new HColumnDescriptor("info"));
            descriptor.addFamily(new HColumnDescriptor("address"));
            admin.createTable(descriptor);
            System.out.println(tableName+"  建立好了");
        }
    }

    @Test
    public void testPut() throws IOException {
        table = connection.getTable(TableName.valueOf(tableName));
        Put put = new Put(Bytes.toBytes("pk"));

        put.addColumn(Bytes.toBytes("info"),Bytes.toBytes("age"),Bytes.toBytes("28"));
        put.addColumn(Bytes.toBytes("info"),Bytes.toBytes("bir"),Bytes.toBytes("28"));
        put.addColumn(Bytes.toBytes("info"),Bytes.toBytes("company"),Bytes.toBytes("28"));
        put.addColumn(Bytes.toBytes("address"),Bytes.toBytes("country"),Bytes.toBytes("28"));
        put.addColumn(Bytes.toBytes("address"),Bytes.toBytes("colName"),Bytes.toBytes("28"));

        // list puts 添加多个rowkey
        table.put(put);
    }
    @Test
    public void testPutList() throws IOException{
        table  = connection.getTable(TableName.valueOf(tableName));

        List<Put> puts = new ArrayList<Put>();

        Put put1 = new Put(Bytes.toBytes("liangy"));
        put1.addColumn(Bytes.toBytes("info"),Bytes.toBytes("age"),Bytes.toBytes("25"));
        put1.addColumn(Bytes.toBytes("info"), Bytes.toBytes("birthday"), Bytes.toBytes("xxxx"));
        put1.addColumn(Bytes.toBytes("info"), Bytes.toBytes("company"), Bytes.toBytes("bigdata"));
        put1.addColumn(Bytes.toBytes("address"), Bytes.toBytes("country"), Bytes.toBytes("CN"));
        put1.addColumn(Bytes.toBytes("address"), Bytes.toBytes("province"), Bytes.toBytes("ZHEJIANG"));
        put1.addColumn(Bytes.toBytes("address"), Bytes.toBytes("city"), Bytes.toBytes("HANGZHOU"));

        Put put2 = new Put(Bytes.toBytes("Bigdata"));
        put2.addColumn(Bytes.toBytes("info"),Bytes.toBytes("age"),Bytes.toBytes("40"));
        put2.addColumn(Bytes.toBytes("info"), Bytes.toBytes("birthday"), Bytes.toBytes("xxxx"));
        put2.addColumn(Bytes.toBytes("info"), Bytes.toBytes("company"), Bytes.toBytes("apple"));
        put2.addColumn(Bytes.toBytes("address"), Bytes.toBytes("country"), Bytes.toBytes("CN"));
        put2.addColumn(Bytes.toBytes("address"), Bytes.toBytes("province"), Bytes.toBytes("SHANGHAI"));
        put2.addColumn(Bytes.toBytes("address"), Bytes.toBytes("city"), Bytes.toBytes("SHANGHAI"));

        puts.add(put1);
        puts.add(put2);
        table.put(puts);
    }

    @Test
    public void testUpdate() throws IOException {
        table = connection.getTable(TableName.valueOf(tableName));
        Put put =  new Put(Bytes.toBytes("Bigdata"));
        put.addColumn(Bytes.toBytes("info"),Bytes.toBytes("age"),Bytes.toBytes("50"));
        table.put(put);
    }

    @Test
    public void testGet() throws IOException {
        table = connection.getTable(TableName.valueOf(tableName));

        Get get = new Get(Bytes.toBytes("liangy"));
        Result result = table.get(get);
        printRes(result);
    }

    @Test
    public void testScan() throws IOException {
        table = connection.getTable(TableName.valueOf(tableName));
        Scan scan = new Scan(new Get(Bytes.toBytes("liangy")));
        scan.addColumn(Bytes.toBytes("info"),Bytes.toBytes("company"));
        ResultScanner rs = table.getScanner(scan);

        for (Result r : rs) {
            printRes(r);
            System.out.println("--------------");
        }

    }
    @Test
    public void testFilter() throws IOException {
        table = connection.getTable(TableName.valueOf(tableName));
        Scan scan = new Scan();
//        String reg = "^*ng";
//        Filter filter = new RowFilter(CompareFilter.CompareOp.EQUAL, new RegexStringComparator(reg));
//        scan.setFilter(filter);
        FilterList filters = new FilterList(FilterList.Operator.MUST_PASS_ONE);
        Filter f1 = new PrefixFilter("l".getBytes());
        Filter f2 = new PrefixFilter("p".getBytes());

        filters.addFilter(f1);
        filters.addFilter(f2);
        scan.setFilter(filters);

        ResultScanner rs = table.getScanner(scan);
        for (Result r : rs) {
            printRes(r);
            System.out.println("-----------");
        }
    }


    private void printRes(Result result){
        for (Cell cell : result.rawCells()) {
            System.out.println(Bytes.toString(result.getRow())+"\t"
                                +Bytes.toString(CellUtil.cloneFamily(cell))+"\t"
                                + Bytes.toString(CellUtil.cloneQualifier(cell))+"\t"
                                + Bytes.toString(CellUtil.cloneValue(cell))+"\t"
                                + cell.getTimestamp());
        }
    }

    @After
    public void tearDown() throws IOException {
        connection.close();
    }

}
