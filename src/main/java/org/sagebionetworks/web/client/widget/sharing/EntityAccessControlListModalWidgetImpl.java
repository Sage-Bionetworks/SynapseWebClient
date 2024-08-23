package org.sagebionetworks.web.client.widget.sharing;

import static org.sagebionetworks.web.client.jsinterop.SRC.SynapseComponents.EntityAclEditorModal;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.EntityAclEditorModalProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class EntityAccessControlListModalWidgetImpl
  implements EntityAccessControlListModalWidget {

  private final ReactComponent reactComponent;
  private final SynapseReactClientFullContextPropsProvider propsProvider;

  private EntityAclEditorModalProps componentProps;

  @Inject
  EntityAccessControlListModalWidgetImpl(
    ReactComponent reactComponent,
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    this.reactComponent = reactComponent;
    this.propsProvider = propsProvider;
  }

  @Override
  public void configure(
    String entityId,
    EntityAclEditorModalProps.Callback onUpdateSuccess
  ) {
    componentProps =
      EntityAclEditorModalProps.create(
        entityId,
        false,
        onUpdateSuccess,
        () -> setOpen(false)
      );
    renderComponent();
  }

  @Override
  public void setOpen(boolean open) {
    componentProps.open = open;
    renderComponent();
  }

  @Override
  public Widget asWidget() {
    return reactComponent.asWidget();
  }

  private void renderComponent() {
    ReactElement node = React.createElementWithSynapseContext(
      EntityAclEditorModal,
      componentProps,
      propsProvider.getJsInteropContextProps()
    );
    reactComponent.render(node);
  }
}
