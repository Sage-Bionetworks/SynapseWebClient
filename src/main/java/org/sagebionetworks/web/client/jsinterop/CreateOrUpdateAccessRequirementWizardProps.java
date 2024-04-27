package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class CreateOrUpdateAccessRequirementWizardProps
  extends ReactComponentProps {

  @FunctionalInterface
  @JsFunction
  public interface OnComplete {
    void onComplete();
  }

  @FunctionalInterface
  @JsFunction
  public interface OnCancel {
    void onCancel();
  }

  boolean open;

  @JsNullable
  RestrictableObjectDescriptorJsObject subject;

  @JsNullable
  String accessRequirementId;

  @JsNullable
  OnComplete onComplete;

  @JsNullable
  OnCancel onCancel;

  @JsOverlay
  public static CreateOrUpdateAccessRequirementWizardProps create(
    boolean open,
    RestrictableObjectDescriptor subject,
    String accessRequirementId,
    OnComplete onComplete,
    OnCancel onCancel
  ) {
    CreateOrUpdateAccessRequirementWizardProps props =
      new CreateOrUpdateAccessRequirementWizardProps();
    props.open = open;
    if (subject != null) {
      props.subject = RestrictableObjectDescriptorJsObject.create(subject);
    }
    props.accessRequirementId = accessRequirementId;
    props.onComplete = onComplete;
    props.onCancel = onCancel;
    return props;
  }
}
