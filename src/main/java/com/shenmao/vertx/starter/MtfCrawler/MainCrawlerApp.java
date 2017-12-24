package com.shenmao.vertx.starter.MtfCrawler;

import com.shenmao.vertx.starter.commons.HttpGets;
import com.shenmao.vertx.starter.commons.HttpResult;
import com.shenmao.vertx.starter.commons.files.MyFileWriter;

import java.io.File;

public class MainCrawlerApp {

  // mvn clean package -Dmaven.test.skip=true && java -cp target/vertx-starter-1.0-SNAPSHOT.jar com.shenmao.vertx.starter.MtfCrawler.MainCrawlerApp

  public static void main(String[] args) {

    HexunCrawlerParser.run();

  }

}
