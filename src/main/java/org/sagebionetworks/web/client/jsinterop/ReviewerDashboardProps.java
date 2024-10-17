package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ReviewerDashboardProps extends ReactComponentProps {

  String routerBaseName;

  @JsOverlay
  public static ReviewerDashboardProps create(String routerBaseName) {
    ReviewerDashboardProps props = new ReviewerDashboardProps();
    props.routerBaseName = routerBaseName;
    return props;
  }
}
