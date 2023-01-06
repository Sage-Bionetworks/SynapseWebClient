package org.sagebionetworks.web.client.widget.entity.file;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.EntityTypeIcon;

public class ProjectTitleBarViewImpl implements ProjectTitleBarView {

  @UiField
  Heading fileName;

  @UiField
  SimplePanel favoritePanel;

  @UiField
  EntityTypeIcon entityIcon;

  @UiField
  Div actionMenuContainer;

  interface BasicTitleBarViewImplUiBinder
    extends UiBinder<Widget, ProjectTitleBarViewImpl> {}

  private static BasicTitleBarViewImplUiBinder uiBinder = GWT.create(
    BasicTitleBarViewImplUiBinder.class
  );
  Widget widget;

  @Inject
  public ProjectTitleBarViewImpl() {
    widget = uiBinder.createAndBindUi(this);
  }

  public void setFavoritesWidget(Widget favoritesWidget) {
    favoritePanel.addStyleName("inline-block");
    favoritePanel.setWidget(favoritesWidget);
  }

  @Override
  public void setFavoritesWidgetVisible(boolean visible) {
    favoritePanel.setVisible(visible);
  }

  @Override
  public void setTitle(String name) {
    fileName.setText(name);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setEntityType(EntityType entityType) {
    if (entityType == null) {
      entityIcon.setVisible(false);
    } else {
      entityIcon.setVisible(true);
      entityIcon.setType(entityType);
    }
  }

  @Override
  public void showErrorMessage(String message) {
    DisplayUtils.showErrorMessage(message);
  }

  @Override
  public void showLoading() {}

  @Override
  public void showInfo(String message) {
    DisplayUtils.showInfo(message);
  }

  @Override
  public void setActionMenu(IsWidget w) {
    w.asWidget().removeFromParent();
    actionMenuContainer.clear();
    actionMenuContainer.add(w);
  }

  @Override
  public void clear() {}
}
