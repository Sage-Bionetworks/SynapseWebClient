package org.sagebionetworks.web.client.widget.entity.file;

import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.web.client.SynapseView;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface BasicTitleBarView extends IsWidget, SynapseView {

	void setFavoritesWidget(Widget favoritesWidget);

	void setFavoritesWidgetVisible(boolean visible);

	void setTitle(String name);

	void setEntityType(EntityType entityType);

	void setActionMenu(IsWidget w);
}
