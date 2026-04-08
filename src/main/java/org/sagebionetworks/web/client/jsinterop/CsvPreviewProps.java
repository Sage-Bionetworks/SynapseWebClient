package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class CsvPreviewProps extends ReactComponentProps {

  @JsFunction
  @FunctionalInterface
  public interface OnCsvPreviewDataChangeFunction {
    void onCsvPreviewDataChange(UploadToTablePreviewResultJsObject data);
  }

  @JsFunction
  @FunctionalInterface
  public interface OnIsLoadingChangeFunction {
    void onIsLoadingChange(boolean isLoading);
  }

  public String fileHandleId;
  public CsvTableDescriptorJsObject csvTableDescriptor;

  @JsNullable
  public OnCsvPreviewDataChangeFunction onCsvPreviewDataChange;

  @JsNullable
  public OnIsLoadingChangeFunction onIsLoadingChange;

  @JsOverlay
  public static CsvPreviewProps create(
    String fileHandleId,
    CsvTableDescriptorJsObject csvTableDescriptor,
    OnCsvPreviewDataChangeFunction onCsvPreviewDataChange,
    OnIsLoadingChangeFunction onIsLoadingChange
  ) {
    CsvPreviewProps props = new CsvPreviewProps();
    props.fileHandleId = fileHandleId;
    props.csvTableDescriptor = csvTableDescriptor;
    props.onCsvPreviewDataChange = onCsvPreviewDataChange;
    props.onIsLoadingChange = onIsLoadingChange;
    return props;
  }
}
