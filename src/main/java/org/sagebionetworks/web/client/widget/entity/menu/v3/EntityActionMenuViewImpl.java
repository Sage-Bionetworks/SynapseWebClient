package org.sagebionetworks.web.client.widget.entity.menu.v3;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.Map;
import jsinterop.base.JsPropertyMap;
import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.SkeletonButtonProps;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.ActionConfiguration;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.ActionConfigurationMap;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.EntityActionMenuDropdownMap;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.EntityActionMenuLayout;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.EntityActionMenuProps;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class EntityActionMenuViewImpl implements EntityActionMenuView {

  private Presenter presenter;
  private final SynapseContextPropsProvider propsProvider;
  private Map<Action, ActionConfiguration> actionConfigurationMap;
  private EntityActionMenuDropdownMap menuConfiguration;
  private EntityActionMenuLayout layout;

  private final FlowPanel panel = new FlowPanel();
  private final ReactComponentDiv menuComponent = new ReactComponentDiv();
  private final ReactComponentDiv loaderComponent = new ReactComponentDiv();
  private final SimplePanel controllerWidgetContainer = new SimplePanel();

  @Inject
  private EntityActionMenuViewImpl(SynapseContextPropsProvider propsProvider) {
    this.propsProvider = propsProvider;

    renderLoaderComponent();

    panel.add(menuComponent);
    panel.add(loaderComponent);
    panel.add(controllerWidgetContainer);
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void configure(
    Map<Action, ActionConfiguration> actionConfiguration,
    EntityActionMenuDropdownMap menuConfiguration,
    EntityActionMenuLayout layout
  ) {
    this.actionConfigurationMap = actionConfiguration;
    this.menuConfiguration = menuConfiguration;
    this.layout = layout;
    renderMenuComponent();
  }

  @Override
  public void setLayout(EntityActionMenuLayout layout) {
    this.layout = layout;
    renderMenuComponent();
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

  private void renderMenuComponent() {
    ReactNode node = React.createElementWithSynapseContext(
      SRC.SynapseComponents.EntityActionMenu,
      getProps(),
      propsProvider.getJsInteropContextProps()
    );
    menuComponent.render(node);
  }

  private void renderLoaderComponent() {
    ReactNode node = React.createElementWithSynapseContext(
      SRC.SynapseComponents.SkeletonButton,
      SkeletonButtonProps.create("Tools Menu Placeholder"),
      propsProvider.getJsInteropContextProps()
    );
    loaderComponent.render(node);
  }

  private EntityActionMenuProps getProps() {
    JsPropertyMap<ActionConfiguration> actionConfigMap = new ActionConfigurationMap();
    this.actionConfigurationMap.keySet()
      .forEach(action -> {
        ActionConfiguration config = this.actionConfigurationMap.get(action);
        actionConfigMap.set(action.name(), config);
      });

    return EntityActionMenuProps.create(
      actionConfigMap,
      menuConfiguration,
      layout
    );
  }

  @Override
  public Widget asWidget() {
    return panel.asWidget();
  }
}
