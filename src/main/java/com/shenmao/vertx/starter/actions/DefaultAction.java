package com.shenmao.vertx.starter.actions;

import com.github.rjeschke.txtmark.Processor;
import com.shenmao.vertx.starter.database.WikiDatabaseService;
import com.shenmao.vertx.starter.database.WikiDatabaseVerticle;
import com.shenmao.vertx.starter.passport.ShiroRealm;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.shenmao.vertx.starter.database.WikiDatabaseVerticle.EMPTY_PAGE_MARKDOWN;

public class DefaultAction implements Action {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAction.class);
  private static final String wikiDbQueue = WikiDatabaseVerticle.CONFIG_WIKIDB_QUEUE;

  private static final FreeMarkerTemplateEngine templateEngine = FreeMarkerTemplateEngine.create();
  private static final String templateFolderName = "templates";

  private WikiDatabaseService dbService;

  private Vertx _vertx;

  public DefaultAction(Vertx vertx) {
    _vertx = vertx;
    dbService = WikiDatabaseService.createProxy(vertx, wikiDbQueue);
  }

  @Override
  public void indexHandler(RoutingContext context) {

    if (context.user() != null) {

      context.user().isAuthorized(ShiroRealm.Permission.CREATE.toString(),res -> {

        dbService.fetchAllPages(reply -> {

          if (reply.succeeded()) {

            context.put("title", "最新咨讯");
            context.put("content", reply.result());
            context.put("canCreatePage", res.succeeded() && res.result());

            ContextResponse.write(context, "/index.ftl");

          } else {
            context.fail(reply.cause());
          }

        });


      });

    } else {

      dbService.fetchAllPages(reply -> {

        if (reply.succeeded()) {

          context.put("title", "Wiki Home");
          context.put("content", reply.result());
          context.put("canCreatePage", false);

          ContextResponse.write(context, "/index.ftl");

        } else {
          context.fail(reply.cause());
        }

      });

    }





  }

  @Override
  public void pageUpdateHandler(RoutingContext context) {

    Long timestamp = null;
    String articleFileName = null;

    try {
      timestamp = Long.parseLong(context.request().getParam("date"));
      articleFileName = context.request().getParam("name");
    } catch (Exception e) {
      ContextResponse.notFound(context);
      return;
    }


//    Handler<AsyncResult<Long>> createHandler = reply -> {
//
//      if (reply.succeeded()) {
//
//        if (reply.result() == null || reply.result() == -1L) {
//          context.response().setStatusCode(400);
//          // TODO should be going to error page
//          //context.response().putHeader("Location", "/wiki/" + reply.result());
//
//          context.response().end();
//        } else {
//          ContextResponse.write(context, "/wiki/" + reply.result(), reply.result(), 301);
//        }
//
//
//      } else {
//        context.fail(reply.cause());
//      }
//
//    };
//
    Handler<AsyncResult<JsonObject>> updateHandler = reply -> {

      if (reply.succeeded()) {

//        System.out.println(reply.result() + ", updateHandler");
        ContextResponse.write(context, reply.result());

      } else {
        context.fail(reply.cause());
      }

    };

    JsonObject data = new JsonObject();

    if (false) {
//      dbService.createPage(articleFileName, context.request().getParam("markdown"), createHandler);
    } else {
      dbService.savePage(timestamp, articleFileName, context.getBodyAsJson(), updateHandler);
    }


  }

  @Override
  public void pageDeletionHandler(RoutingContext context) {

    Long id = Long.parseLong(context.request().getParam("id"));

    dbService.deletePage(id, reply -> {

      if (reply.succeeded()) {
        ContextResponse.write(context, "/", id, 301);
      } else {
        context.fail(reply.cause());
      }

    });

  }

  @Override
  public void pageCreateHandler(RoutingContext context) {

    String pageName = context.request().getParam("name");

    dbService.createPage(pageName, EMPTY_PAGE_MARKDOWN, reply -> {

      if (reply.succeeded()) {
        ContextResponse.write(context, "/wiki/" + (long) reply.result(), (long) reply.result(), 301);
      } else {
        context.fail(reply.cause());
      }

    });

  }

  @Override
  public void pageRenderingHandler(RoutingContext context) {

    Long timestamp = null;
    String articleFileName = null;

    try {
      timestamp = Long.parseLong(context.request().getParam("date"));
      articleFileName = context.request().getParam("name");
    } catch (Exception e) {
      ContextResponse.notFound(context);
      return;
    }

    dbService.fetchPage(timestamp, articleFileName, reply -> {

      if (reply.succeeded()) {

        if (reply.result() != null) {
          ContextResponse.write(context, reply.result(), "/pages/page_detail.ftl");
        } else {
          ContextResponse.notFound(context);
        }

      } else {
        context.fail(reply.cause());
      }

    });

  }

  @Override
  public void backupHandler(RoutingContext context) {

    dbService.fetchAllPages(reply -> {

      if (!reply.succeeded()) {
        context.fail(reply.cause());
      } else {

        JsonObject filesListObject = new JsonObject();


        JsonObject gistPayload = new JsonObject()
              .put("files", filesListObject)
              .put("description", "A wiki backup")
              .put("public", true);

        WebClient webClient = WebClient.create(_vertx,
              new WebClientOptions().setSsl(true).setUserAgent("vert-x3"));

        webClient.post(443, "api.github.com", "/gists")
          .putHeader("Accept", "application/vnd.github.v3+json")
          .putHeader("Content-Type", "application/json")
          .as(BodyCodec.jsonObject())
          .sendJsonObject(gistPayload, ar -> {

          if (ar.succeeded()) {

            HttpResponse<JsonObject> response = ar.result();

            if (response.statusCode() == 201) {

              context.put("backup_gist_url", response.body().getString("html_url"));

              System.out.println(context.get("backup_gist_url") + " , backup_gist_url 777");
              indexHandler(context);

            } else {

              StringBuilder message = new StringBuilder()
                .append("Could not backup the wiki: ")
                .append(response.statusMessage());

              JsonObject body = response.body();

              if (body != null) {
                message.append(System.getProperty("line.separator")).append(body.encodePrettily());
              }  {
                LOGGER.error(message.toString());
                context.fail(502);
              }

            }

          } else {

            LOGGER.error("Vert.x HTTP Client error", ar.cause());
            context.fail(ar.cause());
          }

        });

      }

    });

  }


  @Override
  public Vertx getVertx() {
    return this._vertx;
  }

}
