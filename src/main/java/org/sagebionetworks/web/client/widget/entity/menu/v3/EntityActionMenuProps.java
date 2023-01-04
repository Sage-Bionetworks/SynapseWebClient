package org.sagebionetworks.web.client.widget.entity.menu.v3;

import java.util.Map;
import jsinterop.base.JsPropertyMap;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.ActionConfiguration;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.ActionConfigurationMap;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.EntityActionMenuDropdownMap;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.EntityActionMenuLayout;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.EntityActionMenuPropsJsInterop;

public class EntityActionMenuProps {

  private Map<Action, ActionConfiguration> actionConfiguration;
  private EntityActionMenuDropdownMap menuConfiguration;
  private EntityActionMenuLayout layout;

  public EntityActionMenuProps(
    Map<Action, ActionConfiguration> actionConfiguration,
    EntityActionMenuDropdownMap menuConfiguration,
    EntityActionMenuLayout layout
  ) {
    this.actionConfiguration = actionConfiguration;
    this.menuConfiguration = menuConfiguration;
    this.layout = layout;
  }

  public Map<Action, ActionConfiguration> getActionConfiguration() {
    return actionConfiguration;
  }

  public EntityActionMenuDropdownMap getMenuConfiguration() {
    return menuConfiguration;
  }

  public EntityActionMenuLayout getLayout() {
    return layout;
  }

  public EntityActionMenuPropsJsInterop toJsInterop() {
    JsPropertyMap<ActionConfiguration> actionConfigMap = new ActionConfigurationMap();
    this.actionConfiguration.keySet()
      .forEach(action -> {
        ActionConfiguration config = this.actionConfiguration.get(action);
        actionConfigMap.set(action.name(), config);
      });

    return EntityActionMenuPropsJsInterop.create(
      actionConfigMap,
      this.menuConfiguration,
      this.layout
    );
  }
}
