package com.shenmao.vertx.starter.MtfCrawler;

import com.shenmao.vertx.starter.Application;
import com.shenmao.vertx.starter.configuration.ApplicationConfig;

public class MainCrawlerApp {

  // mvn clean package -Dmaven.test.skip=true && java -cp target/vertx-starter-1.0-SNAPSHOT.jar com.shenmao.vertx.starter.MtfCrawler.MainCrawlerApp

  public static void main(String[] args) {

    System.out.println(Application.getApplicationRoot() + ", ApplicationConfig.getAppRoot()");
    HexunCrawlerParser.run();

  }

}
