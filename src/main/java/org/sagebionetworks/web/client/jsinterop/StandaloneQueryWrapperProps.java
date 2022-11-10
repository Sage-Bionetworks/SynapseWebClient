package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class StandaloneQueryWrapperProps extends ReactComponentProps {

  String sql;

  @JsNullable
  String unitDescription;

  @JsNullable
  int visibleColumnCount;

  @JsNullable
  String title;

  @JsNullable
  boolean showAccessColumn;

  @JsNullable
  boolean showDownloadColumn;

  @JsNullable
  boolean hideDownload;

  @JsNullable
  boolean isRowSelectionVisible;

  boolean showLastUpdatedOn;

  @JsOverlay
  public static StandaloneQueryWrapperProps create(String sql) {
    StandaloneQueryWrapperProps props = new StandaloneQueryWrapperProps();
    props.sql = sql;
    props.unitDescription = null;
    props.showLastUpdatedOn = true;
    return props;
  }

  @JsOverlay
  public static StandaloneQueryWrapperProps create(
    String sql,
    int visibleColumnCount,
    String title,
    boolean showAccessColumn,
    boolean showDownloadColumn,
    boolean hideDownload,
    boolean isRowSelectionVisible
  ) {
    StandaloneQueryWrapperProps props = new StandaloneQueryWrapperProps();
    props.sql = sql;
    props.visibleColumnCount = visibleColumnCount;
    props.title = title;
    props.showAccessColumn = showAccessColumn;
    props.showDownloadColumn = showDownloadColumn;
    props.hideDownload = hideDownload;
    props.isRowSelectionVisible = isRowSelectionVisible;
    props.showLastUpdatedOn = true;
    return props;
  }

  @JsOverlay
  public static StandaloneQueryWrapperProps create() {
    StandaloneQueryWrapperProps props = new StandaloneQueryWrapperProps();
    props.showLastUpdatedOn = true;
    return props;
  }
}
