package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.VersionableEntity;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl;
import org.sagebionetworks.web.client.widget.entity.menu.v3.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenu;

public class EntityMetadata {

  private final EntityMetadataView view;
  private VersionHistoryWidget versionHistoryWidget;
  private final PortalGinInjector ginInjector;
  private final EntityModalWidget entityModalWidget;
  private boolean annotationsAreVisible = false;

  @Inject
  public EntityMetadata(
    EntityMetadataView view,
    PortalGinInjector ginInjector,
    EntityModalWidget entityModalWidget
  ) {
    this.view = view;
    this.ginInjector = ginInjector;
    this.entityModalWidget = entityModalWidget;
    this.view.setEntityModalWidget(entityModalWidget);
  }

  public Widget asWidget() {
    return view.asWidget();
  }

  public VersionHistoryWidget getVersionHistoryWidget() {
    if (versionHistoryWidget == null) {
      versionHistoryWidget = ginInjector.getVersionHistoryWidget();
      view.setVersionHistoryWidget(versionHistoryWidget);
    }
    return versionHistoryWidget;
  }

  public void configure(
    EntityBundle bundle,
    Long versionNumber,
    EntityActionMenu actionMenu
  ) {
    clear();
    // The "detailed metadata" is shown in the title bar React component for non-project entities.
    view.setDetailedMetadataVisible(bundle.getEntity() instanceof Project);
    Entity en = bundle.getEntity();
    view.setEntityId(en.getId());
    entityModalWidget.configure(
      en.getId(),
      versionNumber,
      () -> setAnnotationsVisible(false),
      "ANNOTATIONS",
      false
    );

    // See comments on SWC-5763
    // TL;DR: we plan to show the description at some point, but not until we implement new designs
    // view.setDescriptionVisible(bundle.getEntity() instanceof Table && en.getDescription() != null && DisplayUtils.isInTestWebsite(ginInjector.getCookieProvider()));
    view.setDescriptionVisible(false);
    view.setDescription(en.getDescription());

    setAnnotationsVisible(false);
    actionMenu.setActionListener(
      Action.SHOW_ANNOTATIONS,
      (action, e) -> setAnnotationsVisible(!annotationsAreVisible)
    );

    actionMenu.setActionListener(
      Action.SHOW_VERSION_HISTORY,
      (action, e) -> {
        getVersionHistoryWidget()
          .setVisible(!getVersionHistoryWidget().isVisible());
      }
    );

    if (EntityActionControllerImpl.isVersionSupported(bundle.getEntity())) {
      getVersionHistoryWidget()
        .setVisible(
          !((VersionableEntity) bundle.getEntity()).getIsLatestVersion()
        );
      getVersionHistoryWidget().setEntityBundle(bundle, versionNumber);
    } else {
      if (versionHistoryWidget != null) {
        versionHistoryWidget.setVisible(false);
      }
    }
  }

  public void setAnnotationsVisible(boolean visible) {
    annotationsAreVisible = visible;
    entityModalWidget.setOpen(visible);
  }

  public void clear() {
    view.clear();
  }
}
