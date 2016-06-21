package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
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
	SimplePanel defaultThreadContainer;
	@UiField
	Span subscribeButtonContainer;
	@UiField
	Tooltip newThreadTooltip;
	
	Timer newThreadTooltipHider = new Timer() { 
	    public void run() {
	    	newThreadTooltip.hide();
	    } 
	};
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
	
	@Override
	public void setDefaultThreadWidget(Widget w) {
		defaultThreadContainer.clear();
		defaultThreadContainer.add(w);
	}
	@Override
	public void setDefaultThreadWidgetVisible(boolean visible) {
		defaultThreadContainer.setVisible(visible);
	}
	
	@Override
	public void showNewThreadTooltip() {
		newThreadTooltip.show();
		newThreadTooltipHider.schedule(5000);
	}
}
