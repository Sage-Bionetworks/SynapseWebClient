package org.sagebionetworks.web.client.view;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.TwoFactorBackupCodesProps;
import org.sagebionetworks.web.client.jsinterop.TwoFactorEnrollmentFormProps;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;
import org.sagebionetworks.web.client.widget.header.Header;

public class TwoFactorAuthViewImpl
  extends Composite
  implements TwoFactorAuthView {

  @UiField
  ReactComponentDiv reactContainer;

  @UiField
  Div synAlertContainer;

  private Presenter presenter;
  private Header headerWidget;
  SynapseJSNIUtils jsniUtils;
  SynapseReactClientFullContextPropsProvider propsProvider;

  public interface LoginViewImplBinder
    extends UiBinder<Widget, TwoFactorAuthViewImpl> {}

  @Inject
  public TwoFactorAuthViewImpl(
    LoginViewImplBinder uiBinder,
    Header headerWidget,
    SynapseJSNIUtils jsniUtils,
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    initWidget(uiBinder.createAndBindUi(this));
    this.headerWidget = headerWidget;
    this.jsniUtils = jsniUtils;
    this.propsProvider = propsProvider;
    headerWidget.configure();
  }

  @Override
  public void setPresenter(Presenter twoFactorAuthPresenter) {
    this.presenter = twoFactorAuthPresenter;
    headerWidget.configure();
    headerWidget.refresh();
    com.google.gwt.user.client.Window.scrollTo(0, 0); // scroll user to top of page
  }

  @Override
  public void showTwoFactorEnrollmentForm() {
    reactContainer.render(
      React.createElementWithSynapseContext(
        SRC.SynapseComponents.TwoFactorEnrollmentForm,
        TwoFactorEnrollmentFormProps.create(() ->
          presenter.onTwoFactorEnrollmentComplete()
        ),
        propsProvider.getJsInteropContextProps()
      )
    );
  }

  @Override
  public void showGenerateRecoveryCodes(boolean showWarning) {
    reactContainer.render(
      React.createElementWithSynapseContext(
        SRC.SynapseComponents.TwoFactorBackupCodes,
        TwoFactorBackupCodesProps.create(
          showWarning,
          () -> presenter.onGenerateRecoveryCodesComplete()
        ),
        propsProvider.getJsInteropContextProps()
      )
    );
  }

  @Override
  public void showErrorMessage(String message) {
    DisplayUtils.showErrorMessage(message);
  }

  @Override
  public void showLoading() {}

  @Override
  public void showInfo(String message) {
    DisplayUtils.showInfo(message);
  }

  @Override
  public void clear() {
    reactContainer.clear();
  }

  @Override
  public void setSynAlert(IsWidget w) {
    synAlertContainer.clear();
    synAlertContainer.add(w);
  }
}
