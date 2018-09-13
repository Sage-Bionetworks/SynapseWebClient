package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.BlockQuote;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EmailInvitationViewImpl extends Composite implements EmailInvitationView {
	@UiField
	LoadingSpinner loading;
	@UiField
	Div notLoggedInContainer;
	@UiField
	SimplePanel synapseAlertContainer;
	@UiField
	Heading invitationTitle;
	@UiField
	BlockQuote invitationMessageWrapper;
	@UiField
	Heading invitationMessage;
	@UiField
	Div registerWidgetContainer;
	@UiField
	Button loginButton;

	private Presenter presenter;
	private Header headerWidget;

	@Inject
	public EmailInvitationViewImpl(EmailInvitationViewImplUiBinder binder,
								   Header headerWidget) {
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		loginButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				presenter.onLoginClick();
			}
		});
	}

	@Override
	public void setSynapseAlertContainer(Widget w) {
		synapseAlertContainer.setWidget(w);
	}

	@Override
	public void setInvitationTitle(String title) {
		invitationTitle.setText(title);
	}

	@Override
	public void setInvitationMessage(String message) {
		invitationMessageWrapper.setVisible(true);
		invitationMessage.setText(message);
	}

	@Override
	public void setRegisterWidget(Widget w) {
		registerWidgetContainer.clear();
		registerWidgetContainer.add(w);
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		headerWidget.configure();
		headerWidget.refresh();
		Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public void showLoading() {
		loading.setVisible(true);
	}

	@Override
	public void hideLoading() {
		loading.setVisible(false);
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void clear() {
		notLoggedInContainer.setVisible(false);
		invitationMessageWrapper.setVisible(false);
	}

	@Override
	public void showNotLoggedInUI() {
		notLoggedInContainer.setVisible(true);
	}

	public interface EmailInvitationViewImplUiBinder extends UiBinder<Widget, EmailInvitationViewImpl> {
	}
}
