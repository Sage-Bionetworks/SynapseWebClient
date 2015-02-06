package org.sagebionetworks.web.client.widget.table.v2.results;

import org.gwtbootstrap3.client.ui.Tooltip;

import com.google.gwt.user.client.ui.Widget;

public class TableQueryResultWikiWidgetViewImpl implements TableQueryResultWikiWidgetView {
	Widget tableQueryResultWidget;
	Tooltip tip;
	
	@Override
	public void setTableQueryResultWidget(Widget tableQueryResultWidget) {
		this.tableQueryResultWidget = tableQueryResultWidget;
		tip = new Tooltip(tableQueryResultWidget);
	}
	@Override
	public void setTooltip(String tooltip) {
		tip.setText(tooltip);
	}
	
	@Override
	public Widget asWidget() {
		return tip.asWidget();
	}
}
