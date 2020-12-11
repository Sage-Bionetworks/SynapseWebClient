package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name="Object")
public class EvaluationEditorProps extends ReactComponentProps  {
	@JsFunction
	public interface Callback {
		void run();
	}

	String sessionToken;
	String evaluationId;
	String entityId;
	boolean utc;
	Callback onDeleteSuccess;

	@JsOverlay
	public static EvaluationEditorProps create(String sessionToken, String evaluationId, String entityId, boolean utc, Callback onDeleteSuccess) {
		EvaluationEditorProps props = new EvaluationEditorProps();
		props.sessionToken = sessionToken;
		props.evaluationId = evaluationId;
		props.entityId = entityId;
		props.utc = utc;
		props.onDeleteSuccess = onDeleteSuccess;
		return props;
	}
}
