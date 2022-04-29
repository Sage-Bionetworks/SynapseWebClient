package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;
import com.google.gwt.user.client.ui.IsWidget;

public interface TableQueryResultWikiView extends IsWidget, WidgetEditorView {
	void setSql(String sql);

	String getSql();

	@Deprecated
	Boolean isQueryVisible();
	@Deprecated
	void setQueryVisible(boolean isQueryVisible);
	
	Boolean isShowTableOnly();
	void setIsShowTableOnly(boolean isShowTableOnly);

}
