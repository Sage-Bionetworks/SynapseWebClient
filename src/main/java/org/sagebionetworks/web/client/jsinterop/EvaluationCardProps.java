package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EvaluationCardProps extends ReactComponentProps {

  @JsFunction
  public interface Callback {
    void run();
  }

  public EvaluationJSObject evaluation;

  public Callback onEdit;
  public Callback onModifyAccess;
  public Callback onSubmit;
  public Callback onDeleteSuccess;

  @JsOverlay
  public static EvaluationCardProps create(
    EvaluationJSObject evaluation,
    Callback onEdit,
    Callback onModifyAccess,
    Callback onSubmit,
    Callback onDeleteSuccess
  ) {
    EvaluationCardProps props = new EvaluationCardProps();
    props.evaluation = evaluation;
    props.onEdit = onEdit;
    props.onModifyAccess = onModifyAccess;
    props.onSubmit = onSubmit;
    props.onDeleteSuccess = onDeleteSuccess;
    return props;
  }
}
