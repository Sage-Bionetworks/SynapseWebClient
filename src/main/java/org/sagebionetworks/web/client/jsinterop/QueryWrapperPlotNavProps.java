package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class QueryWrapperPlotNavProps extends ReactComponentProps {

  @FunctionalInterface
  @JsFunction
  public interface OnQueryCallback {
    void run(String newQueryJson);
  }

  @FunctionalInterface
  @JsFunction
  public interface OnQueryResultBundleCallback {
    void run(String newQueryResultBundleJson);
  }

  String name;
  String initQueryJson;
  String sql;

  @JsNullable
  OnQueryCallback onQueryChange;

  @JsNullable
  OnQueryResultBundleCallback onQueryResultBundleChange;

  @JsNullable
  boolean shouldDeepLink;

  @JsNullable
  String downloadCartPageUrl;

  @JsNullable
  boolean hideSqlEditorControl;

  @JsNullable
  SynapseTableProps tableConfiguration;

  @JsNullable
  boolean defaultShowPlots;

  @JsNullable
  boolean defaultShowSearchBox;

  @JsNullable
  boolean hideCopyToClipboard;

  @JsNullable
  boolean hideDownload;

  boolean showLastUpdatedOn;

  @JsNullable
  CardConfiguration cardConfiguration;

  @JsOverlay
  public static QueryWrapperPlotNavProps create(
    String sql,
    String initQueryJson,
    OnQueryCallback onQueryChange,
    OnQueryResultBundleCallback onQueryResultBundleChange,
    boolean hideSqlEditorControl,
    Boolean defaultShowPlots,
    Boolean defaultShowSearchBox,
    Boolean hideCopyToClipboard,
    Boolean hideDownload,
    SynapseTableProps tableConfiguration,
    CardConfiguration cardConfiguration,
    String name
  ) {
    QueryWrapperPlotNavProps props = new QueryWrapperPlotNavProps();
    props.sql = sql;
    props.initQueryJson = initQueryJson;
    props.hideSqlEditorControl = hideSqlEditorControl;
    props.onQueryChange = onQueryChange;
    props.onQueryResultBundleChange = onQueryResultBundleChange;
    if (tableConfiguration != null) {
      props.tableConfiguration = tableConfiguration;
    }
    props.shouldDeepLink = false;
    props.name = "Items";
    if (name != null) {
      props.name = name;
    }
    props.downloadCartPageUrl = "DownloadCart:0";
    props.showLastUpdatedOn = false;
    // SWC-6138 - hide charts by default
    props.defaultShowPlots = false;
    if (defaultShowPlots != null) {
      //unbox
      props.defaultShowPlots = defaultShowPlots;
    }
    if (defaultShowSearchBox != null) {
      //unbox
      props.defaultShowSearchBox = defaultShowSearchBox;
    }
    if (hideCopyToClipboard != null) {
      //unbox
      props.hideCopyToClipboard = hideCopyToClipboard;
    }
    if (hideDownload != null) {
      //unbox
      props.hideDownload = hideDownload;
    }
    props.cardConfiguration = cardConfiguration;
    return props;
  }
}
