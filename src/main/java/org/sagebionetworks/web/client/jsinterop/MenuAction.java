package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class MenuAction {

  @JsFunction
  public interface Callback {
    void run();
  }

  String field;
  Callback callback;

  @JsOverlay
  public static MenuAction create(String field, Callback callback) {
    MenuAction props = new MenuAction();
    props.field = field;
    props.callback = callback;
    return props;
  }
}
