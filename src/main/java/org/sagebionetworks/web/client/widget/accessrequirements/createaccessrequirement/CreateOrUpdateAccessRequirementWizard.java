package org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.CreateOrUpdateAccessRequirementWizardProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class CreateOrUpdateAccessRequirementWizard implements IsWidget {

  private final SynapseReactClientFullContextPropsProvider propsProvider;

  private final ReactComponent reactComponent;

  private boolean open;
  private RestrictableObjectDescriptor subject;
  private String accessRequirementId;
  private CreateOrUpdateAccessRequirementWizardProps.OnComplete onComplete;
  private CreateOrUpdateAccessRequirementWizardProps.OnCancel onCancel;

  @Inject
  public CreateOrUpdateAccessRequirementWizard(
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    super();
    this.propsProvider = propsProvider;
    reactComponent = new ReactComponent();
  }

  private void renderComponent() {
    CreateOrUpdateAccessRequirementWizardProps props =
      CreateOrUpdateAccessRequirementWizardProps.create(
        this.open,
        this.subject,
        this.accessRequirementId,
        this.onComplete,
        this.onCancel
      );

    ReactNode reactNode = React.createElementWithSynapseContext(
      SRC.SynapseComponents.CreateOrUpdateAccessRequirementWizard,
      props,
      propsProvider.getJsInteropContextProps()
    );
    reactComponent.render(reactNode);
  }

  public void configure(
    RestrictableObjectDescriptor subject,
    CreateOrUpdateAccessRequirementWizardProps.OnComplete onComplete,
    CreateOrUpdateAccessRequirementWizardProps.OnCancel onCancel
  ) {
    reactComponent.clear();

    this.subject = subject;
    this.onComplete = onComplete;
    this.onCancel = onCancel;

    this.open = false;
    this.accessRequirementId = null;

    renderComponent();
  }

  public void configure(
    AccessRequirement accessRequirement,
    CreateOrUpdateAccessRequirementWizardProps.OnComplete onComplete,
    CreateOrUpdateAccessRequirementWizardProps.OnCancel onCancel
  ) {
    reactComponent.clear();

    this.accessRequirementId = accessRequirement.getId().toString();
    this.onComplete = onComplete;
    this.onCancel = onCancel;

    this.open = false;
    this.subject = null;

    renderComponent();
  }

  public void setOpen(boolean open) {
    this.open = open;
    renderComponent();
  }

  @Override
  public Widget asWidget() {
    return reactComponent.asWidget();
  }
}
