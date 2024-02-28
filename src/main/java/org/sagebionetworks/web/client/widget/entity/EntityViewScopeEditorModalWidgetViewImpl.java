package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.EntityViewScopeEditorModalProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class EntityViewScopeEditorModalWidgetViewImpl
  implements EntityViewScopeEditorModalWidgetView {

  private final SynapseReactClientFullContextPropsProvider propsProvider;
  private final ReactComponentDiv reactComponentDiv;

  @Inject
  public EntityViewScopeEditorModalWidgetViewImpl(
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    super();
    this.propsProvider = propsProvider;
    reactComponentDiv = new ReactComponentDiv();
  }

  @Override
  public void renderComponent(EntityViewScopeEditorModalProps props) {
    ReactNode reactNode = React.createElementWithSynapseContext(
      SRC.SynapseComponents.EntityViewScopeEditorModal,
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
