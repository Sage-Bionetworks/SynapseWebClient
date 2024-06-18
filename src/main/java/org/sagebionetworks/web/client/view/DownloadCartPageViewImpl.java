package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.DownloadCartPageProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.ReactComponent;
import org.sagebionetworks.web.client.widget.header.Header;

public class DownloadCartPageViewImpl implements DownloadCartPageView {

  ReactComponent container;

  private Header headerWidget;
  private SynapseReactClientFullContextPropsProvider propsProvider;
  private Presenter presenter;

  @Inject
  public DownloadCartPageViewImpl(
    AuthenticationController authenticationController,
    Header headerWidget,
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    container = new ReactComponent();
    this.headerWidget = headerWidget;
    this.propsProvider = propsProvider;
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void render() {
    Window.scrollTo(0, 0); // scroll user to top of page
    headerWidget.configure();
    DownloadCartPageProps props = DownloadCartPageProps.create(entityId -> {
      presenter.onViewSharingSettingsClicked(entityId);
    });
    ReactNode component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.DownloadCartPage,
      props,
      propsProvider.getJsInteropContextProps()
    );
    container.render(component);
  }

  @Override
  public Widget asWidget() {
    return container.asWidget();
  }
}
