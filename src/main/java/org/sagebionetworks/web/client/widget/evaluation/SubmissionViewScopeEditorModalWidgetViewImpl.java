package org.sagebionetworks.web.client.widget.evaluation;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.SubmissionViewScopeEditorModalProps;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class SubmissionViewScopeEditorModalWidgetViewImpl
  implements SubmissionViewScopeEditorModalWidgetView {

  private final SynapseReactClientFullContextPropsProvider propsProvider;
  private final ReactComponentDiv reactComponentDiv;

  @Inject
  public SubmissionViewScopeEditorModalWidgetViewImpl(
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    super();
    this.propsProvider = propsProvider;
    reactComponentDiv = new ReactComponentDiv();
  }

  @Override
  public void renderComponent(SubmissionViewScopeEditorModalProps props) {
    ReactNode reactNode = React.createElementWithSynapseContext(
      SRC.SynapseComponents.SubmissionViewScopeEditorModal,
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
