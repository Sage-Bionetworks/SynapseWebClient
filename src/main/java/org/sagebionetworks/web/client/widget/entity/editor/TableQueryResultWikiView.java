package org.sagebionetworks.web.client.widget.entity.editor;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.widget.WidgetEditorView;

public interface TableQueryResultWikiView extends IsWidget, WidgetEditorView {
  void setSql(String sql);

  String getSql();

  Boolean isQueryVisible();
  void setQueryVisible(boolean isQueryVisible);

  Boolean isShowTableOnly();
  void setIsShowTableOnly(boolean isShowTableOnly);
}
