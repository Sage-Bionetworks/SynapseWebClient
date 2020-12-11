package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name="Object")
public class EvaluationRoundEditorListProps extends ReactComponentProps {
	String sessionToken;
	String evaluationId;
	boolean utc;

	@JsOverlay
	public static EvaluationRoundEditorListProps create(String sessionToken, String evaluationId, boolean utc) {
		EvaluationRoundEditorListProps props = new EvaluationRoundEditorListProps();
		props.sessionToken = sessionToken;
		props.evaluationId = evaluationId;
		props.utc = utc;
		return props;
	}
}
