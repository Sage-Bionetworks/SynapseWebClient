package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.web.client.jsni.ReferenceJSNIObject;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EntityFileBrowserProps extends ReactComponentProps {

  @FunctionalInterface
  @JsFunction
  public interface OnSelectCallback {
    void run(ReferenceJSNIObject selected);
  }

  OnSelectCallback onSelect;
  String parentContainerId;

  @JsOverlay
  public static EntityFileBrowserProps create(
    String parentContainerId,
    OnSelectCallback onSelect
  ) {
    EntityFileBrowserProps props = new EntityFileBrowserProps();
    props.parentContainerId = parentContainerId;
    props.onSelect = onSelect;
    return props;
  }
}
