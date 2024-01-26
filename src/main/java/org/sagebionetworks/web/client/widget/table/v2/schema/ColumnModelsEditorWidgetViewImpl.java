package org.sagebionetworks.web.client.widget.table.v2.schema;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.TableColumnSchemaEditorProps;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class ColumnModelsEditorWidgetViewImpl
  implements ColumnModelsEditorWidgetView {

  private final SynapseReactClientFullContextPropsProvider propsProvider;

  private final ReactComponentDiv reactComponentDiv;

  @Inject
  public ColumnModelsEditorWidgetViewImpl(
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    super();
    this.propsProvider = propsProvider;
    reactComponentDiv = new ReactComponentDiv();
  }

  @Override
  public void renderComponent(TableColumnSchemaEditorProps props) {
    ReactNode reactNode = React.createElementWithSynapseContext(
      SRC.SynapseComponents.TableColumnSchemaEditor,
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
