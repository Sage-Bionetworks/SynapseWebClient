package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.widget.PortalBannersWidget;
import org.sagebionetworks.web.client.widget.TextBoxWithCopyToClipboardWidget;

public class EntityMetadataViewImpl
  extends Composite
  implements EntityMetadataView {

  interface EntityMetadataViewImplUiBinder
    extends UiBinder<Widget, EntityMetadataViewImpl> {}

  private static final EntityMetadataViewImplUiBinder uiBinder = GWT.create(
    EntityMetadataViewImplUiBinder.class
  );

  @UiField
  HTMLPanel detailedMetadata;

  @UiField
  TextBoxWithCopyToClipboardWidget idField;

  @UiField
  Span doiPanel;

  @UiField
  Div fileHistoryContainer;

  @UiField
  Span uploadDestinationPanel;

  @UiField
  Span uploadDestinationField;

  @UiField
  Div descriptionContainer;

  @UiField
  Paragraph descriptionText;

  @UiField
  SimplePanel entityModalWidgetContainer;

  @UiField
  Span projectDataAvailabilityPanel;

  @UiField
  SimplePanel portalBannersContainer;

  PortalBannersWidget portalBannersWidget;

  @Inject
  public EntityMetadataViewImpl() {
    initWidget(uiBinder.createAndBindUi(this));
    this.portalBannersWidget = new PortalBannersWidget();
    portalBannersContainer.setWidget(portalBannersWidget);
  }

  @Override
  public void setDoiWidget(IsWidget doiWidget) {
    doiPanel.clear();
    doiPanel.add(doiWidget);
  }

  @Override
  public void setUploadDestinationPanelVisible(boolean isVisible) {
    uploadDestinationPanel.setVisible(isVisible);
  }

  @Override
  public void setUploadDestinationText(String text) {
    uploadDestinationField.setText(text);
  }

  @Override
  public void setDescriptionVisible(boolean visible) {
    descriptionContainer.setVisible(visible);
  }

  @Override
  public void setVersionHistoryWidget(IsWidget fileHistoryWidget) {
    fileHistoryContainer.clear();
    fileHistoryContainer.add(fileHistoryWidget);
  }

  @Override
  public void clear() {
    uploadDestinationField.setText("");
    uploadDestinationPanel.setVisible(false);
  }

  @Override
  public void setDetailedMetadataVisible(boolean visible) {
    detailedMetadata.setVisible(visible);
  }

  @Override
  public void setEntityId(String entityId) {
    idField.setText(entityId);
    portalBannersWidget.configure(entityId);
  }

  @Override
  public void setProjectDataAvailabilityWidget(IsWidget widget) {
    projectDataAvailabilityPanel.clear();
    projectDataAvailabilityPanel.add(widget);
  }

  @Override
  public void setEntityModalWidget(IsWidget entityModalWidget) {
    entityModalWidgetContainer.clear();
    entityModalWidgetContainer.add(entityModalWidget);
  }

  @Override
  public void setDescription(String description) {
    descriptionText.setText(description);
  }
}
