package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SearchIndexEditorModalProps extends ReactComponentProps {

  @FunctionalInterface
  @JsFunction
  public interface OnComplete {
    void onComplete(String entityId);
  }

  @FunctionalInterface
  @JsFunction
  public interface Callback {
    void run();
  }

  boolean open;
  String entityId;
  OnComplete onComplete;
  Callback onCancel;

  @JsOverlay
  public static SearchIndexEditorModalProps create(
    boolean open,
    String entityId,
    OnComplete onComplete,
    Callback onCancel
  ) {
    SearchIndexEditorModalProps props = new SearchIndexEditorModalProps();
    props.open = open;
    props.entityId = entityId;
    props.onComplete = onComplete;
    props.onCancel = onCancel;
    return props;
  }
}
