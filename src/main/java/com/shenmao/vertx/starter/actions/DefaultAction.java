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
import org.apache.xpath.operations.Bool;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
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

            ContextResponse.write(context, new ActionView("/index.ftl"));

          } else {
            context.fail(reply.cause());
          }

        });


      });

    } else {

      dbService.fetchAllPages(reply -> {

        if (reply.succeeded()) {

          context.put("title", "最新咨讯");
          context.put("content", reply.result());
          context.put("canCreatePage", false);

          ContextResponse.write(context, new ActionView("/index.ftl"));

        } else {
          context.fail(reply.cause());
        }

      });

    }

  }

  @Override
  public void pageModifyHandler(RoutingContext context, String behaver) {

    System.out.println(behaver + ", behaver 1");
    if (!Arrays.asList(new String[] { "publish", "draft", "delete" }).contains(behaver)) {
      ContextResponse.notFound(context);
      return;
    }

    final Long timestamp = Long.parseLong(context.request().getParam("date"));
    final String articleName = context.request().getParam("name");

    dbService.fetchPage(timestamp, articleName, reply -> {

      if (reply.succeeded() && reply.result() != null) {

        String articleStatus = "pending";

        switch (behaver) {
          case "publish":
            articleStatus = "published";
            break;
          case "draft":
            articleStatus = "pending";
            break;
          case "delete":
            articleStatus = "deleted";
            break;
          default:
            articleStatus = "pending";
        }

        JsonObject artilce = reply.result();
        artilce.put("article_status", articleStatus);
        artilce.put("published_at", WikiDatabaseService.DATE_FORMAT.format(Calendar.getInstance().getTime()));

        dbService.savePage(timestamp, articleName, artilce, res -> {
          System.out.println(res.result().encode() + ", behaver");
          ContextResponse.redirect(context, "/articles/" + timestamp + "/" + articleName, 301);
        });

      } else {
        ContextResponse.notFound(context);
      }

    });

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

    // dbService.createPage(articleFileName, context.request().getParam("markdown"), createHandler);
    dbService.savePage(timestamp, articleFileName, context.getBodyAsJson(), updateHandler(context, null));

  }

  private Handler<AsyncResult<JsonObject>> updateHandler(RoutingContext context, ActionView view) {

    Handler<AsyncResult<JsonObject>> updateHandler = reply -> {

      if (reply.succeeded()) {
        ContextResponse.write(context, reply.result(), view);
      } else {
        context.fail(reply.cause());
      }

    };

    return updateHandler;

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

    dbService.createPage(pageName, reply -> {

      context.response().end(reply.result().encode());

//      if (reply.succeeded()) {
//        ContextResponse.write(context, "/wiki/" + (JsonObject) reply.result(), (long) reply.result(), 301);
//      } else {
//        context.fail(reply.cause());
//      }

    });

  }

  @Override
  public void pageRenderingHandler(RoutingContext context) {

    Long timestamp = null;
    String articleFileName = null;

    String action = context.queryParams() != null && context.queryParams().contains("action") ? context.queryParams().get("action") : null;   // preview, pub, draft, del

    System.out.println(action + ", action");

    if (action != null && !action.equals("preview")) {
      this.pageModifyHandler(context, action);
      return;
    }

    context.put("isArticlePreview", action != null && action.equals("preview") ? "yes" : "no");

    try {
      timestamp = Long.parseLong(context.request().getParam("date"));
      articleFileName = context.request().getParam("name");
    } catch (Exception e) {
      ContextResponse.notFound(context);
      return;
    }

    dbService.fetchPage(timestamp, articleFileName, reply -> {

      if (!reply.succeeded() || reply.result() == null) {
//        context.fail(reply.cause());
        ContextResponse.notFound(context);
        return;
      }

      context.put("seo", new JsonObject()
        .put("title", reply.result().getString("title"))
        .put("keywords", reply.result().getString("keywords"))
        .put("description", reply.result().getString("description")));

      ContextResponse.write(context, reply.result(), new ActionView("/pages/page_detail.ftl"));

    });

  }



  @Override
  public Vertx getVertx() {
    return this._vertx;
  }

}
