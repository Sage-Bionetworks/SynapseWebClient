package org.sagebionetworks.web.client.widget.table.v2.schema;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.jsinterop.TableColumnSchemaEditorProps;

public interface ColumnModelsEditorWidgetView extends IsWidget {
  void renderComponent(TableColumnSchemaEditorProps props);
}
