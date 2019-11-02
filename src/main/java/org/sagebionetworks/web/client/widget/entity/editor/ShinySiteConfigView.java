package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;
import com.google.gwt.user.client.ui.IsWidget;

public interface ShinySiteConfigView extends IsWidget, WidgetEditorView {
	void configure(String url, int height, boolean isIncludePrincipalId);

	String getSiteUrl();

	Integer getSiteHeight();

	Boolean isIncludePrincipalId();
}
