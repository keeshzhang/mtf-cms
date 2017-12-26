package com.shenmao.vertx.starter;

import com.shenmao.vertx.starter.MtfCrawler.HexunCrawlerParser;
import com.shenmao.vertx.starter.commons.files.MyFileWriter;
import com.shenmao.vertx.starter.configuration.ApplicationConfig;
import com.shenmao.vertx.starter.configuration.SqlQueriesConfig;
import com.shenmao.vertx.starter.database.WikiDatabaseServiceImpl;

import java.io.FileNotFoundException;
import java.util.HashMap;

public class Application {

  static String _APP_ENV = "prod";
  static SqlQueriesConfig sqlQueriesConfig;
  static ApplicationConfig applicationConfig;

  static {


    MyFileWriter.getAppResource(WikiDatabaseServiceImpl._USER_ARTICLE_STORE_FOLDER, false);
    MyFileWriter.getAppResource(HexunCrawlerParser._PAGE_SAVE_FOLDER, false);
    MyFileWriter.getAppResource("/properties", true);

    if (System.getenv().containsKey("MTF_APP_ENV")) {
      _APP_ENV = System.getenv().get("MTF_APP_ENV");
    }


    SqlQueriesConfig.setConfigFile(ApplicationConfig.getAppRoot() +"/properties/db-queries.properties");
    ApplicationConfig.setConfigFile(ApplicationConfig.getAppRoot() +"/properties/application_" + _APP_ENV + ".properties");

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

  public static String getAppEndpoint() {
    return ("true".equals(applicationConfig.getConfig().get(ApplicationConfig.AppConfig.APP_SSL_ENABLED)) ? "https" : "http")
            + "://" + applicationConfig.getConfig().get(ApplicationConfig.AppConfig.APP_HOST) + ":"
            +  applicationConfig.getConfig().get(ApplicationConfig.AppConfig.APP_PORT);
  }

}
