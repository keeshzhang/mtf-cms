package com.shenmao.vertx.starter.actions;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public interface Action {

  public default void staticHandler(RoutingContext context) {
    context.request().response().sendFile(context.request().path().substring(1));
  }
  public void indexHandler(RoutingContext context);
  public void pageUpdateHandler(RoutingContext context);
  public void pageModifyHandler(RoutingContext context, String action);
  public void pageDeletionHandler(RoutingContext context);
  public void pageCreateHandler(RoutingContext context);
  public void pageRenderingHandler(RoutingContext context);

  public default Integer getInt(String str) {
    return (str != null && str.trim().length() > 0 && str.replaceAll("\\d+", "").isEmpty())
      ? Integer.parseInt(str) : -1;
  }

  public default Long getLong(String str) {
    System.out.println(str + ", str");
    return (str != null && str.trim().length() > 0 && str.replaceAll("\\d+", "").isEmpty())
            ? Long.parseLong(str) : -1;
  }

  public Vertx getVertx();

}
