package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class TermsAndConditionsProps extends ReactComponentProps {

  @JsFunction
  public interface Callback {
    void onFormChange(boolean formComplete);
  }

  Callback onFormChange;

  @JsOverlay
  public static TermsAndConditionsProps create(Callback onFormChange) {
    TermsAndConditionsProps props = new TermsAndConditionsProps();
    props.onFormChange = onFormChange;
    return props;
  }
}
