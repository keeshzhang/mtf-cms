package com.shenmao.vertx.starter.database;

import com.shenmao.vertx.starter.commons.files.FindFileVisitor;
import com.shenmao.vertx.starter.configuration.SqlQueriesConfig;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class WikiDatabaseServiceImpl implements WikiDatabaseService {

  private static Logger LOGGER = LoggerFactory.getLogger(WikiDatabaseService.class);

  private final HashMap<SqlQueriesConfig.SqlQuery, String> sqlQueries;
  private final JDBCClient jdbcClient;

  private final String _USER_ARTICLES_FOLDER =
    (WikiDatabaseServiceImpl.class.getClassLoader().getResource("") + "user_articles/").substring(5);


  public WikiDatabaseServiceImpl(JDBCClient jdbcClient, HashMap<SqlQueriesConfig.SqlQuery, String> sqlQueries, Handler<AsyncResult<WikiDatabaseService>> resultHandler) {


    this.jdbcClient = jdbcClient;
    this.sqlQueries = sqlQueries;

    jdbcClient.query(sqlQueries.get(SqlQueriesConfig.SqlQuery.CREATE_PAGES_TABLE), create -> {
      if (create.failed()) {
        LOGGER.error("Database preparation error##WikiDatabaseVerticle.prepareDatabase", create.cause());
        resultHandler.handle(Future.failedFuture(create.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(this));
      }
    });

  }

  @Override
  public WikiDatabaseService fetchAllPages(Handler<AsyncResult<List<JsonObject>>> resultHandler) {

//    boolean path_exists = Files.exists(FileSystems.getDefault().getPath(_USER_ARTICLES_FOLDER),
//                            new LinkOption[]{LinkOption.NOFOLLOW_LINKS});

    Path startingDir = Paths.get(_USER_ARTICLES_FOLDER);
    FindFileVisitor findJavaVisitor = new FindFileVisitor(".xml");

//    System.out.println(new String(Base64.encodeBase64("我的第2个文章".getBytes())) + ", base64");
//    System.out.println(new String(Base64.encodeBase64("我的 第2 _ / # . asdf #$%^&**(!@#$%%)(*&`个文章".getBytes())) + ", base64");

    try {

      Files.walkFileTree(startingDir, findJavaVisitor);

      List<JsonObject> pages = findJavaVisitor.getFilenameList().stream()
          .filter(file -> {
            String _filename = file.replace(_USER_ARTICLES_FOLDER, "");

            System.out.println(_filename + ", _filename");
            return _filename.indexOf('_') != -1 && _filename.indexOf('.') != -1;
          })
          .map(file -> {

            String _filename = file.replace(_USER_ARTICLES_FOLDER, "");
            String createdTimespan = _filename.substring(0, _filename.indexOf('_'));
              String _file_name_base64 = _filename.substring(_filename.indexOf('_') + 1, _filename.indexOf('.'));
            String _file_name_string = _file_name_base64;

            if (Base64.isBase64(_file_name_base64)) {
              _file_name_string = new String(Base64.decodeBase64(_file_name_base64));
            }

            return new JsonObject()
              .put("id", file.replace(_USER_ARTICLES_FOLDER, ""))
              .put("url", "/articles/" + createdTimespan + "/" + _file_name_base64 + ".html")
              .put("name", _file_name_string)
              .put("content", file.replace(_USER_ARTICLES_FOLDER, ""));
          })
          .collect(Collectors.toList());

        resultHandler.handle(Future.succeededFuture(pages));


    } catch (IOException e) {
      resultHandler.handle(Future.failedFuture(e.getCause()));
    }


    return this;

  }



  @Override
  public WikiDatabaseService fetchPage(Long id, Handler<AsyncResult<JsonObject>> resultHandler) {

    JsonArray params = new JsonArray().add(id);

    jdbcClient.queryWithParams(sqlQueries.get(SqlQueriesConfig.SqlQuery.GET_PAGE), params, fetch -> {

      if (fetch.succeeded()) {

        JsonObject response = new JsonObject();
        ResultSet resultSet = fetch.result();

        if (resultSet.getNumRows() == 0) {
          response.put("found", false);
        } else {

          response.put("found", true);

          JsonArray row = resultSet.getResults().get(0);

          response.put("id", row.getInteger(0));
          response.put("title", row.getString(1));
          response.put("rawContent", row.getString(2));
        }

        resultHandler.handle(Future.succeededFuture(response));

      } else {
        LOGGER.error("Database query error", fetch.cause());
        resultHandler.handle(Future.failedFuture(fetch.cause()));
      }

    });

    return this;

  }

  @Override
  public WikiDatabaseService createPage(String title, String markdown, Handler<AsyncResult<Long>> resultHandler) {

    JsonArray _data = new JsonArray()
      .add(title)
      .add(markdown);

    jdbcClient.updateWithParams(sqlQueries.get(SqlQueriesConfig.SqlQuery.CREATE_PAGE), _data, res -> {

      if (res.succeeded()) {

        fetchLastIncrementId( reply -> {
          if (reply.succeeded())
            resultHandler.handle(Future.succeededFuture(reply.result()));
          else {
            LOGGER.error("failed to get last increment id", reply.cause());
            resultHandler.handle(Future.failedFuture(reply.cause()));
          }
        });

      } else {

        LOGGER.error("Database update error", res.cause());
        resultHandler.handle(Future.failedFuture(res.cause()));
      }

    });

    return this;

  }

  @Override
  public WikiDatabaseService savePage(Long id, String title, String markdown, Handler<AsyncResult<Void>> resultHandler) {


    JsonArray data = new JsonArray()
      .add(title)
      .add(markdown)
      .add(id);

    jdbcClient.updateWithParams(sqlQueries.get(SqlQueriesConfig.SqlQuery.SAVE_PAGE), data, res -> {

      if (res.succeeded()) {
        resultHandler.handle(Future.succeededFuture());
      } else {
        LOGGER.error("Database save error", res.cause());
        resultHandler.handle(Future.failedFuture(res.cause()));
      }

    });

    return this;

  }

  @Override
  public WikiDatabaseService deletePage(Long id, Handler<AsyncResult<Void>> resultHandler) {

    JsonArray data = new JsonArray().add(id);

    jdbcClient.updateWithParams(sqlQueries.get(SqlQueriesConfig.SqlQuery.DELETE_PAGE), data, res -> {
      if (res.succeeded()) {
        resultHandler.handle(Future.succeededFuture());
      } else {
        LOGGER.error("Database delete error", res.cause());
        resultHandler.handle(Future.failedFuture(res.cause()));
      }
    });

    return this;
  }

  @Override
  public WikiDatabaseService fetchLastIncrementId(Handler<AsyncResult<Long>> resultHandler) {

    jdbcClient.query(sqlQueries.get(SqlQueriesConfig.SqlQuery.LAST_INSERT_ID), res -> {

      if (res.succeeded()) {
        resultHandler.handle(Future.succeededFuture(res.result().getResults().get(0).getLong(0)));
      } else {
        resultHandler.handle(Future.failedFuture(res.cause()));
      }
    });

    return this;

  }

}
