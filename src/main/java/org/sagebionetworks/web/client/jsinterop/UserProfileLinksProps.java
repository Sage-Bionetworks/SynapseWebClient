package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class UserProfileLinksProps extends ReactComponentProps {

  String userId;

  @JsOverlay
  public static UserProfileLinksProps create(String userId) {
    UserProfileLinksProps props = new UserProfileLinksProps();
    props.userId = userId;
    return props;
  }
}
