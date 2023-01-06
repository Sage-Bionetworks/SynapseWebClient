package org.sagebionetworks.web.client.widget.entity.file;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.function.Consumer;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.VersionableEntity;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.jsinterop.EntityPageTitleBarProps;
import org.sagebionetworks.web.client.place.AccessRequirementsPlace;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenu;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenuProps;

public class BasicTitleBar implements SynapseWidgetPresenter {

  private BasicTitleBarView view;
  private final GlobalApplicationState globalAppState;

  private EntityPageTitleBarProps props;

  @Inject
  public BasicTitleBar(
    BasicTitleBarView view,
    GlobalApplicationState globalApplicationState
  ) {
    this.view = view;
    this.globalAppState = globalApplicationState;
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  public void configure(EntityBundle bundle, EntityActionMenu actionMenu) {
    if (bundle.getEntity() instanceof VersionableEntity) {
      this.props =
        EntityPageTitleBarProps.create(
          bundle.getEntity().getId(),
          ((VersionableEntity) bundle.getEntity()).getVersionNumber()
        );
    } else {
      this.props = EntityPageTitleBarProps.create(bundle.getEntity().getId());
    }
    addActClickhandler(bundle.getEntity().getId());
    setActionMenu(actionMenu);
    this.view.setProps(this.props);
  }

  private void addActClickhandler(String entityId) {
    this.props.setOnActMemberClickAddConditionsForUse(() -> {
        // go to access requirements place where they can modify access requirements
        AccessRequirementsPlace place = new AccessRequirementsPlace("");
        place.putParam(AccessRequirementsPlace.ID_PARAM, entityId);
        place.putParam(
          AccessRequirementsPlace.TYPE_PARAM,
          RestrictableObjectType.ENTITY.toString()
        );
        globalAppState.getPlaceChanger().goTo(place);
      });
  }

  private void setActionMenu(EntityActionMenu actionMenu) {
    this.props.setEntityActionMenuProps(actionMenu.getProps());

    // An action menu will be rendered in the title bar by React, so remove it from the parent
    actionMenu.asWidget().removeFromParent();

    // Register a listener so the menu will update when the props change
    Consumer<EntityActionMenuProps> onActionMenuPropsChange = actionMenuProps -> {
      this.props.setEntityActionMenuProps(actionMenuProps);
      this.view.setProps(this.props);
    };
    actionMenu.setPropUpdateListener(onActionMenuPropsChange);
  }
}
