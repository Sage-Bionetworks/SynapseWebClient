package org.sagebionetworks.web.client.widget.accessrequirements;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.EntityHeaderTableProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class EntitySubjectsWidgetViewImpl implements EntitySubjectsWidgetView {

  ReactComponent reactContainer;
  SynapseReactClientFullContextPropsProvider propsProvider;
  Presenter presenter;

  @Inject
  public EntitySubjectsWidgetViewImpl(
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    reactContainer = new ReactComponent();
    this.propsProvider = propsProvider;
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
    ReactNode element = React.createElementWithSynapseContext(
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
      ),
      propsProvider.getJsInteropContextProps()
    );
    reactContainer.render(element);
  }
}
