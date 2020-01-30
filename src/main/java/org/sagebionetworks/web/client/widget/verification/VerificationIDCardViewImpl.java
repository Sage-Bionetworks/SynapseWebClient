package org.sagebionetworks.web.client.widget.verification;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Span;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class VerificationIDCardViewImpl implements IsWidget {

	public interface Binder extends UiBinder<Widget, VerificationIDCardViewImpl> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	Widget widget;

	@UiField
	Heading firstName;
	@UiField
	Heading lastName;
	@UiField
	Span currentAffiliation;
	@UiField
	Span location;
	@UiField
	Anchor orcIdAnchor;
	@UiField
	Modal modal;
	@UiField
	Span dateVerified;

	public VerificationIDCardViewImpl() {
		widget = uiBinder.createAndBindUi(this);
	}


	public void clear() {
		firstName.setText("");
		lastName.setText("");
		currentAffiliation.setText("");
		location.setText("");
		orcIdAnchor.setText("");
	}

	public void setFirstName(String fname) {
		firstName.setText(fname);
	}

	public void setLastName(String lname) {
		lastName.setText(lname);
	}

	public void setOrganization(String organization) {
		currentAffiliation.setText(organization);
	}

	public void setLocation(String l) {
		location.setText(l);
	}

	public void setOrcID(String href) {
		orcIdAnchor.setText(href);
		orcIdAnchor.setHref(href);
	}

	public void setDateVerified(String date) {
		dateVerified.setText(date);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	public void show() {
		modal.show();
	}

	public void hide() {
		modal.hide();
	}
}
