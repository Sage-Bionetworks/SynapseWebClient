package org.sagebionetworks.web.client.widget.entity.tabs;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.extras.toggleswitch.client.ui.ToggleSwitch;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class DiscussionTabViewImpl implements DiscussionTabView {

	@UiField
	Button newThreadButton;
	@UiField
	SimplePanel discussionContainer;
	@UiField
	SimplePanel newThreadModalContainer;
	@UiField
	Div synAlertContainer;
	@UiField
	ToggleSwitch moderatorModeSwitch;

	private Presenter presenter;

	Widget widget;
	public interface TabsViewImplUiBinder extends UiBinder<Widget, DiscussionTabViewImpl> {}

	public DiscussionTabViewImpl() {
		TabsViewImplUiBinder binder = GWT.create(TabsViewImplUiBinder.class);
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
		discussionContainer.setWidget(w);
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
	public void setModeratorModeSwitchVisibility(Boolean visible) {
		moderatorModeSwitch.setVisible(visible);
	}

	@Override
	public Boolean getModeratorMode() {
		return moderatorModeSwitch.getValue();
	}
}
