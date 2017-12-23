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
import java.util.*;
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

    String isError = context.queryParams().contains("error") ? "yes" : "no";

    context.put("title", "最新咨讯");
    context.put("error", isError);

    dbService.fetchAllPages(indexResponseHandler(context, new ActionView("/index.ftl")));

  }


  private Handler<AsyncResult<List<JsonObject>>> indexResponseHandler(RoutingContext context, ActionView view) {

    Handler<AsyncResult<List<JsonObject>>> updateHandler = reply -> {

      if (reply.succeeded()) {

        List<JsonObject> list = reply.result().stream()
          .filter(a -> {
            return !a.getString("article_status").equals("deleted");
          })
          .filter(a -> {

            Boolean isDisplay = context.user() != null;

            if (!isDisplay) {
              isDisplay = a.getString("article_status").equals("published");
            }

            return isDisplay;

          }).map(a -> {

            switch (a.getString("article_status")) {
              case "published":
                a.put("article_status_name", "已发布");
                break;
              case "pending":
                a.put("article_status_name", "未发布");
                break;
              case "deleted":
                a.put("article_status_name", "已删除");
                break;
              default:
                a.put("article_status_name", "未知");
            }

            return a;
          }).collect(Collectors.toList());

        list.forEach(o -> System.out.println(o.getString("last_updated") + "," + o.getString("title")));

        context.put("content", list);
        context.put("canCreatePage", context.user() != null);

        ContextResponse.write(context, new ActionView("/index.ftl"));

      } else {
        context.fail(reply.cause());
      }

    };

    return updateHandler;

  }


  @Override
  public void pageModifyHandler(RoutingContext context, String behaver) {

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

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    dbService.createPage(timestamp.getTime(), pageName.trim(), reply -> {

      if (reply.succeeded() && reply.result() != null) {
        ContextResponse.redirect(context, reply.result().getString("url"), 301);
      } else {
        ContextResponse.redirect(context, "/index?error", 303);
      }

//      context.response().end(reply.result().encode());


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

    String action = context.queryParams().contains("action") ? context.queryParams().get("action") : null;   // preview, pub, draft, del

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
