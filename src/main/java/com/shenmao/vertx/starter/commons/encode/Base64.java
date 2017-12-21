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
    //  我的 第2 _ / # . asdf #$%^&**(!@#$%%)(*&`个文章 adf asdf 234adf2#@!#
    String base64str = org.apache.commons.codec.binary.Base64.encodeBase64String(str.getBytes());

//    String test = org.apache.commons.codec.binary.Base64.encodeBase64String("我的 第2 _ / # . asdf #$%^&**(!@#$%%)(*&`个文章 adf asdf 234adf2#@!#".getBytes());
//
//    try {
//      test = URLEncoder.encode(test, "UTF-8");
//      System.out.println(test + " base 64 URLEncoder");
//
//      test = URLDecoder.decode(test, "UTF-8");
//      System.out.println(URLDecoder.decode(test, "UTF-8") + " base 64 URLDecoder");
//
//      test = decodeBase64(test);
//      System.out.println(URLDecoder.decode(test, "UTF-8") + " decodeBase64");
//
//    } catch (UnsupportedEncodingException e) {
//      e.printStackTrace();
//    }

    return base64str.replaceAll("/", _SLASH_REPLACE).replaceAll("=+$", "");
  }

  public static Boolean isBase64(String str) {
    return org.apache.commons.codec.binary.Base64.isBase64(replaceSlash(str));
  }

  public static String decodeBase64(String str) {
    return new String(org.apache.commons.codec.binary.Base64.decodeBase64(replaceSlash(str)));
  }

}
