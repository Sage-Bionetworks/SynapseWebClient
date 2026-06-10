package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.CreatedByModifiedByProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class ModifiedCreatedByWidgetViewImpl
  implements ModifiedCreatedByWidgetView {

  @UiField
  ReactComponent container;

  public interface ModifiedCreatedByWidgetViewImplUiBinder
    extends UiBinder<Widget, ModifiedCreatedByWidgetViewImpl> {}

  private Widget widget;

  private final ModifiedCreatedByWidgetViewImplUiBinder binder = GWT.create(
    ModifiedCreatedByWidgetViewImplUiBinder.class
  );

  @Inject
  public ModifiedCreatedByWidgetViewImpl() {
    widget = binder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setProps(CreatedByModifiedByProps props) {
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.CreatedByModifiedBy,
      props
    );
    container.render(component);
  }

  @Override
  public void setVisible(boolean isVisible) {
    container.setVisible(isVisible);
  }
}
