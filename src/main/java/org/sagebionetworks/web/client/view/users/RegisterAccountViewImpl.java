package org.sagebionetworks.web.client.view.users;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTTimer;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.ValidationUtils;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.pageprogress.PageProgressWidget;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.shared.WebConstants;

public class RegisterAccountViewImpl
  extends Composite
  implements RegisterAccountView {

  public interface RegisterAccountViewImplUiBinder
    extends UiBinder<Widget, RegisterAccountViewImpl> {}

  @UiField
  Div registerWidgetContainer;

  @UiField
  TextBox userNameField;

  @UiField
  Button googleSignUpButton;

  @UiField
  Div googleSynAlertContainer;

  @UiField
  Div googleSynapseAccountCreationUI;

  @UiField
  Div createSynapseAccountUI;

  @UiField
  Div emailSentUI;

  @UiField
  Div pageProgressContainer;

  SynapseAlert synAlert;
  GWTTimer timer;
  Presenter presenter;
  private Header headerWidget;
  public static final String ROOT_PORTAL_URL =
    Window.Location.getProtocol() + "//" + Window.Location.getHost() + "/";
  public static final String OAUTH_CALLBACK_URL =
    RegisterAccountViewImpl.ROOT_PORTAL_URL +
    "Portal/oauth2callback?oauth2provider=";
  public static final String GOOGLE_OAUTH_WITH_STATE_CALLBACK_URL =
    OAUTH_CALLBACK_URL + "GOOGLE_OAUTH_2_0&state=";

  @Inject
  public RegisterAccountViewImpl(
    RegisterAccountViewImplUiBinder binder,
    GlobalApplicationState globalAppState,
    Header headerWidget,
    GWTTimer timer,
    SageImageBundle sageImageBundle,
    PageProgressWidget pageProgressWidget
  ) {
    initWidget(binder.createAndBindUi(this));
    this.timer = timer;
    this.headerWidget = headerWidget;
    timer.configure(() -> {
      setGoogleRegisterButtonEnabled(false);
      if (checkUsernameFormat()) presenter.checkUsernameAvailable(
        userNameField.getValue()
      );
    });
    Image googleLogo = new Image(sageImageBundle.logoGoogle());
    googleLogo.addStyleName("whiteBackground left padding-10 rounded");
    googleLogo.setHeight("42px");
    googleLogo.setWidth("42px");
    googleSignUpButton.add(googleLogo);
    Span googleText = new Span("Sign up with Google");
    googleText.addStyleName("movedown-9");
    googleSignUpButton.add(googleText);
    KeyDownHandler register = event -> {
      if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
        googleSignUpButton.click();
      } else {
        timer.cancel();
        timer.schedule(SynapseSuggestBox.DELAY);
      }
    };
    userNameField.addKeyDownHandler(register);
    googleSignUpButton.addClickHandler(event -> {
      if (checkUsernameFormat()) {
        String encodedUsername = URL.encodeQueryString(
          userNameField.getValue()
        );
        Window.Location.assign(
          RegisterAccountViewImpl.GOOGLE_OAUTH_WITH_STATE_CALLBACK_URL +
          encodedUsername
        );
      }
    });

    pageProgressContainer.add(pageProgressWidget);
    Callback backBtnCallback = () -> {
      showEmailSentUI(false);
    };
    Callback forwardBtnCallback = () -> {
      globalAppState
        .getPlaceChanger()
        .goTo(new Home(ClientProperties.DEFAULT_PLACE_TOKEN));
    };
    pageProgressWidget.configure(
      WebConstants.SYNAPSE_GREEN,
      20,
      "Back",
      backBtnCallback,
      "Go to Home",
      forwardBtnCallback,
      true
    );
  }

  @Override
  public void showEmailSentUI(boolean visible) {
    emailSentUI.setVisible(visible);
    createSynapseAccountUI.setVisible(!visible);
  }

  private boolean checkUsernameFormat() {
    synAlert.clear();
    if (
      userNameField.getValue().length() > 3 &&
      ValidationUtils.isValidUsername(userNameField.getValue())
    ) {
      return true;
    } else {
      synAlert.showError(DisplayConstants.USERNAME_FORMAT_ERROR);
      return false;
    }
  }

  @Override
  public void setRegisterWidget(Widget w) {
    registerWidgetContainer.clear();
    registerWidgetContainer.add(w);
  }

  @Override
  public void setPresenter(final Presenter presenter) {
    this.presenter = presenter;
    headerWidget.configure();
    headerWidget.refresh();
    Window.scrollTo(0, 0); // scroll user to top of page
  }

  @Override
  public void setGoogleRegisterButtonEnabled(boolean enabled) {
    googleSignUpButton.setEnabled(enabled);
  }

  @Override
  public void setGoogleSynAlert(SynapseAlert w) {
    googleSynAlertContainer.clear();
    googleSynAlertContainer.add(w);
    synAlert = w;
  }
}
