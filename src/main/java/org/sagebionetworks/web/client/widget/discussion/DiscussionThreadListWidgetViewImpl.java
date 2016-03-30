package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DiscussionThreadListWidgetViewImpl implements DiscussionThreadListWidgetView{

	public interface Binder extends UiBinder<Widget, DiscussionThreadListWidgetViewImpl> {}

	@UiField
	Column threadListContainer;
	@UiField
	Div synAlertContainer;
	@UiField
	Div refreshAlertContainer;
	@UiField
	Button loadMoreButton;
	@UiField
	HTMLPanel loadingUI;

	Widget widget;
	private DiscussionThreadListWidget presenter;

	@Inject
	public DiscussionThreadListWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		loadMoreButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.loadMore();
			}
		});
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(DiscussionThreadListWidget presenter) {
		this.presenter = presenter;
	}

	@Override
	public void addThread(Widget w) {
		threadListContainer.add(w);
	}

	@Override
	public void clear() {
		threadListContainer.clear();
	}

	@Override
	public void setAlert(Widget w) {
		synAlertContainer.add(w);
	}

	@Override
	public void setLoadMoreButtonVisibility(boolean visible) {
		loadMoreButton.setVisible(visible);
	}

	@Override
	public void setLoadingVisible(boolean visible) {
		loadingUI.setVisible(false);
	}
	
	public void setRefreshAlert(Widget w) {
		refreshAlertContainer.clear();
		refreshAlertContainer.add(w);
	};
}
