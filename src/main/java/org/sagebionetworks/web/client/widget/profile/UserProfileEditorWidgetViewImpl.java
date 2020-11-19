package org.sagebionetworks.web.client.widget.profile;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.base.TextBoxBase;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.search.GooglePlacesSuggestOracle;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

// Want to add enter handler... What is the best way to go about this?
public class UserProfileEditorWidgetViewImpl implements UserProfileEditorWidgetView {

	public interface Binder extends UiBinder<Widget, UserProfileEditorWidgetViewImpl> {
	}

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
	TextBox email;
	@UiField
	Div locationSuggestBoxContainer;
	@UiField
	FormGroup linkFormGroup;
	@UiField
	TextBox link;
	@UiField
	Anchor linkRenderer;
	@UiField
	HelpBlock linkHelpBlock;
	@UiField
	TextArea bio;
	@UiField
	Div synAlertContainer;
	@UiField
	Button editProfileButton;
	@UiField
	Button saveProfileButton;
	@UiField
	Button cancelButton;
	@UiField
	Anchor changeEmailLink;
	@UiField
	Anchor changePasswordLink;
	@UiField
	Span accountLevelBadgeContainer;
	
	SuggestBox locationSuggestBox;
	private Widget widget;

	TextBoxBase[] textBoxes;
	com.google.gwt.user.client.ui.TextBoxBase locationTextBox;
	boolean isEditing = false;
	SynapseJSNIUtils jsniUtils;
	Presenter presenter;
	@Inject
	public UserProfileEditorWidgetViewImpl(Binder binder, GooglePlacesSuggestOracle locationOracle, GlobalApplicationState globalAppState, AuthenticationController authController, SynapseJSNIUtils jsniUtils) {
		widget = binder.createAndBindUi(this);
		this.jsniUtils = jsniUtils;
		locationSuggestBox = new SuggestBox(locationOracle);
		locationSuggestBox.setWidth("100%");
		locationTextBox = locationSuggestBox.getTextBox();
		locationTextBox.addStyleName("form-control");
		locationTextBox.getElement().setAttribute("placeholder", "Enter City, Country");
		locationSuggestBoxContainer.add(locationSuggestBox);
		// note, not adding email since it's not editable here.
		textBoxes = new TextBoxBase[] {username, firstName, lastName, currentPosition, currentAffiliation, industry, link, bio} ;
		editProfileButton.addClickHandler(event -> {
			presenter.setIsEditingMode(true);			
		});
		saveProfileButton.addClickHandler(event -> {
			saveProfileButton.state().loading();
			presenter.onSave();
		});
		cancelButton.addClickHandler(event -> {
			presenter.onCancel();
		});
		linkRenderer.getElement().setAttribute("rel", "noreferrer noopener");
		ClickHandler goToSettingsPage = event -> {
			globalAppState.getPlaceChanger().goTo(new Profile(authController.getCurrentUserPrincipalId(), ProfileArea.SETTINGS));
		};
		changeEmailLink.addClickHandler(goToSettingsPage);
		changePasswordLink.addClickHandler(goToSettingsPage);
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
	public void setEmail(String email) {
		this.email.setText(email);
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
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
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
		this.linkRenderer.setHref(url);
		this.linkRenderer.setText(url);
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

	@Override
	public void setEditMode(boolean isEditing) {
		this.isEditing = isEditing;
		for (TextBoxBase tb : textBoxes) {
			tb.setReadOnly(!isEditing);
		}
		locationTextBox.setReadOnly(!isEditing);
		editProfileButton.setVisible(!isEditing);
		saveProfileButton.setVisible(isEditing);
		cancelButton.setVisible(isEditing);
		linkRenderer.setVisible(!isEditing);
		link.setVisible(isEditing);
		changeEmailLink.setVisible(isEditing);
		changePasswordLink.setVisible(isEditing);
		if (!isEditing) {
			saveProfileButton.state().reset();	
		}		
	}

	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
	@Override
	public void setOwnerId(String userId) {
		jsniUtils.unmountComponentAtNode(accountLevelBadgeContainer.getElement());
		_showAccountLevelBadge(accountLevelBadgeContainer.getElement(), userId);
	}
	
	private static native void _showAccountLevelBadge(Element el, String userId) /*-{
		try {
			var props = {
			  	userId: userId,
			};
			$wnd.ReactDOM.render($wnd.React.createElement(
					$wnd.SRC.SynapseComponents.AccountLevelBadge, props, null),
					el);
		} catch (err) {
			console.error(err);
		}
	}-*/;

	@Override
	public void resetSaveButtonState() {
		saveProfileButton.state().reset();
	}
}
