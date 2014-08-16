package org.sagebionetworks.web.client.widget.table.v2;

import org.sagebionetworks.repo.model.table.TableBundle;
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
	public static final String NO_COLUMNS_EDITABLE = "This table does not have any columns.  Select the Table Schema to add columns to the this table.";
	public static final String NO_COLUMNS_NOT_EDITABLE = "This table does not have any columns.";
	public static final long DEFAULT_PAGE_SIZE = 10L;

	private TableEntityWidgetView view;
	private PortalGinInjector ginInjector;
	
	String tableId;
	TableBundle  tableBundle;
	
	boolean canEdit;
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
	
	/**
	 * Configure this widget with new data.
	 * Calling this method will replace all widget state to the passed parameters.
	 * @param bundle
	 * @param canEdit
	 * @param queryString
	 * @param qch
	 */
	public void configure(EntityBundle bundle, boolean canEdit, QueryChangeHandler qch) {
		this.tableId = bundle.getEntity().getId();
		this.tableBundle = bundle.getTableBundle();
		this.canEdit = canEdit;
		this.queryChagneHandler = qch;
		this.view.configure(this.tableId, this.tableBundle.getColumnModels(), this.canEdit);
		checkState();
	}

	/**
	 * 
	 */
	public void checkState() {
		// If there are no columns, then the first thing to do is ask the user to create some columns.
		if(this.tableBundle.getColumnModels().size() < 1){
			String message = null;
			if(this.canEdit){
				message = NO_COLUMNS_EDITABLE;
			}else{
				message = NO_COLUMNS_NOT_EDITABLE;
			}
			// There can be no query when there are no columns
			if(this.queryChagneHandler.getQueryString() != null){
				this.queryChagneHandler.onQueryChange(null);
			}
			view.setQueryInputVisible(false);
			view.setQueryResultsVisible(false);
			view.showNoColumns(message);
		}
	}
	
	/**
	 * Build the default query based on the current table data.
	 * @return
	 */
	public String getDefaultQueryString(){
		long pageSize = getDefaultPageSize();
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT * FROM ");
		builder.append(this.tableId);
		builder.append(" LIMIT ").append(pageSize).append(" OFFSET 0");
		return builder.toString();
	}
	
	/**
	 * Get the default page size based on the current state of the table.
	 * @return
	 */
	public long getDefaultPageSize(){
		if(this.tableBundle.getMaxRowsPerPage() == null){
			return DEFAULT_PAGE_SIZE;
		}
		long maxRowsPerPage = this.tableBundle.getMaxRowsPerPage();
		long maxTwoThirds = maxRowsPerPage - maxRowsPerPage/3l;
		return Math.min(maxTwoThirds, DEFAULT_PAGE_SIZE);
	}
	
}
