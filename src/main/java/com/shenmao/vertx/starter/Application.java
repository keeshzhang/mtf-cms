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


    MyFileWriter.getAppResource(WikiDatabaseServiceImpl._USER_ARTICLE_STORE_FOLDER, false);
    MyFileWriter.getAppResource(HexunCrawlerParser._PAGE_SAVE_FOLDER, false);
    MyFileWriter.getAppResource("/properties", true);

    SqlQueriesConfig.setConfigFile(ApplicationConfig.getAppRoot() +"/properties/db-queries.properties");
    ApplicationConfig.setConfigFile(ApplicationConfig.getAppRoot() +"/properties/application.properties");

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

  public static String getApplicationRoot() {
    return ApplicationConfig.getAppRoot();
  }

}
