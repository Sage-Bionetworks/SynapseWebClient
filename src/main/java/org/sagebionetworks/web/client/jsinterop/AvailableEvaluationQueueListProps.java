package org.sagebionetworks.web.client.jsinterop;

import java.util.List;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.utils.CallbackP;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class AvailableEvaluationQueueListProps extends ReactComponentProps {

  EvaluationJSObject[] evaluations;
  boolean isSelectable;

  @FunctionalInterface
  @JsFunction
  public interface OnChangeSelectedEvaluationCallback {
    void onChangeSelectedEvaluation(EvaluationJSObject evaluationJsObject);
  }

  @JsNullable
  OnChangeSelectedEvaluationCallback onChangeSelectedEvaluation;

  @JsOverlay
  public static AvailableEvaluationQueueListProps create(
    List<Evaluation> evaluations,
    boolean isSelectable,
    CallbackP<Evaluation> onChangeSelectedEvaluation
  ) {
    AvailableEvaluationQueueListProps props =
      new AvailableEvaluationQueueListProps();

    props.evaluations = new EvaluationJSObject[evaluations.size()];
    for (int i = 0; i < evaluations.size(); i++) {
      EvaluationJSObject newEvaluation = EvaluationJSObject.fromEvaluation(
        evaluations.get(i)
      );
      props.evaluations[i] = newEvaluation;
    }

    props.isSelectable = isSelectable;

    props.onChangeSelectedEvaluation =
      evaluationJsObject -> {
        Evaluation selectedEvaluation = null;
        if (evaluationJsObject != null) {
          for (Evaluation evaluation : evaluations) {
            if (evaluation.getId().equals(evaluationJsObject.id)) {
              selectedEvaluation = evaluation;
              break;
            }
          }
        }
        onChangeSelectedEvaluation.invoke(selectedEvaluation);
      };
    return props;
  }
}
