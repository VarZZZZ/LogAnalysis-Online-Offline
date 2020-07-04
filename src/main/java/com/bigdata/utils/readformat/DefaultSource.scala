//package com.imooc.bigdata.utils.readformat
//
//import org.apache.spark.sql.SQLContext
//import org.apache.spark.sql.sources.{BaseRelation, RelationProvider, SchemaRelationProvider}
//import org.apache.spark.sql.types.StructType
//
///**
// * @Author: ly
// * @Date: 2020/6/29 23:05
// * @Version 1.0
// *
// */
//class DefaultSource
//  extends RelationProvider
//    with SchemaRelationProvider {
//  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String]): BaseRelation = {
//    createRelation(sqlContext,parameters,null)
//  }
//
//  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String], schema: StructType): BaseRelation = {
//    val path=parameters.get("path")
//    path match {
//      case Some(p) => new IPDataSourceRelation(sqlContext,p,schema)
//      case _ => throw new IllegalArgumentException("Path is required for custom-datasource format!!")
//    }
//  }
//}
