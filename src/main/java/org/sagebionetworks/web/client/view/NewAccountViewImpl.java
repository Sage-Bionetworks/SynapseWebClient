package org.sagebionetworks.web.client.view;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.ValidationUtils;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.pageprogress.PageProgressWidget;
import org.sagebionetworks.web.shared.WebConstants;

public class NewAccountViewImpl extends Composite implements NewAccountView {

  public interface NewAccountViewImplUiBinder
    extends UiBinder<Widget, NewAccountViewImpl> {}

  @UiField
  TextBox emailField;

  @UiField
  TextBox firstNameField;

  @UiField
  TextBox lastNameField;

  @UiField
  TextBox userNameField;

  @UiField
  Input password1Field;

  @UiField
  Input password2Field;

  @UiField
  Div pageProgressContainer;

  @UiField
  Div synAlertContainer;

  private Presenter presenter;
  private Header headerWidget;
  private PageProgressWidget pageProgressWidget;
  private GlobalApplicationState globalAppState;
  private SynapseAlert synAlert;

  @Inject
  public NewAccountViewImpl(
    NewAccountViewImplUiBinder binder,
    Header headerWidget,
    PageProgressWidget pageProgressWidget,
    GlobalApplicationState globalAppState
  ) {
    initWidget(binder.createAndBindUi(this));
    this.headerWidget = headerWidget;
    this.pageProgressWidget = pageProgressWidget;
    this.globalAppState = globalAppState;
    headerWidget.configure();
    init();
  }

  private void onRegisterClick() {
    if (
      checkUsernameFormat() &&
      checkPassword1() &&
      checkPassword2() &&
      checkPasswordMatch()
    ) {
      // formatting is ok. submit to presenter (will fail if one is taken)
      presenter.completeRegistration(
        userNameField.getValue(),
        firstNameField.getValue(),
        lastNameField.getValue(),
        password1Field.getValue()
      );
    }
  }

  // Apply to all input fields if clickEvent is enter
  public void init() {
    KeyDownHandler register = event -> {
      if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
        onRegisterClick();
      }
    };
    emailField.addKeyDownHandler(register);
    firstNameField.addKeyDownHandler(register);
    lastNameField.addKeyDownHandler(register);
    userNameField.addKeyDownHandler(register);
    password1Field.addKeyDownHandler(register);
    password2Field.addKeyDownHandler(register);

    userNameField.addBlurHandler(event -> {
      boolean enableNext = checkUsernameFormat();
      reconfigurePageProgressWidget(enableNext);
    });
    password1Field.addBlurHandler(event -> {
      boolean enableNext = (checkPassword1() && checkUsernameFormat());
      reconfigurePageProgressWidget(enableNext);
    });
    password2Field.addBlurHandler(event -> {
      boolean enableNext =
        (checkPassword2() && checkPasswordMatch() && checkUsernameFormat());
      reconfigurePageProgressWidget(enableNext);
    });

    pageProgressContainer.add(pageProgressWidget);
    reconfigurePageProgressWidget(false);
  }

  public void reconfigurePageProgressWidget(boolean enableNext) {
    Callback backBtnCallback = () -> {
      globalAppState
        .getPlaceChanger()
        .goTo(new RegisterAccount(ClientProperties.DEFAULT_PLACE_TOKEN));
    };
    Callback forwardBtnCallback = () -> {
      onRegisterClick();
    };

    pageProgressWidget.configure(
      WebConstants.SYNAPSE_GREEN,
      50,
      "Back",
      backBtnCallback,
      "Next",
      forwardBtnCallback,
      enableNext
    );
  }

  private boolean checkUsernameFormat() {
    synAlert.clear();
    if (ValidationUtils.isValidUsername(userNameField.getValue())) {
      presenter.checkUsernameAvailable(userNameField.getValue());
      return true;
    } else {
      synAlert.showError(DisplayConstants.USERNAME_FORMAT_ERROR);
      return false;
    }
  }

  @Override
  public void setPresenter(final Presenter presenter) {
    this.presenter = presenter;
    headerWidget.configure();
    headerWidget.refresh();
    Window.scrollTo(0, 0); // scroll user to top of page
  }

  @Override
  public void showErrorMessage(String errorMessage) {
    DisplayUtils.showErrorMessage(errorMessage);
  }

  @Override
  public void setLoading(boolean loading) {}

  @Override
  public void showLoading() {}

  @Override
  public void showInfo(String message) {
    DisplayUtils.showInfo(message);
  }

  @Override
  public void clear() {
    firstNameField.setValue("");
    lastNameField.setValue("");
    userNameField.setValue("");
    password1Field.setValue("");
    password2Field.setValue("");
    emailField.setValue("");
    synAlert.clear();
  }

  @Override
  public void markUsernameUnavailable() {
    synAlert.showError(DisplayConstants.ERROR_USERNAME_ALREADY_EXISTS);
    reconfigurePageProgressWidget(false);
  }

  private boolean checkPassword1() {
    synAlert.clear();
    if (!DisplayUtils.isDefined(password1Field.getText())) {
      synAlert.showError(DisplayConstants.ERROR_ALL_FIELDS_REQUIRED);
      return false;
    } else return true;
  }

  private boolean checkPassword2() {
    synAlert.clear();
    if (!DisplayUtils.isDefined(password2Field.getText())) {
      synAlert.showError(DisplayConstants.ERROR_ALL_FIELDS_REQUIRED);
      return false;
    } else return true;
  }

  private boolean checkPasswordMatch() {
    synAlert.clear();
    if (!password1Field.getValue().equals(password2Field.getValue())) {
      synAlert.showError(DisplayConstants.PASSWORDS_MISMATCH);
      return false;
    } else return true;
  }

  @Override
  public void setSynAlert(SynapseAlert synAlert) {
    this.synAlert = synAlert;
    synAlertContainer.clear();
    synAlertContainer.add(synAlert);
  }

  @Override
  public void setEmail(String email) {
    emailField.setValue(email);
  }
}
