package org.sagebionetworks.web.client.widget.entity.menu.v3;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.SkeletonButtonProps;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.EntityActionMenuPropsJsInterop;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class EntityActionMenuViewImpl implements EntityActionMenuView {

  private final SynapseReactClientFullContextPropsProvider propsProvider;

  private final FlowPanel panel = new FlowPanel();
  private final ReactComponent menuComponent = new ReactComponent();
  private final ReactComponent loaderComponent = new ReactComponent();
  private final SimplePanel controllerWidgetContainer = new SimplePanel();

  @Inject
  private EntityActionMenuViewImpl(
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    this.propsProvider = propsProvider;

    renderLoaderComponent();

    panel.add(menuComponent);
    panel.add(loaderComponent);
    panel.add(controllerWidgetContainer);
  }

  @Override
  public void configure(EntityActionMenuProps props) {
    renderMenuComponent(props.toJsInterop());
  }

  @Override
  public void addControllerWidget(IsWidget w) {
    this.controllerWidgetContainer.add(w);
  }

  @Override
  public void setIsLoading(boolean isLoading) {
    loaderComponent.setVisible(isLoading);
    menuComponent.setVisible(!isLoading);
  }

  private void renderMenuComponent(EntityActionMenuPropsJsInterop props) {
    ReactElement node = React.createElementWithSynapseContext(
      SRC.SynapseComponents.EntityActionMenu,
      props,
      propsProvider.getJsInteropContextProps()
    );
    menuComponent.render(node);
  }

  private void renderLoaderComponent() {
    ReactElement node = React.createElementWithSynapseContext(
      SRC.SynapseComponents.SkeletonButton,
      SkeletonButtonProps.create("Tools Menu Placeholder"),
      propsProvider.getJsInteropContextProps()
    );
    loaderComponent.render(node);
  }

  @Override
  public Widget asWidget() {
    return panel.asWidget();
  }
}
