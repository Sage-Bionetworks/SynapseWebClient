package org.sagebionetworks.web.client.widget.table.v2.schema;

import com.google.gwt.user.client.ui.IsWidget;
import java.util.List;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ViewScope;

public interface ColumnModelsEditorV2WidgetView extends IsWidget {
  void configure(
    EntityType entityType,
    ViewScope viewScope,
    List<ColumnModel> startingModels
  );

  List<ColumnModel> getEditedColumnModels();

  boolean validate();
}
