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
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenUserInvitationsWidgetViewImpl implements OpenUserInvitationsWidgetView {
	public interface Binder extends UiBinder<Widget, OpenUserInvitationsWidgetViewImpl> {}
	@UiField Div synAlertContainer;
	@UiField Div invitationsContainer;
	@UiField Table invitations;
	@UiField Button moreButton;

	private Widget widget;
	private Presenter presenter;

	@Inject
	public OpenUserInvitationsWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		invitationsContainer.getElement().setAttribute("highlight-box-title", DisplayConstants.PENDING_INVITATIONS);
		moreButton.addClickHandler(event -> presenter.getNextBatch());
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void addInvitation(UserBadge userBadge, String inviteeEmail, String misId, String message, String createdOn) {
		TableData invitationData = new TableData();
		invitationData.addStyleName("padding-5");
		invitationData.add(userBadge);

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

		TableData removeBottomContainer = new TableData();
		removeBottomContainer.add(createRemoveButton(misId));

		TableRow invitation = new TableRow();
		invitation.add(invitationData);
		invitation.add(removeBottomContainer);
		invitations.add(invitation);
	}

	@Override
	public void addInvitation(EmailInvitationBadge badge, String misId, String message, String createdOn) {
		TableData invitationData = new TableData();
		invitationData.addStyleName("padding-5");
		invitationData.add(badge);

		if (message != null) {
			Div messageDiv = new Div();
			messageDiv.add(new Text(message));
			invitationData.add(messageDiv);
		}

		Div createdOnDiv = new Div();
		createdOnDiv.add(new Italic(createdOn));
		invitationData.add(createdOnDiv);

		TableData removeButton = new TableData();
		removeButton.add(createRemoveButton(misId));

		TableRow invitation = new TableRow();
		invitation.add(invitationData);
		invitation.add(removeButton);
		invitations.add(invitation);
	}

	private Button createRemoveButton(String misId) {
		Button button = new Button("Remove");
		button.setType(ButtonType.DANGER);
		button.setSize(ButtonSize.EXTRA_SMALL);
		button.setPull(Pull.RIGHT);
		button.addClickHandler(event -> presenter.removeInvitation(misId));
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
