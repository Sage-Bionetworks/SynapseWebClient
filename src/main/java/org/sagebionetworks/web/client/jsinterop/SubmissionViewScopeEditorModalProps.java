package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SubmissionViewScopeEditorModalProps extends ReactComponentProps {

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
  public static SubmissionViewScopeEditorModalProps create(
    String entityId,
    SubmissionViewScopeEditorModalProps.Callback onUpdate,
    SubmissionViewScopeEditorModalProps.Callback onCancel,
    boolean open
  ) {
    SubmissionViewScopeEditorModalProps props =
      new SubmissionViewScopeEditorModalProps();
    props.entityId = entityId;
    props.onUpdate = onUpdate;
    props.onCancel = onCancel;
    props.open = open;
    return props;
  }
}
