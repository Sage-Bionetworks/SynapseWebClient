package org.sagebionetworks.web.client.jsinterop;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class DownloadConfirmationProps extends ReactComponentProps {

  @FunctionalInterface
  @JsFunction
  public interface Callback {
    void run();
  }

  @FunctionalInterface
  @JsFunction
  public interface GetQueryRequest {
    JavaScriptObject run();
  }

  @JsNullable
  GetQueryRequest getLastQueryRequest;

  @JsNullable
  String folderId;

  @JsNullable
  Callback fnClose;

  @JsNullable
  String downloadCartPageUrl;

  @JsOverlay
  public static DownloadConfirmationProps create(
    String queryBundleRequestJson,
    String folderId,
    Callback onClose
  ) {
    DownloadConfirmationProps props = new DownloadConfirmationProps();
    props.downloadCartPageUrl = "/#!DownloadCart:0";
    props.getLastQueryRequest =
      new GetQueryRequest() {
        @Override
        public JavaScriptObject run() {
          if (queryBundleRequestJson != null) return JsonUtils.safeEval(
            queryBundleRequestJson
          ); else return null;
        }
      };
    props.folderId = folderId;
    props.fnClose = onClose;
    return props;
  }
}
