package org.sagebionetworks.web.client.widget;

import java.util.Iterator;
import org.gwtbootstrap3.client.ui.html.Div;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LoadMoreWidgetContainerViewImpl implements LoadMoreWidgetContainerView {

	public interface Binder extends UiBinder<Widget, LoadMoreWidgetContainerViewImpl> {
	}

	Presenter presenter;

	@UiField
	Div container;
	@UiField
	LoadingSpinner loadMoreImage;
	@UiField
	org.gwtbootstrap3.client.ui.Button loadMoreButton;

	Widget widget;

	@Inject
	public LoadMoreWidgetContainerViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		loadMoreButton.addClickHandler(event -> {
			presenter.onLoadMore();
		});
	}

	@Override
	public void setPresenter(Presenter p) {
		this.presenter = p;
	}

	@Override
	public void setIsProcessing(boolean isProcessing) {
		loadMoreImage.setVisible(isProcessing);
		if (isProcessing) {
			loadMoreButton.setVisible(false);
		}
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
		loadMoreButton.setVisible(visible);
	}

	@Override
	public void addStyleName(String styles) {
		container.addStyleName(styles);
	}
}
