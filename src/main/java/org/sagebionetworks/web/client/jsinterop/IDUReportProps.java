package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class IDUReportProps extends ReactComponentProps {

  String accessRequirementId;

  @JsOverlay
  public static IDUReportProps create(String accessRequirementId) {
    IDUReportProps props = new IDUReportProps();
    props.accessRequirementId = accessRequirementId;
    return props;
  }
}
