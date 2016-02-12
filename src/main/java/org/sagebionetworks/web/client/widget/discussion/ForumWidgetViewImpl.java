package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.extras.toggleswitch.client.ui.ToggleSwitch;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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
	ToggleSwitch moderatorModeSwitch;
	@UiField
	Div moderatorModeContainer;
	@UiField
	Button showAllThreadsButton;
	@UiField
	SimplePanel singleThreadContainer;
	@UiField
	Span emptyUI;
	@UiField
	Table threadHeader;

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
		moderatorModeSwitch.addValueChangeHandler(new ValueChangeHandler<Boolean>(){
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				presenter.onModeratorModeChange();
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
	public void setModeratorModeContainerVisibility(Boolean visible) {
		moderatorModeContainer.setVisible(visible);
	}

	@Override
	public Boolean getModeratorMode() {
		return moderatorModeSwitch.getValue();
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
}
