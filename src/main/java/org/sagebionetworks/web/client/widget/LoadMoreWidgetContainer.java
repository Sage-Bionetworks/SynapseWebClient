package org.sagebionetworks.web.client.widget;

import java.util.Iterator;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * This is a widget container that will call you back if the Loading spinner is in the viewport (and
 * visible). Tell it if there are more items to load by calling setIsMore() with true or false.
 * 
 * @author jayhodgson
 *
 */
public class LoadMoreWidgetContainer implements IsWidget, HasWidgets, LoadMoreWidgetContainerView.Presenter {
	LoadMoreWidgetContainerView view;
	Callback callback;

	@Inject
	public LoadMoreWidgetContainer(LoadMoreWidgetContainerView view) {
		this.view = view;
		view.setPresenter(this);
	}

	public void configure(Callback loadMoreCallback) {
		this.callback = loadMoreCallback;
	}

	@Override
	public void onLoadMore() {
		// try to load data!
		setIsMore(false);
		view.setIsProcessing(true);
		callback.invoke();
	}

	public void setIsMore(boolean isMore) {
		view.setIsProcessing(false);
		view.setLoadMoreVisibility(isMore);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void add(Widget w) {
		view.add(w);
	}

	@Override
	public void clear() {
		view.clear();
		setIsMore(false);
	}

	@Override
	public Iterator<Widget> iterator() {
		return view.iterator();
	}

	@Override
	public boolean remove(Widget w) {
		return view.remove(w);
	}

	public void setIsProcessing(boolean isProcessing) {
		view.setIsProcessing(isProcessing);
	}

	public void addStyleName(String styles) {
		view.addStyleName(styles);
	}
}
