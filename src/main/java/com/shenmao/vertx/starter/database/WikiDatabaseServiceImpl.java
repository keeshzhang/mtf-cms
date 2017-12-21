package com.shenmao.vertx.starter.database;

import com.shenmao.vertx.starter.commons.encode.Base64;
import com.shenmao.vertx.starter.commons.files.FindFileVisitor;
import com.shenmao.vertx.starter.commons.string.StringHelper;
import com.shenmao.vertx.starter.configuration.SqlQueriesConfig;
import com.sun.jmx.snmp.Timestamp;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

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

  public void updateArticleXml(String url,String url_escape,String title, String file_path) {

    SAXReader reader = new SAXReader();

    try {

      Document masterDocument = reader.read(_USER_ARTICLES_FOLDER + "/" + file_path);

      Node articleUrlNode = masterDocument.selectSingleNode("//article/url");
      Node articleUrlEscapeNode = masterDocument.selectSingleNode("//article/url_escape");
      Node articleTitleNode = masterDocument.selectSingleNode("//article/title");
      Node articleFilePathNode = masterDocument.selectSingleNode("//article/file_path");

      articleUrlNode.setText(url);
      articleUrlEscapeNode.setText(url_escape);
      articleTitleNode.setText("<![CDATA[" + title + "]]>");
      articleFilePathNode.setText(file_path);

      try (FileWriter fw = new FileWriter(file_path, false)) {
        masterDocument.write(fw);
      } catch (IOException e) {
        e.printStackTrace();
      }

    } catch (DocumentException e) {
      e.printStackTrace();
    }

  }


  public JsonObject readArticleXml(String filepath) {

    SAXReader reader = new SAXReader();

    try {

      Document masterDocument = reader.read(filepath);

      String articleUrl = null;
      String articleUrlEscape = null;
      String articleTitle = null;
      String articleFilePath = null;
      String articleStatus = null;
      String articleType = null;
      String articleTags = null;
      String articlePutTop = null;
      String articleAuthors = null;
      String articleCreatedAt = null;
      String articleLastUpdated = null;
      String articleChannel = null;
      String articleKeywords = null;
      String articleDescription = null;
      String articleHtmlContent = null;

      Node articleUrlNode = masterDocument.selectSingleNode("//article/url");
      Node articleUrlEscapeNode = masterDocument.selectSingleNode("//article/url_escape");
      Node articleTitleNode = masterDocument.selectSingleNode("//article/title");
      Node articleFilePathNode = masterDocument.selectSingleNode("//article/file_path");
      Node articleStatusNode = masterDocument.selectSingleNode("//article/article_status");
      Node articleTypeNode = masterDocument.selectSingleNode("//article/type");
      Node articleTagsNode = masterDocument.selectSingleNode("//article/tags");
      Node articleputTopNode = masterDocument.selectSingleNode("//article/put_top");
      Node articleAuthorsNode = masterDocument.selectSingleNode("//article/authors");
      Node articlecreatedAtNode = masterDocument.selectSingleNode("//article/created_at");
      Node articleLastUpdatedNode = masterDocument.selectSingleNode("//article/last_updated");
      Node articleChannelNode = masterDocument.selectSingleNode("//article/channel");
      Node articleKeywordsNode = masterDocument.selectSingleNode("//article/keywords");
      Node articledescriptionNode = masterDocument.selectSingleNode("//article/description");
      Node articleHtmlContentNode = masterDocument.selectSingleNode("//article/html_content");

      if(articleUrlNode != null) articleUrl = articleUrlNode.getText();
      if(articleUrlEscapeNode != null) articleUrlEscape = articleUrlEscapeNode.getText();
      if(articleTitleNode != null) articleTitle = articleTitleNode.getText();
      if(articleFilePathNode != null) articleFilePath = articleFilePathNode.getText();
      if(articleStatusNode != null) articleStatus = articleStatusNode.getText();
      if(articleTypeNode != null) articleType = articleTypeNode.getText();
      if(articleTagsNode != null) articleTags = articleTagsNode.getText();
      if(articleputTopNode != null) articlePutTop = articleputTopNode.getText();
      if(articleAuthorsNode != null) articleAuthors = articleAuthorsNode.getText();
      if(articlecreatedAtNode != null) articleCreatedAt = articlecreatedAtNode.getText();
      if(articleLastUpdatedNode != null) articleLastUpdated = articleLastUpdatedNode.getText();
      if(articleChannelNode != null) articleChannel = articleChannelNode.getText();
      if(articleKeywordsNode != null) articleKeywords = articleKeywordsNode.getText();
      if(articledescriptionNode != null) articleDescription = articledescriptionNode.getText();
      if(articleHtmlContentNode != null) articleHtmlContent = articleHtmlContentNode.getText();

      JsonObject restult = newArticleObject( articleUrl, articleUrlEscape, articleTitle, articleFilePath, articleStatus, articleType, articleTags, articlePutTop, articleAuthors, articleCreatedAt, articleLastUpdated, articleChannel, articleKeywords, articleDescription, articleHtmlContent);


//      System.out.println(restult.encode() + ", readArticleXml##articleTitle");

      return restult;

    } catch (DocumentException e) {
      e.printStackTrace();
    }

    return null;

  }

  @Override
  public WikiDatabaseService fetchAllPages(Handler<AsyncResult<List<JsonObject>>> resultHandler) {

//    boolean path_exists = Files.exists(FileSystems.getDefault().getPath(_USER_ARTICLES_FOLDER),
//                            new LinkOption[]{LinkOption.NOFOLLOW_LINKS});

    Path startingDir = Paths.get(_USER_ARTICLES_FOLDER);
    FindFileVisitor findJavaVisitor = new FindFileVisitor(".xml");

//    System.out.println(new String(Base64.encodeBase64("我的第2个文章".getBytes())) + ", base64");
    // System.out.println(new String(Base64.encodeBase64("我的第2 _ / # . asdf #$%^&**(!@#$%%)(*&`个文章".getBytes())) + ", base64");
    // System.out.println(new Timestamp(System.currentTimeMillis()).getDateTime() + ", new Timestamp(System.currentTimeMillis())");

    try {

      Files.walkFileTree(startingDir, findJavaVisitor);

      List<JsonObject> pages = findJavaVisitor.getFilenameList().stream()
          .filter(file -> {
            String _filename = file.replace(_USER_ARTICLES_FOLDER, "");
            return _filename.indexOf('_') != -1 && _filename.indexOf('.') != -1;
          }).map( file -> {

            return readArticleXml(file);

          })
          .collect(Collectors.toList());

        resultHandler.handle(Future.succeededFuture(pages));


    } catch (IOException e) {
      resultHandler.handle(Future.failedFuture(e.getCause()));
    }


    return this;

  }

  public JsonObject newArticleObject(String articleUrl,
                                      String articleUrlEscape,
                                      String articleTitle,
                                      String articleFilePath,
                                      String articleStatus,
                                      String articleType,
                                      String articleTags,
                                      String articlePutTop,
                                      String articleAuthors,
                                      String articleCreatedAt,
                                      String articleLastUpdated,
                                      String articleChannel,
                                      String articleKeywords,
                                      String articledescription,
                                      String articleHtmlContent) {

    return new JsonObject()
      .put("id", StringHelper.escape(articleTitle))
      .put("title", articleTitle)
      .put("name", StringHelper.escape(articleTitle))
      .put("name_base64", Base64.encode(StringHelper.escape(articleTitle)))
      .put("url", articleUrl)
      .put("url_escape", articleUrlEscape)
      .put("file_name", Paths.get(articleFilePath).toFile().getName())
      .put("file_path", articleFilePath)
      .put("article_status", articleStatus)
      .put("put_top", articlePutTop)
      .put("authors", articleAuthors)
      .put("last_updated", articleLastUpdated)
      .put("channel", articleChannel)
      .put("html_content", articleHtmlContent)
      .put("created_at", articleCreatedAt)
      .put("keywords", articleKeywords)
      .put("type", articleType)
      .put("tags", articleTags)
      .put("description", articledescription);
  }

  public WikiDatabaseService exists(Long id, String articleName, Handler<AsyncResult<JsonObject>> resultHandler) {


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

  public String getFileFullName(Long timestamp, String articleFileName, String status) {

    return _USER_ARTICLES_FOLDER + "/"
          + timestamp + "_" + articleFileName + "."
          + (status == null ? "pending" : status) + ".xml";
  }

  public String getArticleUrl(Long timestamp, String articleFileNameBase64) {
    return "/articles/" + timestamp + "/" + articleFileNameBase64;
  }

  public JsonObject refreshArticleObject(JsonObject article) {

    JsonObject result = article.copy();

    // update: url, url_escape, title, file_path

    String url = "/articles/1513749666348/5oiR55qEX+esrDJfYXNkZlKeeshaAAaABCzzkuKrmlofnq6Bf";
    String url_escape = "/articles/123123123/我的_第2_asdf_个文章";
    String title = "666";//article.getString("title");
    String file_path = "1513749666348_5oiR55qEX+esrDJfYXNkZlKeeshaAAaABCzzkuKrmlofnq6Bf.pending.xml";

    result.put("url", url);
    result.put("url_escape", url_escape);
    result.put("title", title);
    result.put("file_path", file_path);

    return result;

  }

  @Override
  public WikiDatabaseService savePage(Long timestamp, String articleFileName, JsonObject newArticle, Handler<AsyncResult<JsonObject>> resultHandler) {

    String _article_file_name = StringHelper.escape(Base64.isBase64(articleFileName) ? new String(Base64.decodeBase64(articleFileName)) : articleFileName);
    String _article_file_name_base64 = Base64.encode(_article_file_name);

    String _new_article_name = StringHelper.escape(newArticle.getString("title"));
    String _new_article_name_base64 = Base64.encode(_new_article_name);


    Path _article_path = Paths.get(getFileFullName(timestamp, _article_file_name_base64, null));
    Path _new_filenpath = Paths.get(getFileFullName(timestamp, _new_article_name_base64, null));

    _article_file_name = _article_file_name + "_";

    System.out.println(_new_article_name_base64 + ", _new_article_name_base64");

    if (!_new_article_name.equals(_article_file_name)) {

      System.out.println(_new_article_name + ", _new_article_name");
      System.out.println(_article_file_name + ", _article_file_name");

      // 根据原文件复制新文件然后删除原文件
      try {
        Files.copy(_article_path, _new_filenpath, REPLACE_EXISTING);
      } catch (IOException e) {
        LOGGER.error("WikiDatabaseService savePage##Copy file error", e.getCause());
        resultHandler.handle(Future.failedFuture(e.getCause()));
      }

      if (Files.exists(_new_filenpath)) {

//        JsonObject oldArticle = readArticleXml(_new_filenpath.toString());

        // update: url, url_escape, title, file_path
        JsonObject refreshdObject = refreshArticleObject(newArticle);

        System.out.println(refreshdObject.encode() + ", refreshdObject");

//        updateArticleXml(
//          "/articles/1513749666348/5oiR55qEIOesrDIgXyAvICMgLiBhc2RmICMkJV4mKiooIUAjJCUlKSgqJmDkuKrmlofnq6A=.html",
//          "", "", "");


//        try {
//          Files.delete(_article_path);
//        } catch (IOException e) {
//          LOGGER.error("WikiDatabaseService savePage##Delete file error", e.getCause());
//          resultHandler.handle(Future.failedFuture(e.getCause()));
//        }

      }

      _article_path = _new_filenpath;
      _article_file_name = _new_article_name;
      _article_file_name_base64 = _new_article_name_base64;

    }

    File _the_update_file = _article_path.toFile();

    if (!_the_update_file.exists()) {
      resultHandler.handle(Future.succeededFuture(null));
      return this;
    }

    JsonObject result = new JsonObject()
      .put("url", getArticleUrl(timestamp, _article_file_name_base64))
      .put("url_escape", getArticleUrl(timestamp, _article_file_name));

    resultHandler.handle(Future.succeededFuture(result));

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
