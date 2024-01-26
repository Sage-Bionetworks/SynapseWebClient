package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class CreateTableViewWizardProps extends ReactComponentProps {

  @FunctionalInterface
  @JsFunction
  public interface OnComplete {
    void onComplete(String newEntityId);
  }

  @FunctionalInterface
  @JsFunction
  public interface OnCancel {
    void onCancel();
  }

  boolean open;
  String parentId;

  @JsNullable
  OnComplete onComplete;

  @JsNullable
  OnCancel onCancel;

  @JsOverlay
  public static CreateTableViewWizardProps create(
    boolean open,
    String parentId,
    OnComplete onComplete,
    OnCancel onCancel
  ) {
    CreateTableViewWizardProps props = new CreateTableViewWizardProps();
    props.open = open;
    props.parentId = parentId;
    props.onComplete = onComplete;
    props.onCancel = onCancel;
    return props;
  }
}
