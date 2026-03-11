package org.sagebionetworks.web.client.jsinterop;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class AddToDownloadListConfirmationAlertProps
  extends ReactComponentProps {

  @FunctionalInterface
  @JsFunction
  public interface Callback {
    void run();
  }

  @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
  public static class JsAddToDownloadListRequest {

    public String concreteType;

    @JsNullable
    public JavaScriptObject query;

    @JsNullable
    public String parentId;

    @JsNullable
    public Boolean recursive;

    @JsNullable
    public Boolean useVersionNumber;
  }

  public JsAddToDownloadListRequest addToDownloadListRequest;
  public Callback onClose;

  @JsOverlay
  public static AddToDownloadListConfirmationAlertProps createForContainer(
    String containerId,
    Callback onClose
  ) {
    AddToDownloadListConfirmationAlertProps props =
      new AddToDownloadListConfirmationAlertProps();
    JsAddToDownloadListRequest request = new JsAddToDownloadListRequest();
    request.concreteType =
      "org.sagebionetworks.repo.model.download.AddToDownloadListRequest";
    request.parentId = containerId;
    request.recursive = true;
    props.addToDownloadListRequest = request;
    props.onClose = onClose;
    return props;
  }

  @JsOverlay
  public static AddToDownloadListConfirmationAlertProps createForQuery(
    String queryJson,
    Callback onClose
  ) {
    AddToDownloadListConfirmationAlertProps props =
      new AddToDownloadListConfirmationAlertProps();
    JsAddToDownloadListRequest request = new JsAddToDownloadListRequest();
    request.concreteType =
      "org.sagebionetworks.repo.model.download.AddToDownloadListRequest";
    request.query = JsonUtils.safeEval(queryJson);
    props.addToDownloadListRequest = request;
    props.onClose = onClose;
    return props;
  }
}
