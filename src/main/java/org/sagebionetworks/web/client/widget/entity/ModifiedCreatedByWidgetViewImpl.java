package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.CreatedByModifiedByProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.ReferenceJsObject;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.HelpWidget;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class ModifiedCreatedByWidgetViewImpl
  implements ModifiedCreatedByWidgetView {

  @UiField
  ReactComponentDiv container;

  public interface ModifiedCreatedByWidgetViewImplUiBinder
    extends UiBinder<Widget, ModifiedCreatedByWidgetViewImpl> {}

  private Widget widget;

  private final SynapseContextPropsProvider propsProvider;

  @Inject
  public ModifiedCreatedByWidgetViewImpl(
    ModifiedCreatedByWidgetViewImplUiBinder binder,
    SynapseContextPropsProvider propsProvider
  ) {
    widget = binder.createAndBindUi(this);
    this.propsProvider = propsProvider;
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setProps(CreatedByModifiedByProps props) {
    ReactNode component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.CreatedByModifiedBy,
      props,
      propsProvider.getJsInteropContextProps()
    );
    container.render(component);
  }

  @Override
  public void setVisible(boolean isVisible) {
    container.setVisible(isVisible);
  }
}
