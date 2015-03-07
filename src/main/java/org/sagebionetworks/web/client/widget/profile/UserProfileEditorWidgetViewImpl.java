package org.sagebionetworks.web.client.widget.profile;

import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserProfileEditorWidgetViewImpl implements
		UserProfileEditorWidgetView {
	
	public interface Binder extends UiBinder<Widget, UserProfileEditorWidgetViewImpl> {}
	
	@UiField
	TextBox username;
	@UiField
	TextBox firstName;
	@UiField
	TextBox lastName;
	@UiField
	TextArea bio;
	
	private Widget widget;
	
	@Inject
	public UserProfileEditorWidgetViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setUsername(String username) {
		this.username.setText(username);
	}

	@Override
	public void setFirstName(String firstName) {
		this.firstName.setText(firstName);
	}

	@Override
	public String getFirstName() {
		return this.firstName.getText();
	}

	@Override
	public String getLastName() {
		return this.lastName.getText();
	}

	@Override
	public String getUsername() {
		return username.getText();
	}

	@Override
	public void setLastName(String lastName) {
		this.lastName.setText(lastName);
	}

	@Override
	public void setBio(String summary) {
		this.bio.setText(summary);
	}

	@Override
	public String getBio() {
		return this.bio.getText();
	}

}
