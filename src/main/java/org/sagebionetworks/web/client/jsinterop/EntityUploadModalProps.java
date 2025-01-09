package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EntityUploadModalProps extends ReactComponentProps {

  @FunctionalInterface
  @JsFunction
  public interface Callback {
    void run();
  }

  public String entityId;

  public boolean open;

  public Callback onClose;

  @JsNullable
  public Callback onUploadReady;

  @JsNullable
  public ReactRef<EntityUploadHandle> ref;

  @JsOverlay
  public static EntityUploadModalProps create(
    String containerId,
    boolean open,
    Callback onClose,
    ReactRef<EntityUploadHandle> ref,
    Callback onUploadReady
  ) {
    EntityUploadModalProps props = new EntityUploadModalProps();
    props.entityId = containerId;
    props.open = open;
    props.onClose = onClose;
    props.ref = ref;
    props.onUploadReady = onUploadReady;
    return props;
  }
}
