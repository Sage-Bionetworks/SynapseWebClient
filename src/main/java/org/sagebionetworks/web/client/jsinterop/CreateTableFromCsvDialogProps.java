package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class CreateTableFromCsvDialogProps extends ReactComponentProps {

  public boolean open;
  public String parentId;

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
  public static CreateTableFromCsvDialogProps create(
    boolean open,
    String parentId,
    OnSuccessFunction onSuccess,
    OnCloseFunction onClose
  ) {
    CreateTableFromCsvDialogProps props = new CreateTableFromCsvDialogProps();
    props.open = open;
    props.parentId = parentId;
    props.onSuccess = onSuccess;
    props.onClose = onClose;

    // Reset modal state by remounting when open changes.
    props.key = Boolean.toString(open);

    return props;
  }
}
