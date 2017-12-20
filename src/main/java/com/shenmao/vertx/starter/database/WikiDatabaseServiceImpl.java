package com.shenmao.vertx.starter.database;

import com.shenmao.vertx.starter.commons.files.FindFileVisitor;
import com.shenmao.vertx.starter.commons.string.StringHelper;
import com.shenmao.vertx.starter.configuration.SqlQueriesConfig;
import com.sun.jmx.snmp.Timestamp;
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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    System.out.println(new String(Base64.encodeBase64("我的第2 _ / # . asdf #$%^&**(!@#$%%)(*&`个文章".getBytes())) + ", base64");

    System.out.println(new Timestamp(System.currentTimeMillis()).getDateTime() + ", new Timestamp(System.currentTimeMillis())");

    try {

      Files.walkFileTree(startingDir, findJavaVisitor);

      List<JsonObject> pages = findJavaVisitor.getFilenameList().stream()
          .filter(file -> {
            String _filename = file.replace(_USER_ARTICLES_FOLDER, "");
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

            String _file_name_escape = StringHelper.escape(_file_name_string);
            String _article_status = _filename.substring(_filename.indexOf('.') + 1, _filename.lastIndexOf('.'));

            return new JsonObject()
              .put("id", file.replace(_USER_ARTICLES_FOLDER, ""))
              .put("name", _file_name_string)
              .put("name_base64", _file_name_base64)

              .put("file_name", createdTimespan + "_" + _file_name_base64 + "."+_article_status+".xml")

              .put("url", "/articles/" + createdTimespan + "/" + _file_name_escape)
              .put("url_base64", "/articles/" + createdTimespan + "/" + _file_name_base64)
              .put("file_path", "/articles/" + createdTimespan + "_" + _file_name_base64 + "."+_article_status+".xml")
              .put("content", file.replace(_USER_ARTICLES_FOLDER, ""))
              .put("created_at", createdTimespan)
              .put("keywords", "keywords big data, cloud computing")
              .put("type", "big data, cloud computing")
              .put("tags", "aws, linux, vagrant, centos")
              .put("description", "文章描述: some description here ");
          })
          .collect(Collectors.toList());

        resultHandler.handle(Future.succeededFuture(pages));


    } catch (IOException e) {
      resultHandler.handle(Future.failedFuture(e.getCause()));
    }


    return this;

  }

  public WikiDatabaseService exists(Long id, String articleName, Handler<AsyncResult<JsonObject>> resultHandler) {

    System.out.println( articleName + ",, articleName");

    this.fetchAllPages(result -> {

      if (result.succeeded()) {

        Stream<JsonObject> fileStream = result.result().stream().filter(article -> {
          return article.getString("file_name").startsWith(id + "_" + articleName);
        });

        Optional<JsonObject> fileOptional = fileStream.findFirst();

        try {
          resultHandler.handle(Future.succeededFuture(fileOptional != null ? fileOptional.get() : null));
        } catch (Exception e) {
          resultHandler.handle(Future.failedFuture(e.getCause()));
        }


      } else {

      }

    });

    return this;
  }

  @Override
  public WikiDatabaseService fetchPage(Long id, String articleName, Handler<AsyncResult<JsonObject>> resultHandler) {

    String articleNameDecode = articleName;

    if (Base64.isBase64(articleName)) {
      articleNameDecode = new String(Base64.decodeBase64(articleName));
    }

    this.exists(id, articleName, reply -> {

      if (reply.succeeded()) {

        resultHandler.handle(Future.succeededFuture(reply.result()));
      } else {
        resultHandler.handle(Future.succeededFuture(null));
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
