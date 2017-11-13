package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;

public interface HtmlView extends IsWidget {
	void setHtml(String html);
	void setSynAlert(IsWidget w);
	void setLoadingVisible(boolean visible);
}
