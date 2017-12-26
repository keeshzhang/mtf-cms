package com.shenmao.vertx.starter.MtfCrawler;

import com.shenmao.vertx.starter.Application;
import com.shenmao.vertx.starter.configuration.ApplicationConfig;

public class MainCrawlerApp {

  // mvn clean package -Dmaven.test.skip=true && java -cp target/vertx-starter-1.0-SNAPSHOT.jar com.shenmao.vertx.starter.MtfCrawler.MainCrawlerApp

  public static void main(String[] args) {

    String port = Application.applicationConfigInstance().getConfig().get(ApplicationConfig.AppConfig.APP_PORT);
    System.out.println(port + ", port");
    System.out.println(Application.getApplicationRoot() + ", ApplicationConfig.getAppRoot()");

    HexunCrawlerParser.run(null);

    for (int i=1234; i>1000; i--) {
      HexunCrawlerParser.run(i);
    }


  }

}
