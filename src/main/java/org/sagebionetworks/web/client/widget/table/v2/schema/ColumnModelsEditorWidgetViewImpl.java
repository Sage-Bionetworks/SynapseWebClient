package org.sagebionetworks.web.client.widget.table.v2.schema;

import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.TableColumnSchemaEditorProps;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class ColumnModelsEditorWidgetViewImpl
  implements ColumnModelsEditorWidgetView {

  private final ReactComponent reactComponent;

  @Inject
  public ColumnModelsEditorWidgetViewImpl() {
    super();
    reactComponent = new ReactComponent();
  }

  @Override
  public void renderComponent(TableColumnSchemaEditorProps props) {
    ReactElement reactElement = React.createElementWithSynapseContext(
      SRC.SynapseComponents.TableColumnSchemaEditor,
      props
    );
    reactComponent.render(reactElement);
  }

  @Override
  public Widget asWidget() {
    return reactComponent.asWidget();
  }
}
