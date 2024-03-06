package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.*;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class EntityModalWidgetViewImpl implements EntityModalWidgetView {

  private final SynapseReactClientFullContextPropsProvider propsProvider;
  private final ReactComponentDiv reactComponentDiv;

  @Inject
  public EntityModalWidgetViewImpl(
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    super();
    this.propsProvider = propsProvider;
    reactComponentDiv = new ReactComponentDiv();
  }

  @Override
  public void renderComponent(EntityModalProps props) {
    ReactNode reactNode = React.createElementWithSynapseContext(
      SRC.SynapseComponents.EntityModal,
      props,
      propsProvider.getJsInteropContextProps()
    );
    reactComponentDiv.render(reactNode);
  }

  @Override
  public Widget asWidget() {
    return reactComponentDiv.asWidget();
  }
}
