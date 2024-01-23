package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class TableColumnSchemaEditorProps extends ReactComponentProps {

  @FunctionalInterface
  @JsFunction
  public interface OnColumnsUpdated {
    void onColumnsUpdated();
  }

  @FunctionalInterface
  @JsFunction
  public interface OnCancel {
    void onCancel();
  }

  public String entityId;
  public boolean open;

  @JsNullable
  public OnColumnsUpdated onColumnsUpdated;

  @JsNullable
  public OnCancel onCancel;

  @JsOverlay
  public static TableColumnSchemaEditorProps create(
    String entityId,
    boolean open,
    OnColumnsUpdated onColumnsUpdated,
    OnCancel onCancel
  ) {
    TableColumnSchemaEditorProps props = new TableColumnSchemaEditorProps();
    props.entityId = entityId;
    props.open = open;
    props.onColumnsUpdated = onColumnsUpdated;
    props.onCancel = onCancel;
    return props;
  }
}
