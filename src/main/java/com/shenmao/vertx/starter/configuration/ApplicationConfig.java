package com.shenmao.vertx.starter.configuration;

import com.shenmao.vertx.starter.MtfCrawler.HexunCrawlerParser;
import com.shenmao.vertx.starter.commons.files.MyFileWriter;
import com.shenmao.vertx.starter.database.WikiDatabaseServiceImpl;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;

public class ApplicationConfig {

  static {

    MyFileWriter.getAppResource(WikiDatabaseServiceImpl._USER_ARTICLE_STORE_FOLDER);
    MyFileWriter.getAppResource(HexunCrawlerParser._PAGE_SAVE_FOLDER);
//    MyFileWriter.getAppResource("properties");

  }

  public static String getAppRoot () {

    URL appResource =  ApplicationConfig.class.getClassLoader().getResource("");
    String appResourceString = null;

    if (appResource == null) {
      appResourceString = Paths.get(ApplicationConfig.class.getProtectionDomain().getCodeSource().getLocation().toString()).getParent().getParent() + "";
    } else {

      appResourceString = Paths.get(appResource + "").getParent().getParent().toString() + "";
    }

    return appResourceString.substring(5);
  }

  public enum AppConfig {
    APP_PORT,
    APP_HOST
  }

  private static String _config_file = null;
  private HashMap<AppConfig, String> appConfig = null;
  private Properties appProps = null;
  private static ApplicationConfig _instance;

  public static void setConfigFile(String filePath) {
    _config_file = filePath;
  }

  public static synchronized ApplicationConfig newInstance() throws FileNotFoundException {

    if (_instance == null) {
      _instance = new ApplicationConfig(_config_file);
    }

    return _instance;

  }

  private ApplicationConfig(String configfile) throws FileNotFoundException {

    InputStream queriesInputStream = null;

    try {

      queriesInputStream = getClass().getResourceAsStream(configfile);

      appProps = new Properties();
      appProps.load(queriesInputStream);
      queriesInputStream.close();

    } catch (Exception e) {
      e.printStackTrace();
    }

    loadConfig();

  }

  private void loadConfig() {

    this.appConfig = new HashMap<>();

    appConfig.put(AppConfig.APP_PORT, appProps.getProperty("app_port"));
    appConfig.put(AppConfig.APP_HOST, appProps.getProperty("app_host"));

  }

  public HashMap<AppConfig, String> getConfig() {
    return this.appConfig;
  }

  public String getUrl(boolean isSSL) {
    return "http" + (isSSL ? "s" : "") +"://" +
        this.appConfig.get(AppConfig.APP_HOST) + ":" + this.appConfig.get(AppConfig.APP_PORT);
  }

}
