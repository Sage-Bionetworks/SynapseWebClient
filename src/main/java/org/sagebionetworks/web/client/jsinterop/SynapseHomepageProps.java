package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SynapseHomepageProps extends ReactComponentProps {

  String projectViewId;

  @JsOverlay
  public static SynapseHomepageProps create(String projectViewId) {
    SynapseHomepageProps props = new SynapseHomepageProps();
    props.projectViewId = projectViewId;
    return props;
  }
}
