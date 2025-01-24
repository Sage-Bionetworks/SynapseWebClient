package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EntityAclEditorModalProps extends ReactComponentProps {

  @JsFunction
  public interface Callback {
    void run();
  }

  public String entityId;
  public boolean open;
  public Callback onUpdateSuccess;
  public Callback onClose;
  public boolean isAfterUpload;

  @JsOverlay
  public static EntityAclEditorModalProps create(
    String entityId,
    boolean open,
    Callback onUpdateSuccess,
    Callback onClose,
    boolean isAfterUpload
  ) {
    EntityAclEditorModalProps props = new EntityAclEditorModalProps();

    props.entityId = entityId;
    props.open = open;
    props.onUpdateSuccess = onUpdateSuccess;
    props.onClose = onClose;
    props.isAfterUpload = isAfterUpload;
    return props;
  }
}
