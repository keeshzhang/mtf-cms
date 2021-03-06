package com.shenmao.vertx.starter.routers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shenmao.vertx.starter.actions.Action;
import com.shenmao.vertx.starter.actions.DefaultAction;
import com.shenmao.vertx.starter.actions.NormalAction;
import com.shenmao.vertx.starter.passport.JWTAuthenticated;
import com.shenmao.vertx.starter.passport.ShiroAuthenticate;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.auth.shiro.ShiroAuth;
import io.vertx.ext.auth.shiro.ShiroAuthOptions;
import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;
import com.shenmao.vertx.starter.passport.FormLoginHandlerImpl;
import io.vertx.ext.web.sstore.LocalSessionStore;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;


public class VertxRouter {

  NormalAction _normalAction ;
  DefaultAction _defaultAction ;
  Vertx _vertx;

  AuthProvider _shiroAuth;
  JWTAuth _jwtAuth;

  AuthHandler _authHandler;

  public VertxRouter (Vertx vertx) {

    _vertx = vertx;

    _defaultAction = new DefaultAction(vertx);
    _normalAction = new NormalAction(vertx);

    _shiroAuth = ShiroAuthenticate.newInstance(_vertx);
    _jwtAuth = JWTAuthenticated.newInstance(_vertx);

    _authHandler = RedirectAuthHandler.create(_shiroAuth, "/login");


  }

  public Router getRouter() {

    Router router = Router.router(_defaultAction.getVertx());

    router.get("/static/*").handler(_defaultAction::staticHandler);
    router.get("/assets/*").handler(_defaultAction::staticHandler);

    router.route().handler(CookieHandler.create());
    router.route().handler(BodyHandler.create());
    router.route().handler(SessionHandler.create(LocalSessionStore.create(_vertx)));
    router.route().handler(UserSessionHandler.create(_shiroAuth));


    router.route("/articles").handler(_authHandler);
    router.route("/wiki/*").handler(_authHandler);
    router.route("/save/*").handler(_authHandler);
//    router.route("/create/*").handler(_authHandler);
    router.route("/delete/*").handler(_authHandler);
    router.route("/backup/*").handler(_authHandler);

    router.get("/").handler(rc -> {
      rc.response().setStatusCode(303);
      rc.response().putHeader("Location", "/index");
      rc.response().end();
    });


    router.routeWithRegex("/index(.json|.html|.xml)?").handler(_defaultAction::indexHandler);

    router.get("/articles/:ymd/:date/:name.html").handler(_defaultAction::pageRenderingHandler);
    router.get("/articles/:ymd/:date/:name.json").handler(_defaultAction::pageRenderingHandler);
    router.get("/articles/:ymd/:date/:name.xml").handler(_defaultAction::pageRenderingHandler);
    router.get("/articles/:ymd/:date/:name").handler(_defaultAction::pageRenderingHandler);    // must be under exetenion router
//    router.get("/articles/:date/:name/c/:behaver").handler(_defaultAction::pageModifyHandler);

    // curl -X POST -v -u keesh:keesh -F name=666 http://localhost:9180/create.json
    router.post("/articles.json").handler(BasicAuthHandler.create(_shiroAuth)).handler(_defaultAction::pageCreateHandler);
    router.post("/articles").handler(BasicAuthHandler.create(_shiroAuth)).handler(_defaultAction::pageCreateHandler);

    // curl -H "Content-Type: application/json" -X POST -d '{"username":"xyz","password":"xyz"}' http://localhost:9180/save.json
    router.post("/articles/:ymd/:date/:name.json").handler(BasicAuthHandler.create(_shiroAuth)).handler(_defaultAction::pageUpdateHandler);

//    router.get("/wiki/:id").handler(_defaultAction::pageRenderingHandler);

//    router.post("/save.json").handler(BasicAuthHandler.create(_shiroAuth)).handler(_defaultAction::pageUpdateHandler);
//    router.post("/save").handler(BasicAuthHandler.create(_shiroAuth)).handler(_defaultAction::pageUpdateHandler);
//
//    router.post("/create.json").handler(BasicAuthHandler.create(_shiroAuth)).handler(_defaultAction::pageCreateHandler);
//    router.post("/create").handler(BasicAuthHandler.create(_shiroAuth)).handler(_defaultAction::pageCreateHandler);


    router.get("/login").handler(_normalAction::loginHandler);

    // curl -v -d "username=keesh&password=keesh" http://localhost:9180/index
    router.post("/login-auth").handler(new FormLoginHandlerImpl(_shiroAuth, "username", "password", "return_url", "/index"));


    //router.route("/access_token").handler(JWTAuthHandler.create(_jwtAuth));
    router.get("/access_token").handler(_normalAction::accessTokenHandler);

    router.get("/logout").handler(context -> {
      context.clearUser();
      context.response()
        .setStatusCode(302)
        .putHeader("Location", "/") .end();
    });



    return router;

  }
}
