package org.sagebionetworks.web.client.widget.table.v2.schema;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.List;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ViewScope;

public class ColumnModelsEditorV2Widget implements IsWidget {

  private final ColumnModelsEditorV2WidgetView view;

  @Inject
  public ColumnModelsEditorV2Widget(ColumnModelsEditorV2WidgetView view) {
    this.view = view;
  }

  public void configure(
    EntityType entityType,
    ViewScope viewScope,
    List<ColumnModel> startingModels
  ) {
    this.view.configure(entityType, viewScope, startingModels);
  }

  public List<ColumnModel> getEditedColumnModels() {
    return this.view.getEditedColumnModels();
  }

  public boolean validate() {
    return this.view.validate();
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }
}
