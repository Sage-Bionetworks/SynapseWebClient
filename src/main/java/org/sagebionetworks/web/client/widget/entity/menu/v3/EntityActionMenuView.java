package org.sagebionetworks.web.client.widget.entity.menu.v3;

import com.google.gwt.user.client.ui.IsWidget;
import java.util.Map;
import org.sagebionetworks.web.client.jsinterop.ReactMouseEvent;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.ActionConfiguration;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.EntityActionMenuDropdownMap;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.EntityActionMenuLayout;

public interface EntityActionMenuView extends IsWidget {
  void setPresenter(Presenter presenter);

  void configure(
    Map<Action, ActionConfiguration> actionConfiguration,
    EntityActionMenuDropdownMap menuConfiguration,
    EntityActionMenuLayout layout
  );

  void setLayout(EntityActionMenuLayout layout);

  void addControllerWidget(IsWidget w);

  void setIsLoading(boolean isLoading);

  interface Presenter {
    void onAction(Action action, ReactMouseEvent mouseEvent);
  }
}
