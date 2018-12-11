package org.sagebionetworks.web.client.widget.profile;

import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.widget.search.GooglePlacesSuggestOracle;

import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

// Want to add enter handler... What is the best way to go about this?
public class UserProfileEditorWidgetViewImpl implements
		UserProfileEditorWidgetView {
	
	public interface Binder extends UiBinder<Widget, UserProfileEditorWidgetViewImpl> {}
	
	@UiField
	SimplePanel imagePanel;
	@UiField
	SimplePanel fileInputWidgetPanel;
	@UiField
	FormGroup usernameFormGroup;
	@UiField
	HelpBlock usernameHelpBlock;	
	@UiField
	TextBox username;
	@UiField
	TextBox firstName;
	@UiField
	TextBox lastName;
	@UiField
	TextBox currentPosition;
	@UiField
	TextBox currentAffiliation;
	@UiField
	TextBox industry;
	@UiField
	Div locationSuggestBoxContainer;
	@UiField
	FormGroup linkFormGroup;
	@UiField
	TextBox link;
	@UiField
	HelpBlock linkHelpBlock;
	@UiField
	TextArea bio;
	SuggestBox locationSuggestBox;
	private Widget widget;
	
	@Inject
	public UserProfileEditorWidgetViewImpl(Binder binder, 
			GooglePlacesSuggestOracle locationOracle){
		widget = binder.createAndBindUi(this);
		locationSuggestBox = new SuggestBox(locationOracle);
		locationSuggestBox.setWidth("100%");
		locationSuggestBox.getTextBox().addStyleName("form-control");
		locationSuggestBox.getTextBox().getElement().setAttribute("placeholder", "Enter City, Country");
		locationSuggestBoxContainer.add(locationSuggestBox);
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

	@Override
	public void showUsernameError(String error) {
		usernameFormGroup.setValidationState(ValidationState.ERROR);
		usernameHelpBlock.setText(error);
	}

	@Override
	public void hideUsernameError() {
		usernameFormGroup.setValidationState(ValidationState.NONE);
		usernameHelpBlock.setText("");
	}

	@Override
	public void addImageWidget(IsWidget image) {
		imagePanel.add(image);
	}

	@Override
	public void addFileInputWidget(IsWidget fileInputWidget) {
		fileInputWidgetPanel.add(fileInputWidget);
	}

	@Override
	public void setPresenter(final Presenter presenter) {

	}

	@Override
	public String getLink() {
		return link.getText();
	}

	@Override
	public void showLinkError(String string) {
		linkFormGroup.setValidationState(ValidationState.ERROR);
		linkHelpBlock.setText(string);
	}

	@Override
	public void hideLinkError() {
		linkFormGroup.setValidationState(ValidationState.NONE);
		linkHelpBlock.setText("");
	}

	@Override
	public String getCurrentPosition() {
		return currentPosition.getText();
	}

	@Override
	public void setCurrentPosition(String position) {
		currentPosition.setText(position);
	}

	@Override
	public void setCurrentAffiliation(String company) {
		currentAffiliation.setText(company);
	}

	@Override
	public String getCurrentAffiliation() {
		return currentAffiliation.getText();
	}

	@Override
	public void setIndustry(String industry) {
		this.industry.setText(industry);
	}

	@Override
	public String getIndustry() {
		return this.industry.getText();
	}

	@Override
	public void setLocation(String location) {
		this.locationSuggestBox.setText(location);
	}

	@Override
	public String getLocation() {
		return this.locationSuggestBox.getText();
	}

	@Override
	public void setLink(String url) {
		this.link.setText(url);
	}

	@Override
	public void addKeyDownHandlerToFields(KeyDownHandler keyDownHandler) {
		username.addKeyDownHandler(keyDownHandler);
		firstName.addKeyDownHandler(keyDownHandler);
		lastName.addKeyDownHandler(keyDownHandler);
		currentPosition.addKeyDownHandler(keyDownHandler);
		currentAffiliation.addKeyDownHandler(keyDownHandler);
		industry.addKeyDownHandler(keyDownHandler);
		locationSuggestBox.addKeyDownHandler(keyDownHandler);
		link.addKeyDownHandler(keyDownHandler);
		bio.addKeyDownHandler(keyDownHandler);
	}

}
