package com.shenmao.vertx.starter.MtfCrawler;

public class MainCrawlerApp {

  // mvn clean package -Dmaven.test.skip=true && java -cp target/vertx-starter-1.0-SNAPSHOT.jar com.shenmao.vertx.starter.MtfCrawler.MainCrawlerApp

  public static void main(String[] args) {

    HexunCrawlerParser.run();

  }

}
