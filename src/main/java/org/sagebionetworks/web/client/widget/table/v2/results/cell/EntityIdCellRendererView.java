package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.constants.IconType;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;

public interface EntityIdCellRendererView extends IsWidget {
	void setLinkText(String text);

	void setEntityId(String entityId);

	void setClickHandler(ClickHandler clickHandler);

	void setIcon(IconType iconType);

	void showLoadingIcon();

	void showErrorIcon(String error);

	void hideAllIcons();

	void setVisible(boolean visible);
}
