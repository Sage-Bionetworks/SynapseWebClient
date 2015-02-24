package org.sagebionetworks.web.client.widget.table.v2.results;

import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TableQueryResultWikiWidgetViewImpl implements TableQueryResultWikiWidgetView {
	
	public interface Binder extends UiBinder<Widget, TableQueryResultWikiWidgetViewImpl> {}
	Widget widget;
	@UiField
	Div tableWidgetContainer;
	
	@Inject
	public TableQueryResultWikiWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
	}
	
	@Override
	public void setTableQueryResultWidget(Widget tableQueryResultWidget) {
		tableWidgetContainer.clear();
		tableWidgetContainer.add(tableQueryResultWidget);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
}
