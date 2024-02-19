package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SqlDefinedTableEditorModalProps extends ReactComponentProps {

  @FunctionalInterface
  @JsFunction
  public interface Callback {
    void run();
  }

  public String entityId;
  public boolean open;

  @JsNullable
  public Callback onUpdate;

  @JsNullable
  public Callback onCancel;

  @JsOverlay
  public static SqlDefinedTableEditorModalProps create(
    String entityId,
    boolean open,
    Callback onUpdate,
    Callback onCancel
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
