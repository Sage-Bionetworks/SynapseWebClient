package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.gwt.client.schema.adapter.DateUtils;
import org.sagebionetworks.schema.FORMAT;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EvaluationJSObject {

  public String id;
  public String etag;
  public String name;
  public String description;
  public String ownerId;
  public String createdOn;
  public String contentSource;
  public String submissionInstructionsMessage;
  public String submissionReceiptMessage;

  // omit "submissionQuota" and "status" fields because they are deprecated and will not be used in React.js

  @JsOverlay
  public static EvaluationJSObject fromEvaluation(Evaluation evaluation) {
    if (evaluation == null) {
      throw new IllegalArgumentException("evaluation can not be null");
    }
    EvaluationJSObject evaluationJSObject = new EvaluationJSObject();
    evaluationJSObject.id = evaluation.getId();
    evaluationJSObject.etag = evaluation.getEtag();
    evaluationJSObject.name = evaluation.getName();
    evaluationJSObject.description = evaluation.getDescription();
    evaluationJSObject.ownerId = evaluation.getOwnerId();
    evaluationJSObject.createdOn =
      DateUtils.convertDateToString(
        FORMAT.DATE_TIME,
        evaluation.getCreatedOn()
      );
    evaluationJSObject.contentSource = evaluation.getContentSource();
    evaluationJSObject.submissionInstructionsMessage =
      evaluation.getSubmissionInstructionsMessage();
    evaluationJSObject.submissionReceiptMessage =
      evaluation.getSubmissionReceiptMessage();
    return evaluationJSObject;
  }
}
