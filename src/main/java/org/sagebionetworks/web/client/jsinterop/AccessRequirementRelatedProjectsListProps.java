package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class AccessRequirementRelatedProjectsListProps
  extends ReactComponentProps {

  String accessRequirementId;

  @JsOverlay
  public static AccessRequirementRelatedProjectsListProps create(
    String accessRequirementId
  ) {
    AccessRequirementRelatedProjectsListProps props =
      new AccessRequirementRelatedProjectsListProps();
    props.accessRequirementId = accessRequirementId;
    return props;
  }
}
