package com.shenmao.vertx.starter.MtfCrawler;

import com.shenmao.vertx.starter.commons.HttpGets;
import com.shenmao.vertx.starter.commons.HttpResult;
import com.shenmao.vertx.starter.commons.files.JavaGrep;
import com.shenmao.vertx.starter.commons.files.MyFileWriter;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexunCrawlerParser {

  private static Logger LOGGER = LoggerFactory.getLogger(HexunCrawlerParser.class);

  // http://forex.hexun.com/fxobservation/index-1246.html
  private static final String _PAGE_ENCODE = "GB2312";
  private static final String _PAGE_INDEX_URL = "http://forex.hexun.com/fxobservation";
  private static final String _PAGE_SAVE_FOLDER = "crawler_pages/hexun_pages";
  private static final String _ARTICLE_CACHED_FILE = "articles.url.txt";

  private String _htmlContent;
  private String _from;
  private MyFileWriter _myFileWriter;

  public void setFrom(String url) {
    this._from = url;
  }

  public HexunCrawlerParser(String htmlContent, String url) {
    this._myFileWriter = new MyFileWriter(_PAGE_SAVE_FOLDER);
    this._htmlContent = htmlContent;
    this._from = url;
  }

  private static String getParseUrl(Integer pagenumber) {
    if (pagenumber == null) return _PAGE_INDEX_URL + "/index.html";
    return _PAGE_INDEX_URL + "/index-" + pagenumber + ".html";
  }

  private static String download(String url) {
    HttpResult httpResultHexun = HttpGets.execute(getParseUrl(null), _PAGE_ENCODE);
    return httpResultHexun.getContent();
  }

  public static void run() {

    String indexPageUrl = getParseUrl(null);
    String indexPageContent = download(indexPageUrl);
    HexunCrawlerParser indexCrawlerParser = new HexunCrawlerParser(indexPageContent, indexPageUrl);

    int maxPageNumber = indexCrawlerParser.getMaxPageNumber();

    if (maxPageNumber == -1) {
      return;
    }

    indexCrawlerParser.setFrom(getParseUrl(maxPageNumber));
    indexCrawlerParser.parseIndexArticle();

  }

  private void parseIndexArticle () {

    String articleClassName = ".list24px li a";
    Document articleIndexDocument = Jsoup.parse(_htmlContent);
    Elements articleUrlListElement = articleIndexDocument.select(articleClassName);

    articleUrlListElement.stream()
      .map(a -> a.attr("href"))
      .filter(a -> {
        return a.equals("http://forex.hexun.com/2017-12-24/192069228.html");
      })
      .filter(a -> {
        // 查看当前页面是否已经爬取过， 如果已经爬取过则跳过
        return !cachedAndWriteArticleUrl(a);
      })
      .map(link -> parseArticleContent(link))
      .forEach(article -> System.out.println(article.encode()));

  }

  private void writeArticlePage(JsonObject articleObject) {

    // curl -X POST -v -u keesh:keesh -F name=666 http://localhost:9180/create
    // update

  }

  /**
   * 持久缓存一个 url, 如果没有缓存则写入持久文件缓存
   * @return
   */
  private boolean cachedAndWriteArticleUrl(String url) {

    File articleCachedFile = this._myFileWriter.getFile(_ARTICLE_CACHED_FILE);

    List<String> result = null;

    try {

      if (!articleCachedFile.exists())  throw new IOException();
      result = JavaGrep.grep(url, articleCachedFile.getAbsolutePath());

      if (result.size() == 0) {
        _myFileWriter.appendLine(url, articleCachedFile.getAbsolutePath());
        return false;
      }

      return true;

    } catch (IOException e) {
      LOGGER.error("HexunCrawlerParser.cachedAndWriteArticleUrl article cached file not exists [" + articleCachedFile.getAbsolutePath() + "]");
    }

    return false;
  }

  private JsonObject parseArticleContent(String articlePageUrl) {

    String articleContent = HttpGets.execute(articlePageUrl, _PAGE_ENCODE).getContent();
    String artilceLink = articlePageUrl;
    String artilceTitle = null;
    String artilceKeywords = null;
    String artilceDescription = null;
    String artilcePubdate = null;
    String articleHtml = null;

    Document articleDocument = Jsoup.parse(articleContent);
    Elements articleMetaAttrs = articleDocument.head().getElementsByTag("meta");

    artilceTitle = articleDocument.title();
    articleDocument.head().select("meta");

    for (Element meta : articleMetaAttrs) {
      switch (meta.attr("name")) {
        case "keywords":
          artilceKeywords = meta.attr("content");
          break;
        case "description":
          artilceDescription = meta.attr("content");
          break;
        default:
      }
    }

    artilcePubdate = articleDocument.select("span.pr20").first().text();
    articleHtml = articleDocument.select("div.art_contextBox").first().outerHtml();

    JsonObject article = new JsonObject()
      .put("article_link", artilceLink)
      .put("article_title", artilceTitle)
      .put("article_keywords", artilceKeywords)
      .put("article_description", artilceDescription)
      .put("article_pubdate", artilcePubdate)
      .put("artilce_html", articleHtml)
      .put("article_source_from", "hexun")
      .put("article_from_url", _from);

    return article;

  }

  private int getMaxPageNumber() {

//    String _pagerCainer = "page2011nav";

    String pattern = "hxPage.maxPage\\s*=\\s*([\\d]{4})\\s*;";
    Pattern _maxPagePattern = Pattern.compile(pattern);

    Matcher _maxPageMatcher = _maxPagePattern.matcher(_htmlContent);

    if (_maxPageMatcher.find()) {
      return Integer.parseInt(_maxPageMatcher.group(1));
    } else {
      LOGGER.error("HexunCrawlerParser.getMaxPageNumber##Could not get regual pattern [" + pattern + "]");
    }

    return -1;
  }


}
