package org.sagebionetworks.web.client.widget.table.v2;

import com.google.gwt.user.client.ui.IsWidget;

public interface TotalVisibleResultsWidgetView extends IsWidget {
  void setVisible(boolean visible);

  void setTotalNumberOfResults(int count);

  void setNumberOfHiddenResults(int count);

  void setNumberOfHiddenResultsVisible(boolean visible);

  void setHelpMarkdown(String md);
}
