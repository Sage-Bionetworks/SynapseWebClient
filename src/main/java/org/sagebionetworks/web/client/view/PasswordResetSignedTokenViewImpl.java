package org.sagebionetworks.web.client.view;

import static org.sagebionetworks.web.client.DisplayConstants.AUTOCOMPLETE_ATTRIBUTE;
import static org.sagebionetworks.web.client.DisplayConstants.AUTOCOMPLETE_VALUE_NEW_PASSWORD;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Input;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.header.Header;

public class PasswordResetSignedTokenViewImpl
  implements PasswordResetSignedTokenView {

  public interface PasswordResetSignedTokenViewImplUiBinder
    extends UiBinder<Widget, PasswordResetSignedTokenViewImpl> {}

  @UiField
  Input password1Field;

  @UiField
  Input password2Field;

  @UiField
  SimplePanel passwordSynAlertPanel;

  @UiField
  Button changePasswordBtn;

  private Presenter presenter;
  Header headerWidget;
  Widget w;

  @Inject
  public PasswordResetSignedTokenViewImpl(
    PasswordResetSignedTokenViewImplUiBinder binder,
    Header headerWidget
  ) {
    w = binder.createAndBindUi(this);
    this.headerWidget = headerWidget;
    changePasswordBtn.addClickHandler(event -> {
      presenter.onChangePassword();
    });

    password1Field
      .getElement()
      .setAttribute(AUTOCOMPLETE_ATTRIBUTE, AUTOCOMPLETE_VALUE_NEW_PASSWORD);
    password2Field
      .getElement()
      .setAttribute(AUTOCOMPLETE_ATTRIBUTE, AUTOCOMPLETE_VALUE_NEW_PASSWORD);

    headerWidget.configure();
  }

  @Override
  public void setPresenter(final Presenter presenter) {
    this.presenter = presenter;
    headerWidget.configure();
    headerWidget.refresh();
    Window.scrollTo(0, 0); // scroll user to top of page
  }

  @Override
  public void showPasswordChangeSuccess() {
    clear();
    DisplayUtils.showInfo("Password has been successfully changed");
  }

  @Override
  public String getPassword1Field() {
    return password1Field.getText();
  }

  @Override
  public String getPassword2Field() {
    return password2Field.getText();
  }

  @Override
  public void clear() {
    password1Field.setValue("");
    password2Field.setValue("");
    changePasswordBtn.setEnabled(true);
  }

  @Override
  public void setChangePasswordEnabled(boolean isEnabled) {
    changePasswordBtn.setEnabled(isEnabled);
  }

  @Override
  public void setSynAlertWidget(IsWidget synAlert) {
    passwordSynAlertPanel.setWidget(synAlert);
  }

  @Override
  public Widget asWidget() {
    return w;
  }
}
