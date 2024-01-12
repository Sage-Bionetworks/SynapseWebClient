package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class AccountLevelBadgesProps extends ReactComponentProps {

  String userId;

  @JsOverlay
  public static AccountLevelBadgesProps create(String userId) {
    AccountLevelBadgesProps props = new AccountLevelBadgesProps();
    props.userId = userId;
    return props;
  }
}
