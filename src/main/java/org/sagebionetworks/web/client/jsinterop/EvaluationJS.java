package org.sagebionetworks.web.client.jsinterop;

import java.util.Date;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.EvaluationStatus;
import org.sagebionetworks.gwt.client.schema.adapter.DateUtils;
import org.sagebionetworks.schema.FORMAT;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name="Object")
public class EvaluationJS {
	public String id;
	public String etag;
	public String name;
	public String description;
	public String ownerId;
	public String createdOn;
	public String contentSource;
	public String status;
	public String submissionInstructionsMessage;
	public String submissionReceiptMessage;
	// omit submissionQuota field because it is deprecated and will not be used in React.js



	@JsOverlay
	public static EvaluationJS fromEvaluation(Evaluation evaluation){
		if (evaluation == null){
			throw new IllegalArgumentException("evaluation can not be null");
		}
		EvaluationJS evaluationJS = new EvaluationJS();
		evaluationJS.id = evaluation.getId();
		evaluationJS.etag = evaluation.getEtag();
		evaluationJS.name = evaluation.getName();
		evaluationJS.description = evaluation.getDescription();
		evaluationJS.ownerId = evaluation.getOwnerId();
		evaluationJS.createdOn = DateUtils.convertDateToString(FORMAT.DATE_TIME,evaluation.getCreatedOn());
		evaluationJS.contentSource = evaluation.getContentSource();
		evaluationJS.status = evaluation.getStatus().name();
		evaluationJS.submissionInstructionsMessage = evaluation.getSubmissionInstructionsMessage();
		evaluationJS.submissionReceiptMessage = evaluation.getSubmissionReceiptMessage();
		return evaluationJS;
	}
}
