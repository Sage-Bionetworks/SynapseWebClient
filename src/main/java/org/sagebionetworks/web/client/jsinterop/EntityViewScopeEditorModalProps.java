package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EntityViewScopeEditorModalProps extends ReactComponentProps {

  @FunctionalInterface
  @JsFunction
  public interface Callback {
    void run();
  }

  public String entityId;

  @JsNullable
  public Callback onUpdate;

  @JsNullable
  public Callback onCancel;

  public boolean open;

  @JsOverlay
  public static EntityViewScopeEditorModalProps create(
    String entityId,
    Callback onUpdate,
    Callback onCancel,
    boolean open
  ) {
    EntityViewScopeEditorModalProps props =
      new EntityViewScopeEditorModalProps();
    props.entityId = entityId;
    props.onUpdate = onUpdate;
    props.onCancel = onCancel;
    props.open = open;
    return props;
  }
}
// TODO: create EntityViewScopeEditorModalWidget, View, and IMPL
