package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SqlDefinedTableEditorModalProps extends ReactComponentProps {

  // going to match the props sent from src
  @FunctionalInterface
  @JsFunction
  public interface OnUpdate {
    void OnUpdate();
  }

  @FunctionalInterface
  @JsFunction
  public interface OnCancel {
    void onCancel();
  }

  public String entityId;
  public boolean open;

  @JsNullable
  public OnUpdate onUpdate;

  @JsNullable
  public OnCancel onCancel;

  @JsOverlay
  public static SqlDefinedTableEditorModalProps create(
    String entityId,
    boolean open,
    OnUpdate onUpdate,
    OnCancel onCancel
  ) {
    SqlDefinedTableEditorModalProps props =
      new SqlDefinedTableEditorModalProps();
    props.entityId = entityId;
    props.open = open;
    props.onUpdate = onUpdate;
    props.onCancel = onCancel;
    return props;
  }
}
