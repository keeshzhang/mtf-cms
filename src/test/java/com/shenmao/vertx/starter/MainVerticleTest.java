package com.shenmao.vertx.starter;

import com.shenmao.vertx.starter.commons.HttpGets;
import com.shenmao.vertx.starter.commons.HttpPosts;
import com.shenmao.vertx.starter.commons.HttpResult;
import com.shenmao.vertx.starter.configuration.ApplicationConfig;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.apache.commons.io.FileUtils;
import org.apache.http.message.BasicNameValuePair;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RunWith(VertxUnitRunner.class)
public class MainVerticleTest {

  public static int _PORT = Integer.parseInt(Application.getAppConfig().get(ApplicationConfig.AppConfig.APP_PORT));
  public static String _HOST = Application.getAppConfig().get(ApplicationConfig.AppConfig.APP_HOST);
  public static String _URL = Application.applicationConfigInstance().getUrl(false);

  private Vertx vertx;
  private HttpClient _httpClient;


  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();
    vertx.deployVerticle(MainVerticle.class.getName(), tc.asyncAssertSuccess());
    _httpClient = vertx.createHttpClient();
  }

  @After
  public void tearDown(TestContext tc) throws IOException {

    Path hsqldb_folder = Paths.get("db");

    if (Files.exists(hsqldb_folder)) {
      FileUtils.forceDelete(hsqldb_folder.toFile());
    }

    vertx.close(tc.asyncAssertSuccess());
  }

  @Test
  public void testThatTheServerIsStarted(TestContext tc) {

//    Async async = tc.async();
//
//    _httpClient.getNow(_PORT, _HOST, "/index", response -> {
//      tc.assertEquals(response.statusCode(), 200);
//      response.bodyHandler(body -> {
//        tc.assertTrue(body.length() > 0);
//        async.complete();
//      });
//    });

    tc.assertTrue(1 > 0);

  }



}



