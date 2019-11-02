package org.sagebionetworks.web.client.widget.team;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Italic;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenUserInvitationsWidgetViewImpl implements OpenUserInvitationsWidgetView {
	public static final String MEMBERSHIP_INVITATION_ID = "data-membership-invitation-id";

	public interface Binder extends UiBinder<Widget, OpenUserInvitationsWidgetViewImpl> {
	}

	@UiField
	Div synAlertContainer;
	@UiField
	Div invitationsContainer;
	@UiField
	Heading title;
	@UiField
	Table invitations;
	@UiField
	Button moreButton;

	private Widget widget;
	PortalGinInjector ginInjector;
	private Presenter presenter;
	SynapseJSNIUtils jsniUtils;
	private ClickHandler removeInvitationClickHandler = event -> {
		event.preventDefault();
		Button btn = (Button) event.getSource();
		String membershipInvitationId = btn.getElement().getAttribute(MEMBERSHIP_INVITATION_ID);
		btn.setEnabled(false);
		presenter.removeInvitation(membershipInvitationId);
	};

	private ClickHandler resendInvitationClickHandler = event -> {
		event.preventDefault();
		Button btn = (Button) event.getSource();
		String membershipInvitationId = btn.getElement().getAttribute(MEMBERSHIP_INVITATION_ID);
		btn.setEnabled(false);
		presenter.resendInvitation(membershipInvitationId);
	};

	@Inject
	public OpenUserInvitationsWidgetViewImpl(Binder binder, PortalGinInjector ginInjector) {
		widget = binder.createAndBindUi(this);
		this.ginInjector = ginInjector;
		jsniUtils = ginInjector.getSynapseJSNIUtils();
		moreButton.addClickHandler(event -> presenter.getNextBatch());
	}

	@Override
	public void setVisible(boolean visible) {
		widget.setVisible(visible);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void addInvitation(IsWidget badge, String inviteeEmail, String misId, String message, String createdOn) {
		OpenUserInvitationWidget openUserInvitationWidget = ginInjector.getOpenUserInvitationWidget();

		boolean isInviteeEmail = inviteeEmail != null;
		openUserInvitationWidget.badgeTableData.setVisible(!isInviteeEmail);
		openUserInvitationWidget.inviteeEmailTableData.setVisible(isInviteeEmail);
		if (isInviteeEmail) {
			openUserInvitationWidget.inviteeEmailTableData.add(new Text(inviteeEmail));
		} else {
			openUserInvitationWidget.badgeTableData.add(badge);
		}
		if (message != null) {
			openUserInvitationWidget.messageTableData.add(new HTML(jsniUtils.sanitizeHtml(message)));
		}
		openUserInvitationWidget.createdOnTableData.add(new Italic(createdOn));

		openUserInvitationWidget.cancelButton.getElement().setAttribute(MEMBERSHIP_INVITATION_ID, misId);
		openUserInvitationWidget.cancelButton.addClickHandler(removeInvitationClickHandler);

		openUserInvitationWidget.resendButton.getElement().setAttribute(MEMBERSHIP_INVITATION_ID, misId);
		openUserInvitationWidget.resendButton.addClickHandler(resendInvitationClickHandler);

		invitations.add(openUserInvitationWidget);
		title.setVisible(true);
	}

	@Override
	public void clear() {
		title.setVisible(false);
		invitations.clear();
	}

	@Override
	public void hideMoreButton() {
		moreButton.setVisible(false);
	}

	@Override
	public void showMoreButton() {
		moreButton.setVisible(true);
	}

	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
}
