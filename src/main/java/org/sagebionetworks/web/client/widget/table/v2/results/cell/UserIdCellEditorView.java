package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface UserIdCellEditorView extends IsWidget {
	void setSynapseSuggestBoxWidget(Widget w);

	void setUserIdCellRenderer(Widget w);

	void showEditor(boolean visible);

	void setUserIdCellRendererClickHandler(ClickHandler clickHandler);
}
