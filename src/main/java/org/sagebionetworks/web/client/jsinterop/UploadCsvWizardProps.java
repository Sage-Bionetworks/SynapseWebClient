package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class UploadCsvWizardProps extends ReactComponentProps {

  @FunctionalInterface
  @JsFunction
  public interface OnComplete {
    void onComplete(String entityId);
  }

  @FunctionalInterface
  @JsFunction
  public interface OnClose {
    void onClose();
  }

  boolean open;

  @JsNullable
  String parentId;

  @JsNullable
  String tableId;

  @JsNullable
  OnComplete onComplete;

  @JsNullable
  OnClose onClose;

  @JsOverlay
  public static UploadCsvWizardProps create(
    boolean open,
    String parentId,
    String tableId,
    OnComplete onComplete,
    OnClose onClose
  ) {
    UploadCsvWizardProps props = new UploadCsvWizardProps();
    props.open = open;
    props.parentId = parentId;
    props.tableId = tableId;
    props.onComplete = onComplete;
    props.onClose = onClose;
    return props;
  }
}
