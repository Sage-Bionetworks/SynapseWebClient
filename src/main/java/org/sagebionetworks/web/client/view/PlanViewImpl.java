package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.SynapseHomepageV2Props;
import org.sagebionetworks.web.client.widget.ReactComponent;
import org.sagebionetworks.web.client.widget.header.Header;

public class PlanViewImpl extends Composite implements PlansView {

  ReactComponent container;

  private Header headerWidget;
  private SynapseReactClientFullContextPropsProvider propsProvider;
  private GlobalApplicationState globalAppState;

  @Inject
  public PlanViewImpl(
    Header headerWidget,
    final SynapseReactClientFullContextPropsProvider propsProvider,
    GlobalApplicationState globalAppState
  ) {
    this.headerWidget = headerWidget;
    this.propsProvider = propsProvider;
    this.globalAppState = globalAppState;
    headerWidget.configure();
    container = new ReactComponent();
    initWidget(container);
  }

  @Override
  public void render() {
    headerWidget.configure();
    headerWidget.refresh();
    scrollToTop();
    SynapseHomepageV2Props props = SynapseHomepageV2Props.create(href -> {
      globalAppState.handleRelativePathClick(href);
    });

    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.SynapsePlansPage,
      props,
      propsProvider.getJsInteropContextProps()
    );

    container.render(component);
  }

  public void scrollToTop() {
    Window.scrollTo(0, 0);
  }
}
