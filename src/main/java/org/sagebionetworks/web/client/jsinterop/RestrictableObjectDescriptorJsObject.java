package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class RestrictableObjectDescriptorJsObject extends ReactComponentProps {

  String id;
  String type;

  @JsOverlay
  public static RestrictableObjectDescriptorJsObject create(
    RestrictableObjectDescriptor rod
  ) {
    RestrictableObjectDescriptorJsObject props =
      new RestrictableObjectDescriptorJsObject();
    props.id = rod.getId();
    props.type = rod.getType().toString();
    return props;
  }
}
