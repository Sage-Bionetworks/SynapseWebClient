package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.BlockQuote;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
import org.sagebionetworks.web.client.widget.header.Header;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
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
	Anchor loginLink;
	@UiField
	Button registerButton;

	private Presenter presenter;
	private Header headerWidget;
	SynapseJSNIUtils jsniUtils;

	@Inject
	public EmailInvitationViewImpl(EmailInvitationViewImplUiBinder binder, Header headerWidget, SynapseJSNIUtils jsniUtils) {
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		this.jsniUtils = jsniUtils;
		loginLink.addClickHandler(event -> presenter.onLoginClick());
		registerButton.addClickHandler(event -> presenter.onRegisterClick());
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
		invitationMessage.clear();
		invitationMessageWrapper.setVisible(true);
		invitationMessage.add(new HTML(jsniUtils.sanitizeHtml(message)));
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
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
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
