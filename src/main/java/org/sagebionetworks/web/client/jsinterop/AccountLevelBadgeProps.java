package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class AccountLevelBadgeProps extends ReactComponentProps {

  String userId;

  @JsOverlay
  public static AccountLevelBadgeProps create(String userId) {
    AccountLevelBadgeProps props = new AccountLevelBadgeProps();
    props.userId = userId;
    return props;
  }
}
