package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.repo.model.verification.VerificationStateEnum;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class RejectProfileValidationRequestModalProps
  extends ReactComponentProps {

  @JsFunction
  public interface Callback {
    void run();
  }

  /* SynID of the table which contains the email responses which should populate this modal. */
  public String tableId;
  public boolean open;
  public String verificationSubmissionId;
  public String currentState;
  public Callback onRejectionSubmittedSuccess;
  public Callback onClose;

  @JsOverlay
  public static RejectProfileValidationRequestModalProps create(
    String verificationSubmissionId,
    VerificationStateEnum currentState,
    String tableId,
    boolean open,
    Callback onRejectionSubmittedSuccess,
    Callback onClose
  ) {
    RejectProfileValidationRequestModalProps props =
      new RejectProfileValidationRequestModalProps();

    props.verificationSubmissionId = verificationSubmissionId;
    props.currentState = currentState.toString();
    props.tableId = tableId;
    props.open = open;
    props.onRejectionSubmittedSuccess = onRejectionSubmittedSuccess;
    props.onClose = onClose;
    return props;
  }
}
