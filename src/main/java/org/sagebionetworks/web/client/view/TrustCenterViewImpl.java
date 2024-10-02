package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.GovernanceMarkdownGithubProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;
import org.sagebionetworks.web.client.widget.header.Header;

public class TrustCenterViewImpl extends Composite implements TrustCenterView {

  ReactComponent container = new ReactComponent();

  private Header headerWidget;
  private SynapseReactClientFullContextPropsProvider propsProvider;

  @Inject
  public TrustCenterViewImpl(
    Header headerWidget,
    final SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    initWidget(container);

    this.headerWidget = headerWidget;
    this.propsProvider = propsProvider;
    headerWidget.configure();
  }

  @Override
  public void render(String repoOwner, String repoName, String filePath) {
    scrollToTop();
    ReactElement component;

    GovernanceMarkdownGithubProps props = GovernanceMarkdownGithubProps.create(
      repoOwner,
      repoName,
      filePath
    );
    component =
      React.createElementWithSynapseContext(
        SRC.SynapseComponents.GovernanceMarkdownGithub,
        props,
        propsProvider.getJsInteropContextProps()
      );

    container.render(component);
  }

  @Override
  public void refresh() {
    headerWidget.configure();
    headerWidget.refresh();
  }

  @Override
  public void scrollToTop() {
    Window.scrollTo(0, 0);
  }
}
