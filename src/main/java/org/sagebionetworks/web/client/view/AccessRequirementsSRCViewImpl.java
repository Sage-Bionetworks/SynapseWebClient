package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.web.client.jsinterop.AccessRequirementListProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class AccessRequirementsSRCViewImpl
  implements AccessRequirementsSRCView {

  ReactComponent requestDataAccessWidget = new ReactComponent();

  @Inject
  public AccessRequirementsSRCViewImpl() {}

  @Override
  public void configure(
    RestrictableObjectType type,
    RestrictableObjectDescriptor subject
  ) {
    AccessRequirementListProps props = AccessRequirementListProps.create(
      null,
      null,
      subject.getId(),
      type,
      false
    );
    requestDataAccessWidget.render(
      React.createElementWithSynapseContext(
        SRC.SynapseComponents.AccessRequirementList,
        props
      )
    );
  }

  @Override
  public Widget asWidget() {
    return requestDataAccessWidget.asWidget();
  }
}
