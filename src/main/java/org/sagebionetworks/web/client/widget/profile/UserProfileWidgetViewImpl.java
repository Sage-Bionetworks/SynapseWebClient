package org.sagebionetworks.web.client.widget.profile;

import static org.sagebionetworks.web.client.presenter.ProfilePresenter.IS_CERTIFIED;
import static org.sagebionetworks.web.client.presenter.ProfilePresenter.IS_VERIFIED;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.List;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.jsinterop.AccountLevelBadgesProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.UserProfileLinksProps;
import org.sagebionetworks.web.client.jsinterop.mui.Grid;
import org.sagebionetworks.web.client.widget.ReactComponent;
import org.sagebionetworks.web.shared.WebConstants;

public class UserProfileWidgetViewImpl implements UserProfileWidgetView {

  public interface Binder extends UiBinder<Widget, UserProfileWidgetViewImpl> {}

  @UiField
  SimplePanel imagePanel;

  @UiField
  SimplePanel fileInputWidgetPanel;

  @UiField
  FormGroup usernameFormGroup;

  @UiField
  HelpBlock usernameHelpBlock;

  @UiField
  Paragraph usernameRenderer;

  @UiField
  Paragraph firstNameRenderer;

  @UiField
  Paragraph lastNameRenderer;

  @UiField
  Paragraph currentPositionRenderer;

  @UiField
  Paragraph currentAffiliationRenderer;

  @UiField
  Paragraph industryRenderer;

  @UiField
  Div emailDiv;

  @UiField
  Paragraph locationRenderer;

  @UiField
  FormGroup linkFormGroup;

  @UiField
  Anchor linkRenderer;

  @UiField
  HelpBlock linkHelpBlock;

  @UiField
  Paragraph bioRenderer;

  @UiField
  Div synAlertContainer;

  @UiField
  Button editProfileButton;

  @UiField
  Anchor changePasswordLink;

  @UiField
  Grid orcIDContainer;

  @UiField
  Anchor orcIdLink;

  @UiField
  Grid accountTypeContainer;

  @UiField
  ReactComponent accountLevelBadgesContainer;

  @UiField
  Div userProfileLinksUI;

  @UiField
  ReactComponent userProfileLinksReactComponentContainer;

  @UiField
  Grid emailAddressContainer;

  @UiField
  Div commandsContainer;

  private Widget widget;

  SynapseJSNIUtils jsniUtils;
  SynapseReactClientFullContextPropsProvider propsProvider;
  CookieProvider cookies;
  SynapseJavascriptClient jsClient;

  @Inject
  public UserProfileWidgetViewImpl(
    Binder binder,
    SynapseJSNIUtils jsniUtils,
    SynapseReactClientFullContextPropsProvider propsProvider,
    CookieProvider cookies,
    SynapseJavascriptClient jsClient
  ) {
    this.jsniUtils = jsniUtils;
    this.propsProvider = propsProvider;
    this.cookies = cookies;
    this.jsClient = jsClient;
    widget = binder.createAndBindUi(this);
    editProfileButton.addClickHandler(event -> {
      Window.open(WebConstants.ONESAGE_ACCOUNT_SETTINGS_URL, "_blank", "");
    });
    linkRenderer.getElement().setAttribute("rel", "noreferrer noopener");
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setUsername(String username) {
    usernameRenderer.setText(username);
  }

  @Override
  public void clearEmails() {
    emailDiv.clear();
  }

  @Override
  public void setEmailsVisible(boolean visible) {
    emailAddressContainer.setVisible(visible);
  }

  @Override
  public void setEmails(List<String> emails, String notificationEmail) {
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
    firstNameRenderer.setText(firstName);
  }

  @Override
  public void setLastName(String lastName) {
    lastNameRenderer.setText(lastName);
  }

  @Override
  public void setBio(String summary) {
    this.bioRenderer.setText(summary);
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
  public void setCurrentPosition(String position) {
    currentPositionRenderer.setText(position);
  }

  @Override
  public void setCurrentAffiliation(String company) {
    currentAffiliationRenderer.setText(company);
  }

  @Override
  public void setIndustry(String industry) {
    industryRenderer.setText(industry);
  }

  @Override
  public void setLocation(String location) {
    locationRenderer.setText(location);
  }

  @Override
  public void setLink(String url) {
    this.linkRenderer.setHref(url);
    this.linkRenderer.setText(url);
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
  public void setCanEdit(boolean canEdit) {
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
