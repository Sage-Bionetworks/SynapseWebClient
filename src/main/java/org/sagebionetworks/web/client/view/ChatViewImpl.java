package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import javax.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.SynapseChatProps;
import org.sagebionetworks.web.client.widget.ReactComponent;
import org.sagebionetworks.web.client.widget.header.Header;

public class ChatViewImpl extends Composite implements ChatView {

  ReactComponent container;

  private Header headerWidget;

  @Inject
  public ChatViewImpl(Header headerWidget) {
    this.headerWidget = headerWidget;
    headerWidget.configure();
    container = new ReactComponent();
    initWidget(container);
  }

  @Override
  public void render(
    String initMessage,
    String agentRegistrationId,
    String chatbotName
  ) {
    headerWidget.configure();
    headerWidget.refresh();
    scrollToTop();
    SynapseChatProps props = SynapseChatProps.create(
      initMessage,
      agentRegistrationId,
      chatbotName
    );
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.SynapseChat,
      props
    );

    container.render(component);
  }

  @Override
  public void scrollToTop() {
    Window.scrollTo(0, 0);
  }
}
