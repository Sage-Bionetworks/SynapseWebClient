package org.sagebionetworks.web.client.widget;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;

public interface LoadMoreWidgetContainerView extends IsWidget, HasWidgets {
	void clear();

	void setLoadMoreVisibility(boolean visible);

	void setIsProcessing(boolean isProcessing);

	void addStyleName(String styles);

	void setPresenter(Presenter p);

	public interface Presenter {
		void onLoadMore();
	}
}
