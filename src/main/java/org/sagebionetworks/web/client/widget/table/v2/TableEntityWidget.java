package org.sagebionetworks.web.client.widget.table.v2;

import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.table.QueryChangeHandler;

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
	
	public static final long DEFAULT_PAGE_SIZE = 10L;

	private TableEntityWidgetView view;
	private PortalGinInjector ginInjector;
	
	EntityBundle tableBundle;
	boolean canEdit;
	String queryString;
	QueryChangeHandler queryChagneHandler;
	
	@Inject
	public TableEntityWidget(TableEntityWidgetView view, PortalGinInjector ginInjector){
		this.view = view;
		this.ginInjector = ginInjector;
		this.view.setPresenter(this);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void configure(EntityBundle bundle, boolean canEdit,
			String queryString, QueryChangeHandler qch) {
		this.tableBundle = bundle;
		this.canEdit = canEdit;
		this.queryString = queryString;
		this.queryChagneHandler = qch;
		if(this.queryString == null){
			this.queryString = getDefaultQueryString();
			qch.onQueryChange(this.queryString);
		}
		this.view.configure(this.tableBundle.getEntity().getId(), this.tableBundle.getTableBundle(), this.canEdit, this.queryString);
	}
	
	/**
	 * Build the default query based on the current table data.
	 * @return
	 */
	public String getDefaultQueryString(){
		long pageSize = getDefaultPageSize();
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT * FROM ");
		builder.append(this.tableBundle.getEntity().getId());
		builder.append(" LIMIT ").append(pageSize).append(" OFFSET 0");
		return builder.toString();
	}
	
	/**
	 * Get the default page size based on the current state of the table.
	 * @return
	 */
	public long getDefaultPageSize(){
		if(this.tableBundle.getTableBundle().getMaxRowsPerPage() == null){
			return DEFAULT_PAGE_SIZE;
		}
		long maxRowsPerPage = this.tableBundle.getTableBundle().getMaxRowsPerPage();
		long maxTwoThirds = maxRowsPerPage - maxRowsPerPage/3l;
		return Math.min(maxTwoThirds, DEFAULT_PAGE_SIZE);
	}
	
}
