package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.EntityViewScopeEditorModalProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class EntityViewScopeEditorModalWidgetViewImpl
  implements EntityViewScopeEditorModalWidgetView {

  private final ReactComponent reactComponent;

  @Inject
  public EntityViewScopeEditorModalWidgetViewImpl() {
    super();
    reactComponent = new ReactComponent();
  }

  @Override
  public void renderComponent(EntityViewScopeEditorModalProps props) {
    ReactElement reactElement = React.createElementWithSynapseContext(
      SRC.SynapseComponents.EntityViewScopeEditorModal,
      props
    );
    reactComponent.render(reactElement);
  }

  @Override
  public Widget asWidget() {
    return reactComponent.asWidget();
  }
}
