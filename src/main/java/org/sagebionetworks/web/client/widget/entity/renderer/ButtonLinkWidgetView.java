package org.sagebionetworks.web.client.widget.entity.renderer;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.IsWidget;

public interface ButtonLinkWidgetView extends IsWidget {
	void configure(WikiPageKey wikiKey, String buttonText, String url, boolean isHighlight, boolean openInNewWindow);

	void setWidth(String width);

	void setSize(ButtonSize size);

	void addStyleNames(String styleNames);
}
