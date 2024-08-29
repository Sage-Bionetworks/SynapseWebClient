package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.IDUReportProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class IntendedDataUseReportWidgetViewImpl
  implements IntendedDataUseReportWidgetView {

  ReactComponent reactComponent;
  SynapseReactClientFullContextPropsProvider propsProvider;

  @Inject
  public IntendedDataUseReportWidgetViewImpl(
    ReactComponent reactComponent,
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    this.reactComponent = reactComponent;
    this.propsProvider = propsProvider;
  }

  @Override
  public void render(String accessRequirementId) {
    IDUReportProps props = IDUReportProps.create(accessRequirementId);

    ReactElement node = React.createElementWithSynapseContext(
      SRC.SynapseComponents.IDUReport,
      props,
      propsProvider.getJsInteropContextProps()
    );
    reactComponent.render(node);
  }

  @Override
  public Widget asWidget() {
    return reactComponent;
  }
}
