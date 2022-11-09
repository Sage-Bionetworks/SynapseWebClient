package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EvaluationEditorPageProps extends ReactComponentProps {

  @JsFunction
  public interface Callback {
    void run();
  }

  String evaluationId;
  String entityId;
  Callback onDeleteSuccess;

  @JsOverlay
  public static EvaluationEditorPageProps create(
    String evaluationId,
    String entityId,
    Callback onDeleteSuccess
  ) {
    if (
      (evaluationId != null && entityId != null) ||
      (evaluationId == null && entityId == null)
    ) {
      throw new IllegalArgumentException(
        "Either evaluationId (non-null means edit existing) or entityId (non-null means create new) must be null, but not both"
      );
    }

    EvaluationEditorPageProps props = new EvaluationEditorPageProps();
    props.evaluationId = evaluationId;
    props.entityId = entityId;
    props.onDeleteSuccess = onDeleteSuccess;
    return props;
  }
}
