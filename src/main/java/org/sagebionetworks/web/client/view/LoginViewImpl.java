package org.sagebionetworks.web.client.view;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.TermsAndConditionsProps;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.FullWidthAlert;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.login.LoginWidget;
import org.sagebionetworks.web.client.widget.pageprogress.PageProgressWidget;
import org.sagebionetworks.web.shared.WebConstants;

public class LoginViewImpl extends Composite implements LoginView {

  @UiField
  SimplePanel loginWidgetPanel;

  @UiField
  HTMLPanel loginView;

  // terms of service view
  @UiField
  Div termsOfUseView;

  @UiField
  ReactComponentDiv termsOfUseContainer;

  @UiField
  FullWidthAlert acceptedTermsOfUseView;

  @UiField
  LoadingSpinner loadingUi;

  @UiField
  Heading loadingUiText;

  @UiField
  Div synAlertContainer;

  @UiField
  Div pageProgressContainer;

  private Presenter presenter;
  private LoginWidget loginWidget;
  private Header headerWidget;
  SynapseJSNIUtils jsniUtils;
  SynapseReactClientFullContextPropsProvider propsProvider;
  PageProgressWidget pageProgressWidget;
  Callback backBtnCallback, forwardBtnCallback;

  public interface LoginViewImplBinder
    extends UiBinder<Widget, LoginViewImpl> {}

  @Inject
  public LoginViewImpl(
    LoginViewImplBinder uiBinder,
    Header headerWidget,
    LoginWidget loginWidget,
    SynapseJSNIUtils jsniUtils,
    PageProgressWidget pageProgressWidget,
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    initWidget(uiBinder.createAndBindUi(this));
    this.loginWidget = loginWidget;
    this.headerWidget = headerWidget;
    this.pageProgressWidget = pageProgressWidget;
    this.jsniUtils = jsniUtils;
    this.propsProvider = propsProvider;
    headerWidget.configure();
    pageProgressContainer.add(pageProgressWidget);
    backBtnCallback =
      () -> {
        presenter.onCancelAcceptTermsOfUse();
      };
    forwardBtnCallback =
      () -> {
        presenter.onAcceptTermsOfUse();
      };
  }

  private void reconfigurePageProgress(boolean enableForward) {
    pageProgressWidget.configure(
      WebConstants.SYNAPSE_GREEN,
      75,
      "Cancel",
      backBtnCallback,
      "Next",
      forwardBtnCallback,
      enableForward
    );
  }

  @Override
  public void setPresenter(Presenter loginPresenter) {
    this.presenter = loginPresenter;
    headerWidget.configure();
    headerWidget.refresh();
    com.google.gwt.user.client.Window.scrollTo(0, 0); // scroll user to top of page
  }

  @Override
  public void showLoggingInLoader() {
    hideViews();
    loadingUi.setVisible(true);
    loadingUiText.setVisible(true);
  }

  @Override
  public void hideLoggingInLoader() {
    loadingUi.setVisible(false);
    loadingUiText.setVisible(false);
  }

  @Override
  public void showLogin() {
    clear();
    hideViews();
    loginView.setVisible(true);
    headerWidget.refresh();

    // Add the widget to the panel
    loginWidget.asWidget().removeFromParent();
    loginWidgetPanel.setWidget(loginWidget.asWidget());
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
    loginWidget.clear();
    loginWidgetPanel.clear();
  }

  @Override
  public void showTermsOfUse(boolean hasAccepted) {
    hideViews();

    if (!hasAccepted) {
      termsOfUseView.setVisible(true);
      reconfigurePageProgress(false);
      TermsAndConditionsProps props = TermsAndConditionsProps.create(
        this::onFormChange
      );
      ReactNode component = React.createElementWithSynapseContext(
        SRC.SynapseComponents.TermsAndConditions,
        props,
        propsProvider.getJsInteropContextProps()
      );
      termsOfUseContainer.render(component);
    } else {
      acceptedTermsOfUseView.setVisible(true);
    }
  }

  public void onFormChange(boolean completed) {
    reconfigurePageProgress(completed);
  }

  private void hideViews() {
    loadingUi.setVisible(false);
    loadingUiText.setVisible(false);
    loginView.setVisible(false);
    termsOfUseView.setVisible(false);
    acceptedTermsOfUseView.setVisible(false);
  }

  @Override
  public void setSynAlert(IsWidget w) {
    synAlertContainer.clear();
    synAlertContainer.add(w);
  }
}
