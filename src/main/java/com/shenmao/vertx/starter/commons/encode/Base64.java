package com.shenmao.vertx.starter.commons.encode;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class Base64 {

  public static final String _SLASH_REPLACE = "KeeshaAAaABCzz";

  private static String replaceSlash(String str) {
    return str.replaceAll(_SLASH_REPLACE, "/");
  }

  public static String encode(String str) {
    String base64str = org.apache.commons.codec.binary.Base64.encodeBase64String(str.getBytes());
    return base64str.replaceAll("/", _SLASH_REPLACE).replaceAll("=+$", "");
  }

  public static Boolean isBase64(String str) {
    return encode(decodeBase64(str)).equals(str);
  }

  public static String decodeBase64(String str) {
    return new String(org.apache.commons.codec.binary.Base64.decodeBase64(replaceSlash(str)));
  }

}
