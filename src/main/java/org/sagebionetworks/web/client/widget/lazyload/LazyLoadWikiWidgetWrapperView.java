package org.sagebionetworks.web.client.widget.lazyload;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface LazyLoadWikiWidgetWrapperView extends IsWidget, SupportsLazyLoadInterface {
	void showLoading();

	void showWidget(Widget w, String cssSelector);

	void showError(String text);
}
