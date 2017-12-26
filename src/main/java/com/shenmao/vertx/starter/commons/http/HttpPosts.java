package com.shenmao.vertx.starter.commons.http;

import com.shenmao.vertx.starter.commons.encode.Base64;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.http.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HttpPosts {

  private static Logger LOGGER = LoggerFactory.getLogger(HttpPosts.class);

  public static HttpResult execute(String url, BasicNameValuePair... params) {
    return execute(url, null, null, params);
  }

  public static HttpResult execute(String url, String username, String password, BasicNameValuePair... params) {

    LOGGER.info(url, "url");

    HttpResult result = new HttpResult() ;

    try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

      List<NameValuePair> urlParameters = new ArrayList<>();

      for (BasicNameValuePair p : params) {
        urlParameters.add(p);
      }


      HttpPost httpPost = new HttpPost(url);

      if (username != null && password != null) {
        httpPost.setHeader("Authorization", "Basic " + Base64.encode(username + ":" + password));
      }

      httpPost.setEntity(new UrlEncodedFormEntity(urlParameters, "utf-8"));
      HttpResponse httpResponse = httpClient.execute(httpPost);

      BufferedReader rd = new BufferedReader(
        new InputStreamReader(httpResponse.getEntity().getContent()));

      StringBuffer buffer = new StringBuffer();

      String line = "";

      while ((line = rd.readLine()) != null) {
        buffer.append(line);
      }

      LOGGER.info(httpResponse.getStatusLine().getStatusCode(), "http client status");

      result.setStatusCode(httpResponse.getStatusLine().getStatusCode());
      result.setContent(buffer.toString());

    } catch (IOException e) {
      LOGGER.error(e.getCause(), "HttpPosts error!");
      result.setError(true);
      result.setMessage(e.getCause().toString());
    }

    return result;

  }


}
