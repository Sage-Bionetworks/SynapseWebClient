package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class CsvPreviewDialogProps extends ReactComponentProps {

  public boolean open;
  public String parentId;

  @JsNullable
  public String tableId;

  @JsNullable
  public OnSuccessFunction onSuccess;

  public OnCloseFunction onClose;

  @JsFunction
  @FunctionalInterface
  public interface OnSuccessFunction {
    void onSuccess();
  }

  @JsFunction
  @FunctionalInterface
  public interface OnCloseFunction {
    void onClose();
  }

  @JsOverlay
  public static CsvPreviewDialogProps create(
    boolean open,
    String parentId,
    String tableId,
    OnSuccessFunction onSuccess,
    OnCloseFunction onClose
  ) {
    CsvPreviewDialogProps props = new CsvPreviewDialogProps();
    props.open = open;
    props.parentId = parentId;
    props.tableId = tableId;
    props.onSuccess = onSuccess;
    props.onClose = onClose;
    return props;
  }
}
