package org.sagebionetworks.web.client.widget.accessrequirements;

import javax.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.ImposeRestrictionDialogProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class ImposeRestrictionDialog extends ReactComponent {

  @Inject
  public ImposeRestrictionDialog() {}

  private void renderComponent(ImposeRestrictionDialogProps props) {
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.ImposeRestrictionDialog,
      props
    );
    this.render(component);
  }

  public void configure(
    String entityId,
    boolean openModal,
    ImposeRestrictionDialogProps.Callback onSuccess
  ) {
    ImposeRestrictionDialogProps props = ImposeRestrictionDialogProps.create(
      entityId,
      openModal,
      () -> this.configure(entityId, false, onSuccess),
      onSuccess
    );
    this.renderComponent(props);
  }
}
