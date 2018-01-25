package org.sagebionetworks.web.client.widget;

import java.util.Iterator;

import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadCallbackQueue;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * This is a widget container that will call you back if the Loading spinner is in the viewport (and visible).
 * Tell it if there are more items to load by calling setIsMore() with true or false.
 * 
 * @author jayhodgson
 *
 */
public class LoadMoreWidgetContainer implements IsWidget, HasWidgets {
	LoadMoreWidgetContainerView view;
	Callback callback, invokeCheckForInViewAndLoadData;
	boolean isProcessing;
	LazyLoadCallbackQueue lazyLoadCallbackQueue;
	
	@Inject
	public LoadMoreWidgetContainer(LoadMoreWidgetContainerView view, LazyLoadCallbackQueue lazyLoadCallbackQueue) {
		this.view = view;
		this.lazyLoadCallbackQueue = lazyLoadCallbackQueue;
		this.isProcessing = false;
		invokeCheckForInViewAndLoadData = new Callback() {
			@Override
			public void invoke() {
				checkForInViewAndLoadData();
			}
		};
	}
	
	public void configure(Callback loadMoreCallback) {
		this.callback = loadMoreCallback;
	}
	
	public void checkForInViewAndLoadData() {
		if (!view.isLoadMoreAttached()) {
			//Done, view has been detached and widget was never in the viewport
			setIsMore(false);
			return;
		} else if (view.isLoadMoreInViewport() && view.getLoadMoreVisibility() && !isProcessing) {
			//try to load data!
			isProcessing = true;
			setIsMore(false);
			callback.invoke();
		}
	}
	
	public void setIsMore(boolean isMore) {
		isProcessing = false;
		view.setLoadMoreVisibility(isMore);
		if (isMore) {
			lazyLoadCallbackQueue.subscribe(invokeCheckForInViewAndLoadData);
		} else {
			lazyLoadCallbackQueue.unsubscribe(invokeCheckForInViewAndLoadData);
		}
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
	}

	@Override
	public Iterator<Widget> iterator() {
		return view.iterator();
	}

	@Override
	public boolean remove(Widget w) {
		return view.remove(w);
	}

	/*
	 * for test only
	 */
	public void setIsProcessing(boolean isProcessing) {
		this.isProcessing = isProcessing;
	}
}
