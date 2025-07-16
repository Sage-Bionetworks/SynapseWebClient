package org.sagebionetworks.web.client.widget.accessrequirements;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.web.client.jsinterop.EntityHeaderTableProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class EntitySubjectsWidgetViewImpl implements EntitySubjectsWidgetView {

  ReactComponent reactContainer;
  Presenter presenter;

  @Inject
  public EntitySubjectsWidgetViewImpl() {
    reactContainer = new ReactComponent();
  }

  @Override
  public Widget asWidget() {
    return reactContainer;
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void setVisible(boolean visible) {
    reactContainer.setVisible(visible);
  }

  @Override
  public void showEntityHeadersTable(
    ReferenceList entityReferences,
    boolean isEditable
  ) {
    ReactElement element = React.createElementWithSynapseContext(
      SRC.SynapseComponents.EntityHeaderTable,
      EntityHeaderTableProps.create(
        entityReferences.getReferences(),
        isEditable,
        newRefList -> {
          presenter.onChange(newRefList);
        },
        "Mark for Removal from AR",
        newEntityIDsValue -> {
          presenter.onChangeEntityIDsValue(newEntityIDsValue);
        }
      )
    );
    reactContainer.render(element);
  }
}
