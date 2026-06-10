package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.SqlDefinedTableEditorModalProps;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class SqlDefinedEditorModalWidgetViewImpl
  implements SqlDefinedEditorModalWidgetView {

  private final ReactComponent reactComponent;

  @Inject
  public SqlDefinedEditorModalWidgetViewImpl() {
    super();
    reactComponent = new ReactComponent();
  }

  @Override
  public void renderComponent(SqlDefinedTableEditorModalProps props) {
    ReactElement reactElement = React.createElementWithSynapseContext(
      SRC.SynapseComponents.SqlDefinedTableEditorModal,
      props
    );
    reactComponent.render(reactElement);
  }

  @Override
  public Widget asWidget() {
    return reactComponent.asWidget();
  }
}
