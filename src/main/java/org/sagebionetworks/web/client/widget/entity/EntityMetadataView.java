package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;

public interface EntityMetadataView extends IsWidget {
  public void setDetailedMetadataVisible(boolean visible);

  void setDescriptionVisible(boolean visible);

  void setVersionHistoryWidget(IsWidget fileHistoryWidget);

  public void setDoiWidget(IsWidget doiWidget);

  void clear();

  public void setRestrictionPanelVisible(boolean visible);

  void setRestrictionWidgetV2(IsWidget restrictionWidget);

  void setEntityModalWidget(IsWidget widget);

  void setEntityId(String text);

  void setUploadDestinationPanelVisible(boolean isVisible);

  void setUploadDestinationText(String text);

  void setRestrictionWidgetV2Visible(boolean visible);

  void setContainerItemCountWidget(IsWidget w);

  void setDescription(String description);
}
