package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ReviewerDashboardProps extends ReactComponentProps {

  @FunctionalInterface
  @JsFunction
  public interface OnRejectSubmission {
    void onReject(String reason);
  }

  @FunctionalInterface
  @JsFunction
  public interface OnRejectSubmissionClicked {
    void onRejectSubmissionClicked(OnRejectSubmission onReject);
  }

  ReviewerDashboardProps.OnRejectSubmissionClicked onRejectSubmissionClicked;

  @JsOverlay
  public static ReviewerDashboardProps create(
    OnRejectSubmissionClicked onRejectSubmissionClicked
  ) {
    ReviewerDashboardProps props = new ReviewerDashboardProps();
    props.onRejectSubmissionClicked = onRejectSubmissionClicked;
    return props;
  }
}
