package org.sagebionetworks.web.client.widget.table.modal.fileview;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.CreateTableViewWizardProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class CreateTableViewWizard implements IsWidget {

  private final SynapseReactClientFullContextPropsProvider propsProvider;

  private final ReactComponentDiv reactComponentDiv;
  private String parentId;
  private CreateTableViewWizardProps.OnComplete onComplete;
  private CreateTableViewWizardProps.OnCancel onCancel;

  @Inject
  public CreateTableViewWizard(
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    super();
    this.propsProvider = propsProvider;
    reactComponentDiv = new ReactComponentDiv();
  }

  private void renderComponent(CreateTableViewWizardProps props) {
    ReactNode reactNode = React.createElementWithSynapseContext(
      SRC.SynapseComponents.CreateTableViewWizard,
      props,
      propsProvider.getJsInteropContextProps()
    );
    reactComponentDiv.render(reactNode);
  }

  public void configure(
    String parentId,
    CreateTableViewWizardProps.OnComplete onComplete,
    CreateTableViewWizardProps.OnCancel onCancel
  ) {
    reactComponentDiv.clear();
    this.parentId = parentId;
    this.onComplete = onComplete;
    this.onCancel = onCancel;
    CreateTableViewWizardProps props = CreateTableViewWizardProps.create(
      false,
      parentId,
      onComplete,
      onCancel
    );

    renderComponent(props);
  }

  public void setOpen(boolean open) {
    CreateTableViewWizardProps props = CreateTableViewWizardProps.create(
      open,
      parentId,
      onComplete,
      onCancel
    );
    renderComponent(props);
  }

  @Override
  public Widget asWidget() {
    return reactComponentDiv.asWidget();
  }
}
