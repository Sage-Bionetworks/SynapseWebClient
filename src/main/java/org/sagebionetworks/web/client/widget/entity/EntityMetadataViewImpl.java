package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.widget.IconSvg;

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
  HTMLPanel dataUseContainer;

  @UiField
  TextBox idField;

  @UiField
  Span doiPanel;

  @UiField
  Span containerItemCountContainer;

  @UiField
  Span restrictionPanelV2;

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

  @Inject
  public EntityMetadataViewImpl() {
    initWidget(uiBinder.createAndBindUi(this));
    idField.addClickHandler(event -> idField.selectAll());
  }

  @Override
  public void setContainerItemCountWidget(IsWidget w) {
    containerItemCountContainer.clear();
    containerItemCountContainer.add(w);
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
    dataUseContainer.setVisible(false);
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
  }

  @Override
  public void setRestrictionPanelVisible(boolean visible) {
    dataUseContainer.setVisible(visible);
  }

  @Override
  public void setRestrictionWidgetV2(IsWidget restrictionWidget) {
    restrictionPanelV2.clear();
    restrictionPanelV2.add(restrictionWidget);
  }

  @Override
  public void setEntityModalWidget(IsWidget entityModalWidget) {
    entityModalWidgetContainer.clear();
    entityModalWidgetContainer.add(entityModalWidget);
  }

  @Override
  public void setRestrictionWidgetV2Visible(boolean visible) {
    restrictionPanelV2.setVisible(visible);
  }

  @Override
  public void setDescription(String description) {
    descriptionText.setText(description);
  }
}
