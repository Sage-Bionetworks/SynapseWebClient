package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.ReviewerDashboardProps;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;
import org.sagebionetworks.web.client.widget.header.Header;

public class UserAccessRequestHistoryViewImpl
  implements UserAccessRequestHistoryView {

  private final SynapseReactClientFullContextPropsProvider propsProvider;
  private final Header headerWidget;

  private final ReactComponent reactComponent = new ReactComponent();

  @Inject
  public UserAccessRequestHistoryViewImpl(
    Header headerWidget,
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    this.headerWidget = headerWidget;
    this.propsProvider = propsProvider;
    headerWidget.configure();
  }

  @Override
  public void render() {
    headerWidget.configure();
    headerWidget.refresh();
    Window.scrollTo(0, 0);

    ReviewerDashboardProps props = ReviewerDashboardProps.create(
      "/RequestHistory:default"
    );

    ReactElement node = React.createElementWithSynapseContext(
      SRC.SynapseComponents.UserAccessRequestHistoryPlace,
      props,
      propsProvider.getJsInteropContextProps()
    );
    reactComponent.render(node);
  }

  @Override
  public Widget asWidget() {
    return reactComponent.asWidget();
  }
}
