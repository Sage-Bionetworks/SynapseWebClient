package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.web.client.widget.SelectableListItem;
import org.sagebionetworks.web.client.widget.lazyload.SupportsLazyLoadInterface;

public interface EntityListRowBadgeView
  extends IsWidget, SelectableListItem, SupportsLazyLoadInterface {
  /**
   * Set the presenter.
   *
   * @param presenter
   */
  void setPresenter(Presenter presenter);

  void setEntityLink(String name, String url);

  void setCreatedOn(String modifiedOnString);

  void setCreatedByWidget(Widget w);

  void setEntityType(EntityType entityType);

  void showAddToDownloadList();

  void setNote(String note);

  String getNote();

  void setDescription(String description);

  void setDescriptionVisible(boolean visible);

  void setVersion(String version);

  void setIsSelectable(boolean isSelectable);

  void showErrorIcon(String reason);

  void showRow();

  void showLoading();

  /**
   * Presenter interface
   */
  public interface Presenter extends SelectableListItem.Presenter {
    void onAddToDownloadList();
  }
}
