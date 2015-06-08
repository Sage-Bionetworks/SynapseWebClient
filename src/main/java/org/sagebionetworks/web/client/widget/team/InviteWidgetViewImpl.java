package org.sagebionetworks.web.client.widget.team;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Strong;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class InviteWidgetViewImpl extends FlowPanel implements InviteWidgetView {
	
	public interface InviteWidgetViewImplUiBinder extends UiBinder<Widget, InviteWidgetViewImpl> {}
	
	@UiField
	Button sendInviteButton;
	
	@UiField
	Modal inviteUIModal;
	
	@UiField
	TextArea inviteTextArea;
	
	@UiField
	SimplePanel suggestBoxPanel;
	
	@UiField
	SimplePanel synAlertPanel;
	
	@UiField
	Button cancelButton;
	
	private static final int FIELD_WIDTH = 500;
	
	private SynapseJSNIUtils synapseJSNIUtils;
	
	private InviteWidgetView.Presenter presenter;
	private TextArea messageArea;
	
	private Widget widget;
	
	@Inject
	public InviteWidgetViewImpl(InviteWidgetViewImplUiBinder binder) {
		this.widget = binder.createAndBindUi(this);
		this.synapseJSNIUtils = synapseJSNIUtils;
		sendInviteButton.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				presenter.sendInvite(inviteTextArea.getValue());
			}
		});
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				clear();
				inviteUIModal.hide();
			}
		});
	}
	
	@Override
	public void setSuggestWidget(Widget suggestWidget) {
		suggestBoxPanel.setWidget(suggestWidget);
	}
	
	@Override
	public void clear() {
		inviteTextArea.setText("");
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setSynAlertWidget(Widget synAlert) {
		synAlertPanel.setWidget(synAlert);
	}
	
	@Override
	public void setVisible(boolean isVisible) {
		if (isVisible) {
			clear();
			inviteUIModal.show();
		} else
			inviteUIModal.hide();
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
}
