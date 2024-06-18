package org.sagebionetworks.web.client.widget.table.v2.schema;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.TableColumnSchemaEditorProps;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class ColumnModelsEditorWidgetViewImpl
  implements ColumnModelsEditorWidgetView {

  private final SynapseReactClientFullContextPropsProvider propsProvider;

  private final ReactComponent reactComponent;

  @Inject
  public ColumnModelsEditorWidgetViewImpl(
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    super();
    this.propsProvider = propsProvider;
    reactComponent = new ReactComponent();
  }

  @Override
  public void renderComponent(TableColumnSchemaEditorProps props) {
    ReactNode reactNode = React.createElementWithSynapseContext(
      SRC.SynapseComponents.TableColumnSchemaEditor,
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
