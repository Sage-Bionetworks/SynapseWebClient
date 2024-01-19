package org.sagebionetworks.web.client.widget.profile;

import static org.sagebionetworks.web.client.presenter.ProfilePresenter.IS_CERTIFIED;
import static org.sagebionetworks.web.client.presenter.ProfilePresenter.IS_VERIFIED;

import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.List;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.base.TextBoxBase;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.jsinterop.AccountLevelBadgesProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.UserProfileLinksProps;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;
import org.sagebionetworks.web.client.widget.search.GooglePlacesSuggestOracle;

// Want to add enter handler... What is the best way to go about this?
public class UserProfileEditorWidgetViewImpl
  implements UserProfileEditorWidgetView {

  public interface Binder
    extends UiBinder<Widget, UserProfileEditorWidgetViewImpl> {}

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
  Div emailDiv;

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
  Paragraph bioRenderer;

  @UiField
  TextArea bioEditor;

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
  Column orcIDContainer;

  @UiField
  Anchor orcIdLink;

  @UiField
  Column accountTypeContainer;

  @UiField
  ReactComponentDiv accountLevelBadgesContainer;

  @UiField
  Div userProfileLinksUI;

  @UiField
  ReactComponentDiv userProfileLinksReactComponentContainer;

  @UiField
  Row ownerFieldsContainer;

  @UiField
  Div commandsContainer;

  SuggestBox locationSuggestBox;
  private Widget widget;

  TextBoxBase[] textBoxes;
  com.google.gwt.user.client.ui.TextBoxBase locationTextBox;
  boolean isEditing = false;
  SynapseJSNIUtils jsniUtils;
  SynapseReactClientFullContextPropsProvider propsProvider;
  Presenter presenter;
  String originalButtonText;
  CookieProvider cookies;
  SynapseJavascriptClient jsClient;

  @Inject
  public UserProfileEditorWidgetViewImpl(
    Binder binder,
    GooglePlacesSuggestOracle locationOracle,
    GlobalApplicationState globalAppState,
    AuthenticationController authController,
    SynapseJSNIUtils jsniUtils,
    SynapseReactClientFullContextPropsProvider propsProvider,
    CookieProvider cookies,
    SynapseJavascriptClient jsClient
  ) {
    widget = binder.createAndBindUi(this);
    this.jsniUtils = jsniUtils;
    this.propsProvider = propsProvider;
    this.cookies = cookies;
    this.jsClient = jsClient;
    locationSuggestBox = new SuggestBox(locationOracle);
    locationSuggestBox.setWidth("100%");
    locationTextBox = locationSuggestBox.getTextBox();
    locationTextBox.addStyleName("form-control");
    locationSuggestBoxContainer.add(locationSuggestBox);
    // note, not adding email since it's not editable here.
    textBoxes =
      new TextBoxBase[] {
        username,
        firstName,
        lastName,
        currentPosition,
        currentAffiliation,
        industry,
        link,
      };
    editProfileButton.addClickHandler(event -> {
      presenter.setIsEditingMode(true);
    });
    saveProfileButton.addClickHandler(event -> {
      DisplayUtils.showLoading(saveProfileButton, true, originalButtonText);
      presenter.onSave();
    });
    cancelButton.addClickHandler(event -> {
      presenter.onCancel();
    });
    linkRenderer.getElement().setAttribute("rel", "noreferrer noopener");
    originalButtonText = saveProfileButton.getText();
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
  public void setEmails(List<String> emails, String notificationEmail) {
    emailDiv.clear();
    if (notificationEmail != null) {
      IsWidget w = getEmailElement(notificationEmail, "strong");
      emailDiv.add(w);
    }
    for (String email : emails) {
      if (!email.equals(notificationEmail)) {
        IsWidget w = getEmailElement(email, null);
        emailDiv.add(w);
      }
    }
  }

  private IsWidget getEmailElement(String email, String paragraphStyles) {
    Tooltip t = new Tooltip(email);
    Paragraph p = new Paragraph(email);
    if (paragraphStyles != null) {
      p.addStyleName(paragraphStyles);
    }
    p.setMarginBottom(0);
    t.add(p);
    return t;
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
    this.bioEditor.setText(summary);
    this.bioRenderer.setText(summary);
  }

  @Override
  public String getBio() {
    return this.bioEditor.getText();
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
    bioEditor.addKeyDownHandler(keyDownHandler);
  }

  @Override
  public void setEditMode(boolean isEditing) {
    this.isEditing = isEditing;
    for (TextBoxBase tb : textBoxes) {
      tb.setReadOnly(!isEditing);
    }

    bioEditor.setVisible(isEditing);
    bioRenderer.setVisible(!isEditing);
    locationTextBox.setReadOnly(!isEditing);
    firstName.setPlaceholder(isEditing ? "Enter first name" : "");
    lastName.setPlaceholder(isEditing ? "Enter last name" : "");
    currentAffiliation.setPlaceholder(
      isEditing ? "Enter current affiliation" : ""
    );
    bioEditor.setPlaceholder("Enter bio");
    link.setPlaceholder(isEditing ? "Enter link to more info" : "");
    locationTextBox
      .getElement()
      .setAttribute("placeholder", isEditing ? "Enter City, Country" : "");
    currentPosition.setPlaceholder(isEditing ? "Enter current position" : "");
    industry.setPlaceholder(isEditing ? "Enter industry/discipline" : "");

    editProfileButton.setVisible(!isEditing);
    saveProfileButton.setVisible(isEditing);
    cancelButton.setVisible(isEditing);
    linkRenderer.setVisible(!isEditing);
    link.setVisible(isEditing);
    changeEmailLink.setVisible(isEditing);
    changePasswordLink.setVisible(isEditing);
    if (!isEditing) {
      DisplayUtils.showLoading(saveProfileButton, false, originalButtonText);
    }
  }

  @Override
  public void setSynAlert(IsWidget w) {
    synAlertContainer.clear();
    synAlertContainer.add(w);
  }

  @Override
  public void setOwnerId(String userId) {
    ReactNode accountLevelBadgesComponent =
      React.createElementWithSynapseContext(
        SRC.SynapseComponents.AccountLevelBadges,
        AccountLevelBadgesProps.create(userId),
        propsProvider.getJsInteropContextProps()
      );
    accountLevelBadgesContainer.render(accountLevelBadgesComponent);
    setAccountTypeVisibility(Long.parseLong(userId));

    UserProfileLinksProps props = UserProfileLinksProps.create(userId);
    ReactNode profileLinksComponent = React.createElementWithSynapseContext(
      SRC.SynapseComponents.UserProfileLinks,
      props,
      propsProvider.getJsInteropContextProps()
    );
    userProfileLinksReactComponentContainer.render(profileLinksComponent);
  }

  public void setAccountTypeVisibility(Long userId) {
    int mask = IS_CERTIFIED | IS_VERIFIED;
    jsClient.getUserBundle(
      userId,
      mask,
      new AsyncCallback<UserBundle>() {
        @Override
        public void onSuccess(UserBundle bundle) {
          boolean showAccountType =
            bundle.getIsCertified() | bundle.getIsVerified();
          accountTypeContainer.setVisible(showAccountType);
        }

        // error will be handled/shown by React component
        @Override
        public void onFailure(Throwable caught) {}
      }
    );
  }

  @Override
  public void resetSaveButtonState() {
    DisplayUtils.showLoading(saveProfileButton, false, originalButtonText);
  }

  @Override
  public void setCanEdit(boolean canEdit) {
    ownerFieldsContainer.setVisible(canEdit);
    commandsContainer.setVisible(canEdit);
  }

  @Override
  public void setOrcIdHref(String orcIdHref) {
    boolean isDefined = DisplayUtils.isDefined(orcIdHref);
    orcIDContainer.setVisible(isDefined);
    if (isDefined) {
      orcIdLink.setHref(orcIdHref);
      orcIdLink.setText(orcIdHref);
    }
  }
}
