package com.shenmao.vertx.starter.database;

import com.shenmao.vertx.starter.configuration.SqlQueriesConfig;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

@ProxyGen
public interface WikiDatabaseService {

  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
  public static final SimpleDateFormat DATE_FORMAT_MONTH = new SimpleDateFormat("yyyyMMdd");

  static WikiDatabaseService create(JDBCClient jdbcClient, HashMap<SqlQueriesConfig.SqlQuery, String> sqlQueries, Handler<AsyncResult<WikiDatabaseService>> resultHandler) {
    return new WikiDatabaseServiceImpl(jdbcClient, sqlQueries, resultHandler);
  }

  static WikiDatabaseService createProxy(Vertx vertx, String address) {
    return new WikiDatabaseServiceVertxEBProxy(vertx, address);
  }

    @Fluent
  WikiDatabaseService fetchAllPagesCondition(Integer start, Handler<AsyncResult<List<JsonObject>>> resultHandler);


  @Fluent
  WikiDatabaseService fetchPage(Long id, String articleName, Handler<AsyncResult<JsonObject>> resultHandler);

  @Fluent
  WikiDatabaseService createPage(Long timestamp, JsonObject articleObject, Handler<AsyncResult<JsonObject>> resultHandler);

  @Fluent
  WikiDatabaseService savePage(Long timestamp, String articleFileName, JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler);

  @Fluent
  WikiDatabaseService deletePage(Long id, Handler<AsyncResult<Void>> resultHandler);

  @Fluent
  WikiDatabaseService fetchLastIncrementId(Handler<AsyncResult<Long>> resultHandler);

}
