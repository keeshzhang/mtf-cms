package com.shenmao.vertx.starter;

import com.shenmao.vertx.starter.MtfCrawler.HexunCrawlerParser;
import com.shenmao.vertx.starter.commons.files.MyFileWriter;
import com.shenmao.vertx.starter.configuration.ApplicationConfig;
import com.shenmao.vertx.starter.configuration.SqlQueriesConfig;
import com.shenmao.vertx.starter.database.WikiDatabaseServiceImpl;

import java.io.FileNotFoundException;
import java.util.HashMap;

public class Application {

  static SqlQueriesConfig sqlQueriesConfig;
  static ApplicationConfig applicationConfig;

  static {

//    System.out.println(ApplicationConfig.getAppRoot() + "/properties/db-queries.properties");

//    ApplicationConfig.getAppRoot()
    SqlQueriesConfig.setConfigFile("/properties/db-queries.properties");
    ApplicationConfig.setConfigFile("/properties/application.properties");

    try {
      applicationConfig = ApplicationConfig.newInstance();
      sqlQueriesConfig = SqlQueriesConfig.newInstance();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

  }

  public static ApplicationConfig applicationConfigInstance() {
    return applicationConfig;
  }

  public static HashMap<ApplicationConfig.AppConfig, String> getAppConfig() {
    return applicationConfig.getConfig();

  }

  public static HashMap<SqlQueriesConfig.SqlQuery, String> getSqlQueriesConfig() {
    return sqlQueriesConfig.getQueries();

  }

}
