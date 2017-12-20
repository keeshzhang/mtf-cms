package com.shenmao.vertx.starter.commons.string;

public class StringHelper {

  public static String escape(String str) {
    String str_escape = str.replaceAll("[^ a-zA-Z0-9\\u4e00-\\u9fa5]+", "_");
    str_escape = str_escape.replaceAll("[ ]+", "_");
    str_escape = str_escape.replaceAll("[_]+", "_");
    return str_escape;
  }
}
