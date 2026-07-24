package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.sagebionetworks.web.client.widget.PortalBannersWidget;

public class EntityMetadataViewImpl
  extends Composite
  implements EntityMetadataView {

  public interface EntityMetadataViewImplUiBinder
    extends UiBinder<Widget, EntityMetadataViewImpl> {}

  private final EntityMetadataViewImplUiBinder uiBinder = GWT.create(
    EntityMetadataViewImplUiBinder.class
  );

  @UiField
  HTMLPanel detailedMetadata;

  @UiField
  Div fileHistoryContainer;

  @UiField
  Div descriptionContainer;

  @UiField
  Paragraph descriptionText;

  @UiField
  SimplePanel entityModalWidgetContainer;

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
  public void setDescriptionVisible(boolean visible) {
    descriptionContainer.setVisible(visible);
  }

  @Override
  public void setVersionHistoryWidget(IsWidget fileHistoryWidget) {
    fileHistoryContainer.clear();
    fileHistoryContainer.add(fileHistoryWidget);
  }

  @Override
  public void clear() {}

  @Override
  public void setDetailedMetadataVisible(boolean visible) {
    detailedMetadata.setVisible(visible);
  }

  @Override
  public void setEntityId(String entityId) {
    portalBannersWidget.configure(entityId);
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
