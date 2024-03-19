package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class AccessRequirementAclEditorProps extends ReactComponentProps {

  @JsFunction
  public interface BooleanCallback {
    void run(boolean arg);
  }

  String accessRequirementId;
  BooleanCallback onSaveComplete;
  Object ref;

  @JsOverlay
  public static AccessRequirementAclEditorProps create(
    String accessRequirementId,
    BooleanCallback onSaveComplete,
    Object ref
  ) {
    AccessRequirementAclEditorProps props =
      new AccessRequirementAclEditorProps();
    props.accessRequirementId = accessRequirementId;
    props.onSaveComplete = onSaveComplete;
    props.ref = ref;
    return props;
  }
}
