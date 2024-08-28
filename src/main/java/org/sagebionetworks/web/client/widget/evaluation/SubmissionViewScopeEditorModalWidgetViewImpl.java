package org.sagebionetworks.web.client.widget.evaluation;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.SubmissionViewScopeEditorModalProps;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class SubmissionViewScopeEditorModalWidgetViewImpl
  implements SubmissionViewScopeEditorModalWidgetView {

  private final SynapseReactClientFullContextPropsProvider propsProvider;
  private final ReactComponent reactComponent;

  @Inject
  public SubmissionViewScopeEditorModalWidgetViewImpl(
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    super();
    this.propsProvider = propsProvider;
    reactComponent = new ReactComponent();
  }

  @Override
  public void renderComponent(SubmissionViewScopeEditorModalProps props) {
    ReactElement reactElement = React.createElementWithSynapseContext(
      SRC.SynapseComponents.SubmissionViewScopeEditorModal,
      props,
      propsProvider.getJsInteropContextProps()
    );
    reactComponent.render(reactElement);
  }

  @Override
  public Widget asWidget() {
    return reactComponent.asWidget();
  }
}
