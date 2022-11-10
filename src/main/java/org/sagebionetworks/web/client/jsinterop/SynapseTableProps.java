package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SynapseTableProps extends ReactComponentProps {

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

  @JsOverlay
  public static SynapseTableProps create(
    int visibleColumnCount,
    String title,
    boolean showAccessColumn,
    boolean showDownloadColumn,
    boolean hideDownload,
    boolean isRowSelectionVisible
  ) {
    SynapseTableProps props = new SynapseTableProps();
    props.visibleColumnCount = visibleColumnCount;
    props.title = title;
    props.showAccessColumn = showAccessColumn;
    props.showDownloadColumn = showDownloadColumn;
    props.hideDownload = hideDownload;
    props.isRowSelectionVisible = isRowSelectionVisible;
    return props;
  }

  @JsOverlay
  public static SynapseTableProps create() {
    SynapseTableProps props = new SynapseTableProps();
    return props;
  }
}
