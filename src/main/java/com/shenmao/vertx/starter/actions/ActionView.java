package com.shenmao.vertx.starter.actions;

public class ActionView {

  private String _view;
  public ActionView(String viewName) {
    _view = viewName;
  }

  public String view() {
    return _view;
  }
}
