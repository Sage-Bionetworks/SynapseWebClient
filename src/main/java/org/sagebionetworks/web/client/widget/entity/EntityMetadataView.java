package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;

public interface EntityMetadataView extends IsWidget {
  public interface Presenter {
    void toggleAnnotationsVisible();
  }

  public void setPresenter(Presenter presenter);

  public void setDetailedMetadataVisible(boolean visible);

  void setAnnotationsModalVisible(boolean visible);

  boolean getAnnotationsVisible();

  void setAnnotationsVisible(boolean visible);

  void setDescriptionVisible(boolean visible);

  void setVersionHistoryWidget(IsWidget fileHistoryWidget);

  public void setDoiWidget(IsWidget doiWidget);

  public void setAnnotationsRendererWidget(IsWidget annotationsWidget);

  void clear();

  public void setRestrictionPanelVisible(boolean visible);

  void setRestrictionWidgetV2(IsWidget restrictionWidget);

  void setEntityId(String text);

  void setUploadDestinationPanelVisible(boolean isVisible);

  void setUploadDestinationText(String text);

  void setRestrictionWidgetV2Visible(boolean visible);

  void setAnnotationsTitleText(String text);

  void setContainerItemCountWidget(IsWidget w);

  void setDescription(String description);

  void setVersionNumber(Long versionNumber);
}
