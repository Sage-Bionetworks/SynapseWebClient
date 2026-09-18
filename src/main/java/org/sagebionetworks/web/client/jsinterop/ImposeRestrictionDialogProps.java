package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ImposeRestrictionDialogProps extends ReactComponentProps {

  @JsFunction
  public interface Callback {
    void run();
  }

  String entityId;
  boolean open;
  Callback onClose;
  Callback onSuccess;

  @JsOverlay
  public static ImposeRestrictionDialogProps create(
    String entityId,
    boolean open,
    Callback onClose,
    Callback onSuccess
  ) {
    ImposeRestrictionDialogProps props = new ImposeRestrictionDialogProps();
    props.entityId = entityId;
    props.open = open;
    props.onClose = onClose;
    props.onSuccess = onSuccess;
    // Reset the modal state when it is closed
    props.key = Boolean.toString(open);
    return props;
  }
}
