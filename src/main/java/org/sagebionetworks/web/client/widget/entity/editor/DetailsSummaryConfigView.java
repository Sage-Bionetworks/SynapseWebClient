package org.sagebionetworks.web.client.widget.entity.editor;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.widget.WidgetEditorView;

public interface DetailsSummaryConfigView extends IsWidget, WidgetEditorView {
  public String getDetails();

  public String getSummary();
}
