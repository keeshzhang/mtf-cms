package com.shenmao.vertx.starter.MtfCrawler;

import com.shenmao.vertx.starter.commons.HttpGets;
import com.shenmao.vertx.starter.commons.HttpResult;
import com.shenmao.vertx.starter.commons.files.MyFileWriter;
import io.vertx.core.json.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexunCrawlerParser {

  // http://forex.hexun.com/fxobservation/index-1246.html
  private static final String _PAGE_ENCODE = "GB2312";
  private static final String _PAGE_INDEX_URL = "http://forex.hexun.com/fxobservation";
  private static final String _PAGE_SAVE_FOLDER = "crawler_pages/hexun_pages";

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

    if (maxPageNumber == -1) return;

    String indexPageFileName = "index-" + (maxPageNumber) + ".html";
    indexCrawlerParser.setFrom(getParseUrl(maxPageNumber));

    // 看看是否存在, 如果存在则跳过进一步抓去
    boolean pageExists = indexCrawlerParser._myFileWriter.exists(indexPageFileName);

    if (pageExists) {
      return;
    }

    indexCrawlerParser._myFileWriter.write(indexPageContent, indexPageFileName);

    indexCrawlerParser.parseIndexArticle();

  }

  private void parseIndexArticle () {

    String articleClassName = ".list24px li a";
    Document articleIndexDocument = Jsoup.parse(_htmlContent);
    Elements articleUrlListElement = articleIndexDocument.select(articleClassName);

    articleUrlListElement.stream()
      .map(a -> a.attr("href"))
      .filter(a -> {
        // 查看当前页面是否已经爬取过， 如果已经爬取过则跳过
        return a.equals("http://forex.hexun.com/2017-12-24/192069228.html");
      })
      .map(link -> parseArticleContent(link))
      .forEach(article -> System.out.println(article.encode()));


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

    Pattern _maxPagePattern = Pattern.compile("hxPage.maxPage\\s*=\\s*([\\d]{4})\\s*;");

    Matcher _maxPageMatcher = _maxPagePattern.matcher(_htmlContent);

    if (_maxPageMatcher.find()) {
      return Integer.parseInt(_maxPageMatcher.group(1));
    }

    return -1;
  }


}
