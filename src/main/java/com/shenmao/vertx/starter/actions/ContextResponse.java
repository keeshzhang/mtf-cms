package com.shenmao.vertx.starter.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shenmao.vertx.starter.commons.JsonToXMLConverter;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContextResponse {

  public static String original(String uri) {
    if (uri.indexOf('?') == -1) return uri;
    return uri.substring(0, uri.indexOf('?'));
  }

  private static final FreeMarkerTemplateEngine templateEngine = FreeMarkerTemplateEngine.create();
  private static final String templateFolderName = "templates";


  public static void write(RoutingContext context, ActionView view, Integer statusCode) {
    context.response().setStatusCode(statusCode == null ? 200 : statusCode);
    write(context, statusCode == 404 ? new ActionView("/not-found.ftl") : view);
  }


  public static void write(RoutingContext context, Object data) {
    context.put("content", data);
    write(context);
  }

  public static void write(RoutingContext context) {

    JsonObject response = new JsonObject().put("success", true);

    response.put("data", context.get("content") != null ? (Object)context.get("content") : new JsonObject());

    String result = response.encode();

    if (original(context.request().uri()).endsWith(".xml") && 1==2) {

      context.response().putHeader("Content-Type", "application/xml");

      try {
        result = new JsonToXMLConverter().convertJsonToXml(result);
      } catch (IOException e) {
        e.printStackTrace();
      }

    } else {

      context.response().putHeader("Content-Type", "application/json");
    }

    context.response().end(result);

  }


  public static void write(RoutingContext context, Object data, ActionView view) {

    context.put("content", data);

    if (view == null) {
      write(context);
      return;
    }

    write(context, view);
  }

  public static void write(RoutingContext context, ActionView view) {

    if (original(context.request().uri()).endsWith(".json") || original(context.request().uri()).endsWith(".xml")) {
      write(context);
      return;
    }

    context.put("username", context.user() != null && !context.user().principal().getString("username").isEmpty() ? context.user().principal().getString("username") : "anonymous user");

    templateEngine.render(context, templateFolderName, view.view(), ar -> {

      if (ar.succeeded()) {
        context.response().putHeader("Content-Type", "text/html");
        context.response().end(ar.result());
      } else {
        context.fail(ar.cause());
      }

    });

  }


  public static void redirect(RoutingContext context, String location, Integer statusCode) {

    context.response().setStatusCode(statusCode == null ? 301 : statusCode);
    context.response().putHeader("Location", location);
    context.response().end();

  }

  public static void write(RoutingContext context, String location, Object data, Integer statusCode) {

    context.response().setStatusCode(statusCode == null ? 301 : statusCode);
    context.response().putHeader("Location", location);
    context.response().end(data + "");

  }

  public static void notFound(RoutingContext context) {
    ContextResponse.write(context, new ActionView("/pages/not-found.ftl"), 404);
  }

}
