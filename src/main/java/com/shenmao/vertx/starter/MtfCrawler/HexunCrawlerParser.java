package com.shenmao.vertx.starter.MtfCrawler;

import com.shenmao.vertx.starter.commons.HttpGets;
import com.shenmao.vertx.starter.commons.HttpResult;
import com.shenmao.vertx.starter.commons.files.MyFileWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
  private File _htmlFile;
  private String _from;
  private MyFileWriter _myFileWriter;

  public HexunCrawlerParser(String htmlContent, File file, String from) {
    this._myFileWriter = new MyFileWriter(_PAGE_SAVE_FOLDER);
    this._htmlContent = htmlContent;
    this._htmlFile = file;
    this._from = from;
  }

  private static String getParseUrl(String pagenumber) {
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
    HexunCrawlerParser indexCrawlerParser = new HexunCrawlerParser(indexPageContent, null, indexPageUrl);

    int maxPageNumber = indexCrawlerParser.getMaxPageNumber();

    if (maxPageNumber == -1) return;

    String indexPageFileName = "index-" + (maxPageNumber) + ".html";

    // 看看是否存在, 如果存在则跳过进一步抓去
    boolean pageExists = indexCrawlerParser._myFileWriter.exists(indexPageFileName);

    if (pageExists) {
      return;
    }

    File indexPageFile = indexCrawlerParser._myFileWriter.write(indexPageContent, indexPageFileName);

    indexCrawlerParser.fetchArticleUrls();

  }

  private void fetchArticleUrls () {

    String articleClassName = ".list24px li a";

    Document articleDocument = Jsoup.parse(_htmlContent);

    Elements articleUrlListElement = articleDocument.select(articleClassName);

    articleUrlListElement.stream()
      .map(a -> a.attr("href"))
      .map(link -> HttpGets.execute(link, _PAGE_ENCODE).getContent())
      .forEach(articleContent -> System.out.println(articleContent));


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
