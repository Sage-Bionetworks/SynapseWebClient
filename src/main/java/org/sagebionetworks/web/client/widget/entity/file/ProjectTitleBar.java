package org.sagebionetworks.web.client.widget.entity.file;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.ProjectVisibilityChip;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;

public class ProjectTitleBar implements SynapseWidgetPresenter {

  private ProjectTitleBarView view;
  private AuthenticationController authenticationController;
  private FavoriteWidget favWidget;
  private ProjectVisibilityChip visibilityChip;

  @Inject
  public ProjectTitleBar(
    ProjectTitleBarView view,
    AuthenticationController authenticationController,
    FavoriteWidget favWidget,
    ProjectVisibilityChip visibilityChip
  ) {
    this.view = view;
    this.authenticationController = authenticationController;
    this.favWidget = favWidget;
    this.visibilityChip = visibilityChip;
    view.setVisibilityWidget(visibilityChip.asWidget());
    view.setFavoritesWidget(favWidget.asWidget());
  }

  public void configure(EntityBundle bundle) {
    favWidget.configure(bundle.getEntity().getId());
    view.setFavoritesWidgetVisible(authenticationController.isLoggedIn());
    view.setTitle(bundle.getEntity().getName());
    visibilityChip.configure(bundle.getEntity().getId());
    if (!(bundle.getEntity() instanceof Project)) {
      view.setEntityType(EntityTypeUtils.getEntityType(bundle.getEntity()));
    } else {
      view.setEntityType(null);
    }
  }

  public void configure(EntityHeader entityHeader) {
    favWidget.configure(entityHeader.getId());
    visibilityChip.configure(entityHeader.getId());
    view.setFavoritesWidgetVisible(authenticationController.isLoggedIn());
    view.setTitle(entityHeader.getName());
    if (!(Project.class.getName().equals(entityHeader.getType()))) {
      view.setEntityType(EntityTypeUtils.getEntityType(entityHeader));
    } else {
      view.setEntityType(null);
    }
  }

  public void clearState() {
    view.clear();
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  public void setActionMenu(IsWidget w) {
    view.setActionMenu(w);
  }
}
