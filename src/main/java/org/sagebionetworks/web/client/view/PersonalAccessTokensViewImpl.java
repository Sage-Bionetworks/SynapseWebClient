package org.sagebionetworks.web.client.view;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Anchor;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.AccessTokenPageProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.presenter.PersonalAccessTokensPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;
import org.sagebionetworks.web.client.widget.header.Header;

public class PersonalAccessTokensViewImpl
  extends Composite
  implements PersonalAccessTokensView {

  public interface PersonalAccessTokensViewImplUiBinder
    extends UiBinder<Widget, PersonalAccessTokensViewImpl> {}

  private static String PAGE_TITLE = "Personal Access Tokens";
  private static String PAGE_BODY_COPY =
    "Issue personal access tokens to access your Synapse resources in the command line clients. A personal access token will expire if it is unused for 180 consecutive days. You may create up to 100 personal access tokens.";

  @UiField
  ReactComponentDiv container;

  @UiField
  Anchor backToSettingsAnchor;

  private PersonalAccessTokensPresenter presenter;
  private Header headerWidget;
  private SynapseReactClientFullContextPropsProvider propsProvider;

  @Inject
  public PersonalAccessTokensViewImpl(
    PersonalAccessTokensViewImplUiBinder uiBinder,
    AuthenticationController authenticationController,
    Header headerWidget,
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    initWidget(uiBinder.createAndBindUi(this));

    this.headerWidget = headerWidget;
    this.propsProvider = propsProvider;
    headerWidget.configure();

    backToSettingsAnchor.addClickHandler(event ->
      presenter.goTo(
        new Profile(
          authenticationController.getCurrentUserPrincipalId(),
          Synapse.ProfileArea.SETTINGS
        )
      )
    );
  }

  @Override
  public void render() {
    Window.scrollTo(0, 0); // scroll user to top of page
    AccessTokenPageProps props = AccessTokenPageProps.create(
      PAGE_TITLE,
      PAGE_BODY_COPY
    );
    ReactNode component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.AccessTokenPage,
      props,
      propsProvider.getJsInteropContextProps()
    );
    container.render(component);
  }

  @Override
  public void setPresenter(PersonalAccessTokensPresenter presenter) {
    this.presenter = presenter;
    headerWidget.refresh();
  }
}
