package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;

public interface WikiSubpagesOrderEditorView extends IsWidget {
	void configure(WikiSubpageOrderEditorTree subpageTree);

	void setSynAlert(IsWidget w);

	void initializeState();

	void setLoadingVisible(boolean visible);
}
