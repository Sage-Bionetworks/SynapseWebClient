package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;
import com.google.gwt.user.client.ui.IsWidget;

public interface DetailsSummaryConfigView extends IsWidget, WidgetEditorView {
	public String getDetails();

	public String getSummary();
}
