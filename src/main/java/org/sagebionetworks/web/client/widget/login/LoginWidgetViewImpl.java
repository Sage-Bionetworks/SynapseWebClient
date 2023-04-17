package org.sagebionetworks.web.client.widget.login;

import com.google.gwt.place.shared.Place;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.LoginPageProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.users.RegisterAccountViewImpl;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class LoginWidgetViewImpl implements LoginWidgetView, IsWidget {

  public interface LoginWidgetViewImplUiBinder
    extends UiBinder<Widget, LoginWidgetViewImpl> {}

  @UiField
  ReactComponentDiv srcLoginContainer;

  Widget widget;
  SynapseJSNIUtils jsniUtils;
  GlobalApplicationState globalAppState;
  AuthenticationController authController;
  SynapseReactClientFullContextPropsProvider propsProvider;

  @Inject
  public LoginWidgetViewImpl(
    LoginWidgetViewImplUiBinder binder,
    SynapseJSNIUtils jsniUtils,
    GlobalApplicationState globalAppState,
    AuthenticationController authController,
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    widget = binder.createAndBindUi(this);
    this.jsniUtils = jsniUtils;
    this.globalAppState = globalAppState;
    this.authController = authController;
    this.propsProvider = propsProvider;
    widget.addAttachHandler(event -> {
      if (event.isAttached()) {
        LoginPageProps props = LoginPageProps.create(
          RegisterAccountViewImpl.OAUTH_CALLBACK_URL,
          null,
          () -> this.postLogin()
        );
        ReactNode component = React.createElementWithSynapseContext(
          SRC.SynapseComponents.LoginPage,
          props,
          propsProvider.getJsInteropContextProps()
        );
        srcLoginContainer.render(component);
      }
    });
  }

  public void postLogin() {
    Place defaultPlace = new Profile(
      Profile.VIEW_PROFILE_TOKEN,
      ProfileArea.PROJECTS
    );
    globalAppState.gotoLastPlace(defaultPlace);
    authController.checkForUserChange(null);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void clear() {}

  @Override
  public void setVisible(boolean visible) {
    widget.setVisible(visible);
  }
}
