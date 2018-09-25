package org.sagebionetworks.web.client.widget.team;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Italic;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenUserInvitationsWidgetViewImpl implements OpenUserInvitationsWidgetView {
	public static final String MEMBERSHIP_INVITATION_ID = "data-membership-invitation-id";
	public interface Binder extends UiBinder<Widget, OpenUserInvitationsWidgetViewImpl> {}
	@UiField Div synAlertContainer;
	@UiField Div invitationsContainer;
	@UiField Table invitations;
	@UiField Button moreButton;
	
	private Widget widget;
	private Presenter presenter;
	private ClickHandler removeInvitationClickHandler = event -> {
		event.preventDefault();
		Button btn = (Button)event.getSource();
		String membershipInvitationId = btn.getElement().getAttribute(MEMBERSHIP_INVITATION_ID);
		btn.setEnabled(false);
		presenter.removeInvitation(membershipInvitationId);
	};
	
	private ClickHandler resendInvitationClickHandler = event -> {
		event.preventDefault();
		Button btn = (Button)event.getSource();
		String membershipInvitationId = btn.getElement().getAttribute(MEMBERSHIP_INVITATION_ID);
		btn.setEnabled(false);
		presenter.resendInvitation(membershipInvitationId);
	};
	
	@Inject
	public OpenUserInvitationsWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		invitationsContainer.getElement().setAttribute("highlight-box-title", DisplayConstants.PENDING_INVITATIONS);
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
		TableData invitationData = new TableData();
		invitationData.addStyleName("padding-5");
		invitationData.add(badge);

		if (inviteeEmail != null) {
			Div inviteeEmailDiv = new Div();
			inviteeEmailDiv.add(new Text(inviteeEmail));
			invitationData.add(inviteeEmailDiv);
		}

		if (message != null) {
			Div messageDiv = new Div();
			messageDiv.add(new Text(message));
			invitationData.add(messageDiv);
		}

		Div createdOnDiv = new Div();
		createdOnDiv.add(new Italic(createdOn));
		invitationData.add(createdOnDiv);

		TableData removeButtonContainer = new TableData();
		removeButtonContainer.add(createRemoveButton(misId));
		removeButtonContainer.add(createResendButton(misId));
		
		TableRow invitation = new TableRow();
		invitation.add(invitationData);
		invitation.add(removeButtonContainer);
		invitations.add(invitation);
	}

	private Button createRemoveButton(String misId) {
		Button button = new Button("Remove");
		button.setType(ButtonType.DANGER);
		button.setSize(ButtonSize.LARGE);
		button.setPull(Pull.RIGHT);
		button.getElement().setAttribute(MEMBERSHIP_INVITATION_ID, misId);
		button.addClickHandler(removeInvitationClickHandler);
		return button;
	}
	
	private Button createResendButton(String misId) {
		Button button = new Button("Resend");
		button.setSize(ButtonSize.LARGE);
		button.setPull(Pull.RIGHT);
		button.setMarginRight(10);
		button.getElement().setAttribute(MEMBERSHIP_INVITATION_ID, misId);
		button.addClickHandler(resendInvitationClickHandler);
		return button;
	}

	@Override
	public void clear() {
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
