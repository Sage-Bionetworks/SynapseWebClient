package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SearchQueryWrapperPlotNavProps extends ReactComponentProps {

  String searchIndexId;

  @JsNullable
  String name;

  @JsNullable
  boolean defaultShowPlots;

  @JsNullable
  boolean defaultShowSearchBar;

  @JsNullable
  boolean hideCopyToClipboard;

  @JsNullable
  SynapseTableProps tableConfiguration;

  @JsOverlay
  public static SearchQueryWrapperPlotNavProps create(
    String searchIndexId,
    String name
  ) {
    SearchQueryWrapperPlotNavProps props = new SearchQueryWrapperPlotNavProps();
    props.searchIndexId = searchIndexId;
    props.name = name;
    props.defaultShowPlots = false;
    props.defaultShowSearchBar = true;
    SynapseTableProps tableConfig = SynapseTableProps.create();
    tableConfig.showDownloadColumn = false;
    props.tableConfiguration = tableConfig;
    return props;
  }
}
