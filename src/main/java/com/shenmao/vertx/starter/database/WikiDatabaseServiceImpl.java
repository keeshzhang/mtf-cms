package com.shenmao.vertx.starter.database;

import com.shenmao.vertx.starter.commons.encode.Base64;
import com.shenmao.vertx.starter.commons.files.FindFileVisitor;
import com.shenmao.vertx.starter.commons.string.StringHelper;
import com.shenmao.vertx.starter.configuration.SqlQueriesConfig;
import java.sql.Timestamp;
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
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class WikiDatabaseServiceImpl implements WikiDatabaseService {

  private static Logger LOGGER = LoggerFactory.getLogger(WikiDatabaseService.class);

  private final HashMap<SqlQueriesConfig.SqlQuery, String> sqlQueries;
  private final JDBCClient jdbcClient;

  private static final SimpleDateFormat _DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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

  public boolean updateArticleXml(JsonObject articleObject) {

    String file_path = articleObject.getString("file_path");

    SAXBuilder builder = new SAXBuilder();

    try {

      org.jdom.Document doc = builder.build(_USER_ARTICLES_FOLDER + "/" + file_path);
      org.jdom.Element root = doc.getRootElement();
      List<org.jdom.Element> nodelist = root.getChildren();

      for(org.jdom.Element ele: nodelist) {

        switch (ele.getName()) {

          case "url":
            ele.setContent(new org.jdom.Text(articleObject.getString("url")));
            break;
          case "url_escape":
            ele.setContent(new org.jdom.Text(articleObject.getString("url_escape")));
            break;
          case "title":
            ele.setContent(new org.jdom.CDATA(articleObject.getString("title")));
            break;
          case "file_path":
            ele.setContent(new org.jdom.Text(articleObject.getString("file_path")));
            break;
          case "article_status":
            ele.setContent(new org.jdom.Text(articleObject.getString("article_status")));
            break;
          case "type":
            ele.setContent(new org.jdom.Text(articleObject.getString("type")));
            break;
          case "tags":
            ele.setContent(new org.jdom.Text(articleObject.getString("tags")));
            break;
          case "put_top":
            ele.setContent(new org.jdom.Text(articleObject.getString("put_top")));
            break;
          case "authors":
            ele.setContent(new org.jdom.Text(articleObject.getString("authors")));
            break;
          case "created_at":
            ele.setContent(new org.jdom.Text(articleObject.getString("created_at")));
            break;
          case "last_updated":
            ele.setContent(new org.jdom.Text(_DATE_FORMAT.format(Calendar.getInstance().getTime())));
            break;
          case "channel":
            ele.setContent(new org.jdom.Text(articleObject.getString("channel")));
            break;
          case "keywords":
            ele.setContent(new org.jdom.CDATA(articleObject.getString("keywords")));
            break;
          case "description":
            ele.setContent(new org.jdom.CDATA(articleObject.getString("description")));
            break;
          case "html_content":
            ele.setContent(new org.jdom.CDATA(articleObject.getString("html_content")));
            break;
          default:

        }

      }

      XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
      xmlOutputter.output(doc, new FileOutputStream(_USER_ARTICLES_FOLDER + "/" + file_path));
      return true;

//        Document masterDocument = reader.read(_USER_ARTICLES_FOLDER + "/" + file_path);
//      Element rootElement = masterDocument.getRootElement();
//
//      Node articleUrlNode = masterDocument.selectSingleNode("//article/url");
//      Node articleUrlEscapeNode = masterDocument.selectSingleNode("//article/url_escape");
//      Node articleTitleNode = masterDocument.selectSingleNode("//article/title");
//      Node articleFilePathNode = masterDocument.selectSingleNode("//article/file_path");
//
////      org.w3c.dom.Element articleTitleElement = doc.getElementById("title");
////      System.out.println(doc.getFirstChild().getFirstChild().getNodeName() + ", articleTitleNode not a element");
//
//      articleUrlNode.setText(articleObject.getString("url"));
//      articleUrlEscapeNode.setText(articleObject.getString("url_escape"));
//      articleTitleNode.setText("<![CDATA[" + articleObject.getString("title") + "]]>");
//
//      articleFilePathNode.setText(file_path);
//
//      try (FileWriter fw = new FileWriter(_USER_ARTICLES_FOLDER + "/" + file_path, false)) {
//        System.out.println(masterDocument.selectSingleNode("//article/file_path").getText() + ", articleFilePathNode.getText() 1");
//        System.out.println(articleFilePathNode.getText() + ", articleFilePathNode.getText() 2");
//        masterDocument.write(fw);
//        return true;
//      } catch (IOException e) {
//        LOGGER.error("updateArticleXml##Writer xml file error", e.getCause());
//        return false;
//      }

    } catch ( JDOMException | IOException e) {
      LOGGER.error("updateArticleXml##Load xml file error", e.getCause());
      return false;
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
      .put("id", articleUrl)
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

    final String _createAt = article.getString("created_at");


    Date createAtDate = null;
    Timestamp createAtTs = null;

    try {
      createAtDate = _DATE_FORMAT.parse(_createAt);
      createAtTs = new Timestamp(createAtDate.getTime());
    } catch (ParseException e) {

      String defaultDate = "1970-01-01 00:00:00";

      try {
        createAtDate = _DATE_FORMAT.parse(defaultDate);
        createAtTs = new Timestamp(createAtDate.getTime());
      } catch (ParseException e1) { }

    }

    // update: url, url_escape, title, file_path, name, name_base64
    String article_name = StringHelper.escape(article.getString("title"));
    String url = "/articles/" + createAtTs.getTime() + "/" + Base64.encode(article_name);
    String url_escape = "/articles/" + createAtTs.getTime() + "/" + article_name;

    result.put("id", url);
    result.put("url", url);
    result.put("url_escape", url_escape);
    result.put("title", article.getString("title"));
    result.put("name", article_name);
    result.put("name_base64", Base64.encode(article_name));
    result.put("file_path", createAtTs.getTime() + "_" + Base64.encode(article_name) + "." + article.getString("article_status") + ".xml");
    result.put("file_name", createAtTs.getTime() + "_" + Base64.encode(article_name) + "." + article.getString("article_status") + ".xml");

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

    if (!_new_article_name.equals(_article_file_name)) {

      // new article: 根据原文件复制新文件然后删除原文件
      try {
        Files.copy(_article_path, _new_filenpath, REPLACE_EXISTING);
      } catch (IOException e) {
        LOGGER.error("WikiDatabaseService savePage##Copy file error", e.getCause());
        resultHandler.handle(Future.failedFuture(e.getCause()));
      }

      try {
        Files.delete(_article_path);
      } catch (IOException e) {
        LOGGER.error("WikiDatabaseService savePage##Delete file error", e.getCause());
        resultHandler.handle(Future.failedFuture(e.getCause()));
      }

      _article_path = _new_filenpath;
      _article_file_name = _new_article_name;
      _article_file_name_base64 = _new_article_name_base64;

    }

    if (Files.exists(_article_path)) {

      // update: url, url_escape, title, file_path
      JsonObject refreshdObject = refreshArticleObject(newArticle);

      if(!updateArticleXml(refreshdObject)) {
        LOGGER.error("savePage##Update artile page failed");
      } else {
        System.out.println("update page success");
      }

    } else {
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
