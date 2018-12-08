package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
import org.sagebionetworks.web.client.widget.header.Header;

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
	SimplePanel synapseAlertContainer;
	private Header headerWidget;

	@Inject
	public EmailInvitationViewImpl(EmailInvitationViewImplUiBinder binder,
								   Header headerWidget) {
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
	}

	@Override
	public void setSynapseAlertContainer(Widget w) {
		synapseAlertContainer.setWidget(w);
	}

	@Override
	public void refreshHeader() {
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

	public interface EmailInvitationViewImplUiBinder extends UiBinder<Widget, EmailInvitationViewImpl> {
	}
}
