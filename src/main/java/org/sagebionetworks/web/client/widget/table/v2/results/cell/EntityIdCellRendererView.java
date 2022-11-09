package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.repo.model.EntityType;

public interface EntityIdCellRendererView extends IsWidget {
  void setLinkText(String text);

  void setEntityId(String entityId);

  void setClickHandler(ClickHandler clickHandler);

  void setEntityType(EntityType entityType);

  void showLoadingIcon();

  void showErrorIcon(String error);

  void hideAllIcons();

  void setVisible(boolean visible);
}
