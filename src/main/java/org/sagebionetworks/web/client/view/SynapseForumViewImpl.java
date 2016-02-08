package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.extras.toggleswitch.client.ui.ToggleSwitch;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SynapseForumViewImpl implements SynapseForumView {

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
	@UiField
	Div moderatorModeContainer;
	@UiField
	Div wikiContainer;
	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	private Presenter presenter;

	Widget widget;
	public interface SynapseForumViewImplUiBinder extends UiBinder<Widget, SynapseForumViewImpl> {}

	@Inject
	public SynapseForumViewImpl(
			SynapseForumViewImplUiBinder binder, 
			Header headerWidget, 
			Footer footerWidget) {
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
		
		headerWidget.configure(false);
		header.setWidget(headerWidget.asWidget());
		footer.setWidget(footerWidget.asWidget());
		headerWidget.refresh();
		Window.scrollTo(0, 0); // scroll user to top of page
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
	public void setWikiWidget(Widget w) {
		wikiContainer.clear();
		wikiContainer.add(w);
	}
}
