package org.sagebionetworks.web.client.widget.team;

import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Strong;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class EmailInvitationBadgeViewImpl implements EmailInvitationBadgeView {
	public interface Binder extends UiBinder<Widget, EmailInvitationBadgeViewImpl> {
	}

	@UiField
	Icon squareIcon;
	@UiField
	Strong iconLetter;
	@UiField
	Span inviteeEmail;

	private Widget widget;

	@Inject
	public EmailInvitationBadgeViewImpl(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
	}

	@Override
	public void setEmail(String inviteeEmail) {
		this.inviteeEmail.setText(inviteeEmail);
	}

	@Override
	public void setIconColor(String color) {
		squareIcon.setColor(color);
		iconLetter.setColor(color);
	}

	@Override
	public void setIconLetter(String letter) {
		iconLetter.setText(letter);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
}
