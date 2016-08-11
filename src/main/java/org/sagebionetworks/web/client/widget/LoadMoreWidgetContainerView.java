package org.sagebionetworks.web.client.widget;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;

public interface LoadMoreWidgetContainerView extends IsWidget, HasWidgets{
	void clear();
	void setLoadMoreVisibility(boolean visible);
	boolean isLoadMoreAttached();
	boolean isLoadMoreInViewport();
	boolean getLoadMoreVisibility();
}
