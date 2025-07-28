package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.Widget;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.web.client.jsinterop.AccessRequirementListProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.SxProps;
import org.sagebionetworks.web.client.jsinterop.mui.Container;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class AccessRequirementsSRCViewImpl
  implements AccessRequirementsSRCView {

  Container container;
  ReactComponent requestDataAccessWidget;
  RestrictableObjectDescriptor subject;

  public AccessRequirementsSRCViewImpl() {
    container = new Container();
    SxProps sx = new SxProps();
    sx.pb = "40px";
    container.setSx(sx);
    container.setMaxWidth("lg");
  }

  @Override
  public void configure(RestrictableObjectDescriptor subject) {
    this.subject = subject;
    rerender();
  }

  private void rerender() {
    container.clear();
    requestDataAccessWidget = new ReactComponent();
    container.add(requestDataAccessWidget);
    AccessRequirementListProps props = AccessRequirementListProps.create(
      () -> {
        rerender();
      },
      null,
      subject.getId(),
      subject.getType(),
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
    return container.asWidget();
  }
}
