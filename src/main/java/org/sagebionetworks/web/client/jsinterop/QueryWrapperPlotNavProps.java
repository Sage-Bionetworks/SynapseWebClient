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

  @FunctionalInterface
  @JsFunction
  public interface OnViewSharingSettingsHandler {
    void onViewSharingSettingsClicked(String benefactorEntityId);
  }

  String name;
  String initQueryJson;
  String sql;

  @JsNullable
  OnQueryCallback onQueryChange;

  @JsNullable
  OnQueryResultBundleCallback onQueryResultBundleChange;

  @JsNullable
  OnViewSharingSettingsHandler onViewSharingSettingsClicked;

  @JsNullable
  boolean shouldDeepLink;

  @JsNullable
  String downloadCartPageUrl;

  @JsNullable
  boolean hideSqlEditorControl;

  @JsNullable
  SynapseTableProps tableConfiguration;

  @JsNullable
  boolean defaultShowFacetVisualization;

  boolean showLastUpdatedOn;

  @JsOverlay
  public static QueryWrapperPlotNavProps create(
    String sql,
    String initQueryJson,
    OnQueryCallback onQueryChange,
    OnQueryResultBundleCallback onQueryResultBundleChange,
    OnViewSharingSettingsHandler onViewSharingSettingsClicked,
    boolean hideSqlEditorControl
  ) {
    QueryWrapperPlotNavProps props = new QueryWrapperPlotNavProps();
    props.sql = sql;
    props.initQueryJson = initQueryJson;
    props.hideSqlEditorControl = hideSqlEditorControl;
    props.onQueryChange = onQueryChange;
    props.onQueryResultBundleChange = onQueryResultBundleChange;
    props.onViewSharingSettingsClicked = onViewSharingSettingsClicked;
    props.tableConfiguration = SynapseTableProps.create();
    props.shouldDeepLink = false;
    props.name = "Items";
    props.downloadCartPageUrl = "#!DownloadCart:0";
    props.showLastUpdatedOn = false;
    // SWC-6138 - hide charts by default
    props.defaultShowFacetVisualization = false;
    return props;
  }
}
