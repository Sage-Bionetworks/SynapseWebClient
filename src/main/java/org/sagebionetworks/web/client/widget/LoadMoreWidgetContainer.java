package org.sagebionetworks.web.client.widget;

import java.util.Iterator;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.utils.Callback;

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
	GWTWrapper gwt;
	
	@Inject
	public LoadMoreWidgetContainer(LoadMoreWidgetContainerView view, GWTWrapper gwt) {
		this.view = view;
		this.gwt = gwt;
		
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
			return;
		} else if (view.isLoadMoreInViewport() && view.getLoadMoreVisibility()) {
			//try to load data!
			callback.invoke();
		} else {
			//wait for a few seconds and see if we should load data
			gwt.scheduleExecution(invokeCheckForInViewAndLoadData, DisplayConstants.DELAY_UNTIL_IN_VIEW);
		}
	}
	
	public void setIsMore(boolean isMore) {
		view.setLoadMoreVisibility(isMore);
		if (isMore) {
			gwt.scheduleExecution(invokeCheckForInViewAndLoadData, DisplayConstants.DELAY_UNTIL_IN_VIEW);
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
}
