package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.SqlDefinedTableEditorModalProps;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class SqlDefinedEditorModalWidgetViewImpl
  implements SqlDefinedEditorModalWidgetView {

  private final SynapseReactClientFullContextPropsProvider propsProvider;
  private final ReactComponent reactComponent;

  @Inject
  public SqlDefinedEditorModalWidgetViewImpl(
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    super();
    this.propsProvider = propsProvider;
    reactComponent = new ReactComponent();
  }

  @Override
  public void renderComponent(SqlDefinedTableEditorModalProps props) {
    ReactNode reactNode = React.createElementWithSynapseContext(
      SRC.SynapseComponents.SqlDefinedTableEditorModal,
      props,
      propsProvider.getJsInteropContextProps()
    );
    reactComponent.render(reactNode);
  }

  @Override
  public Widget asWidget() {
    return reactComponent.asWidget();
  }
}
