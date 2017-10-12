package org.sagebionetworks.web.client.view;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.html.Div;

public class EmailInvitationViewImpl extends Composite implements EmailInvitationView {

	@UiField
	SimplePanel synapseAlertContainer;
	@UiField
	Div registerWidgetContainer;

	@Inject
	public EmailInvitationViewImpl(EmailInvitationViewImplUiBinder binder) {
		initWidget(binder.createAndBindUi(this));
	}

	@Override
	public void setRegisterWidget(Widget w) {
		registerWidgetContainer.clear();
		registerWidgetContainer.add(w);
	}

	@Override
	public void setSynapseAlertContainer(Widget w) {
		synapseAlertContainer.setWidget(w);
	}

	public interface EmailInvitationViewImplUiBinder extends UiBinder<Widget, EmailInvitationViewImpl> {
	}
}
