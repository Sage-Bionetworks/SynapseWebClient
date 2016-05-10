package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ForumWidgetViewImpl implements ForumWidgetView {

	@UiField
	Button newThreadButton;
	@UiField
	SimplePanel threadListContainer;
	@UiField
	SimplePanel newThreadModalContainer;
	@UiField
	Div synAlertContainer;
	@UiField
	Button showAllThreadsButton;
	@UiField
	SimplePanel singleThreadContainer;
	@UiField
	Span emptyUI;
	@UiField
	Table threadHeader;
	@UiField
	FocusPanel sortByReplies;
	@UiField
	FocusPanel sortByViews;
	@UiField
	FocusPanel sortByActivity;
	@UiField
	Span subscribeButtonContainer;
	
	private Presenter presenter;

	Widget widget;
	public interface Binder extends UiBinder<Widget, ForumWidgetViewImpl> {}

	@Inject
	public ForumWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		newThreadButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onClickNewThread();
			}
		});
		showAllThreadsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onClickShowAllThreads();
			}
		});
		sortByReplies.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.sortByReplies();
			}
		});
		sortByViews.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.sortByViews();
			}
		});
		sortByActivity.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.sortByActivity();
			}
		});
	}
	
	@Override
	public void setSingleThread(Widget w) {
		singleThreadContainer.setWidget(w);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setThreadList(Widget w) {
		threadListContainer.setWidget(w);
	}

	@Override
	public void setNewThreadModal(Widget w) {
		newThreadModalContainer.setWidget(w);
	}

	@Override
	public void setAlert(Widget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}

	@Override
	public void showErrorMessage(String errorMessage) {
		DisplayUtils.showErrorMessage(errorMessage);
	}
	
	@Override
	public void setSingleThreadUIVisible(boolean visible) {
		singleThreadContainer.setVisible(visible);
	}

	@Override
	public void setThreadListUIVisible(boolean visible) {
		threadListContainer.setVisible(visible);
	}

	@Override
	public void setEmptyUIVisible(boolean visible) {
		emptyUI.setVisible(visible);
	}

	@Override
	public void setThreadHeaderVisible(boolean visible) {
		threadHeader.setVisible(visible);
	}

	@Override
	public void setNewThreadButtonVisible(boolean visible) {
		newThreadButton.setVisible(visible);
	}

	@Override
	public void setShowAllThreadsButtonVisible(boolean visible) {
		showAllThreadsButton.setVisible(visible);
	}
	@Override
	public void setSubscribeButton(Widget w) {
		subscribeButtonContainer.clear();
		subscribeButtonContainer.add(w);
	}
}
