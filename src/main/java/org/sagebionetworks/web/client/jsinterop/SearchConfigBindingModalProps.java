package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SearchConfigBindingModalProps extends ReactComponentProps {

  @FunctionalInterface
  @JsFunction
  public interface Callback {
    void run();
  }

  boolean open;
  String entityId;
  Callback onClose;

  @JsOverlay
  public static SearchConfigBindingModalProps create(
    boolean open,
    String entityId,
    Callback onClose
  ) {
    SearchConfigBindingModalProps props = new SearchConfigBindingModalProps();
    props.open = open;
    props.entityId = entityId;
    props.onClose = onClose;
    return props;
  }
}
