package org.sagebionetworks.web.client.widget.table.v2;

import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.model.EntityBundle;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * TableEntity widget provides viewing and editing of both a table's schema and
 * row data. It also allows a user to execute a query against the table by
 * writing SQL.
 * 
 * @author John
 * 
 */
public class TableEntityWidget implements IsWidget, TableEntityWidgetView.Presenter {

	private TableEntityWidgetView view;
	private EntityBundle tableBundle;
	private PortalGinInjector ginInjector;
	
	@Inject
	public TableEntityWidget(TableEntityWidgetView view, PortalGinInjector ginInjector){
		this.view = view;
		this.view.setPresenter(this);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void configure(EntityBundle tableBundle){
		this.tableBundle = tableBundle;
		
	}
	
}
