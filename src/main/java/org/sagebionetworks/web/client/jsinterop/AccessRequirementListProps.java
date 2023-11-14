package org.sagebionetworks.web.client.jsinterop;

import java.util.List;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.repo.model.AccessRequirement;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class AccessRequirementListProps extends ReactComponentProps {

  @JsFunction
  public interface Callback {
    void run();
  }

  boolean renderAsModal;
  Object[] accessRequirementFromProps;
  Callback onHide;
  String entityId;

  @JsOverlay
  public static AccessRequirementListProps create(
    Callback onHide,
    List<AccessRequirement> accessRequirements,
    String entityId
  ) {
    AccessRequirementListProps props = new AccessRequirementListProps();
    props.renderAsModal = true;
    props.onHide = onHide;
    props.entityId = entityId;
    props.accessRequirementFromProps =
      accessRequirements
        .stream()
        .map(JSONEntityUtils::toJsInteropCompatibleObject)
        .toArray();
    return props;
  }
}
