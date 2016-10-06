package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.web.client.widget.lazyload.SupportsLazyLoadInterface;

import com.google.gwt.user.client.ui.IsWidget;

public interface EntityIdCellRendererView extends IsWidget, SupportsLazyLoadInterface {
	void setLinkText(String text);
	void setLinkHref(String href);
	void setIcon(IconType iconType);
	void showLoadingIcon();
	void showErrorIcon(String error);
	void hideAllIcons();
}
