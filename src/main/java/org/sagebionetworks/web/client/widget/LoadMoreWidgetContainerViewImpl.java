package org.sagebionetworks.web.client.widget;

import java.util.Iterator;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LoadMoreWidgetContainerViewImpl implements LoadMoreWidgetContainerView{

	public interface Binder extends UiBinder<Widget, LoadMoreWidgetContainerViewImpl> {}

	@UiField
	Div container;
	@UiField
	HTMLPanel loadMore;
	@UiField
	Image loadMoreImage;

	Widget widget;

	@Inject
	public LoadMoreWidgetContainerViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void add(Widget w) {
		container.add(w);
	}
	@Override
	public Iterator<Widget> iterator() {
		return container.iterator();
	}
	
	@Override
	public boolean remove(Widget w) {
		return container.remove(w);
	}
	
	@Override
	public void clear() {
		container.clear();
	}

	@Override
	public void setLoadMoreVisibility(boolean visible) {
		loadMore.setVisible(visible);
	}

	@Override
	public boolean isLoadMoreAttached() {
		return loadMore.isAttached();
	}

	@Override
	public boolean isLoadMoreInViewport() {
		return DisplayUtils.isInViewport(loadMoreImage);
	}

	@Override
	public boolean getLoadMoreVisibility() {
		return loadMore.isVisible();
	}
}
