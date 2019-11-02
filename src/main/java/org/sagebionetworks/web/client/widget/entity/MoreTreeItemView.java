package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Button;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsTreeItem;
import com.google.gwt.user.client.ui.IsWidget;

public interface MoreTreeItemView extends IsWidget, IsTreeItem {
	public interface Presenter extends IsWidget, IsTreeItem {

	}

	void setButtonClickHandler(ClickHandler handler);

	void setVisible(boolean b);

	Button asButton();
}
