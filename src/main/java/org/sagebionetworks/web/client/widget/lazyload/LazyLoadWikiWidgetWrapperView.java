package org.sagebionetworks.web.client.widget.lazyload;

import org.sagebionetworks.web.client.widget.lazyload.SupportsLazyLoadInterface;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface LazyLoadWikiWidgetWrapperView extends IsWidget, SupportsLazyLoadInterface {
	void showLoading();
	void showWidget(Widget w);
}
