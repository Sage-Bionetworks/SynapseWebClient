package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;
import com.google.gwt.user.client.ui.IsWidget;

public interface TableQueryResultWikiView extends IsWidget, WidgetEditorView {
	void setSql(String sql);

	String getSql();

	Boolean isQueryVisible();

	void setQueryVisible(boolean isQueryVisible);
}
