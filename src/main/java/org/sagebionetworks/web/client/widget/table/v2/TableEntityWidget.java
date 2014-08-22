package org.sagebionetworks.web.client.widget.table.v2;

import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;
import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.repo.model.table.AsynchDownloadFromTableRequestBody;
import org.sagebionetworks.repo.model.table.TableBundle;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
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
	private TableModelUtils tableModelUtils;
	private AsynchronousProgressWidget asynchProgressWidget;
	
	String tableId;
	TableBundle  tableBundle;
	boolean canEdit;
	QueryChangeHandler queryChangeHandler;
	
	@Inject
	public TableEntityWidget(TableEntityWidgetView view, AsynchronousProgressWidget asynchProgressWidget, TableModelUtils tableModelUtils){
		this.view = view;
		this.tableModelUtils = tableModelUtils;
		this.asynchProgressWidget = asynchProgressWidget;
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
		this.queryChangeHandler = qch;
		this.view.configure(bundle, this.canEdit);
		this.view.setProgressWidget(this.asynchProgressWidget);
		checkState();
	}

	/**
	 * 
	 */
	public void checkState() {
		// If there are no columns, then the first thing to do is ask the user to create some columns.
		if(this.tableBundle.getColumnModels().size() < 1){
			setNoColumnsState();
		}else{
			// There are columns.
			String startQuery = queryChangeHandler.getQueryString();;
			if(startQuery == null){
				// use a default query
				startQuery = getDefaultQueryString();
			}
			// Execute the query
			view.setInputQueryString(startQuery);
			view.setQueryInputVisible(true);
			view.setQueryInputLoading(true);
			
			// start the job
			AsynchDownloadFromTableRequestBody body = new AsynchDownloadFromTableRequestBody();
			body.setSql(startQuery);
			waitForQueryResults(body);
		}
	}
	
	private void waitForQueryResults(AsynchronousRequestBody status){
		view.setQueryProgressVisible(true);
		this.asynchProgressWidget.configure("Executing query...", status, new AsynchronousProgressHandler() {
			
			@Override
			public void onStatusCheckFailure(Throwable failure) {
				setQueryFailed(failure.getMessage());
			}
			
			@Override
			public void onComplete(AsynchronousJobStatus status) {
				view.showTableMessage(AlertType.INFO, "Query complete");
			}
			
			@Override
			public void onCancel(AsynchronousJobStatus status) {
				view.showTableMessage(AlertType.WARNING, "Query canceled");
			}
		});
	}
	
	private void setQueryFailed(String message){
		// Set the message
		view.setQueryMessage(AlertType.DANGER, message);
		view.setQueryResultsMessageVisible(true);
		view.setQueryInputLoading(false);
	}
	

	/**
	 * Set the view to show no columns message.
	 */
	private void setNoColumnsState() {
		String message = null;
		if(this.canEdit){
			message = NO_COLUMNS_EDITABLE;
		}else{
			message = NO_COLUMNS_NOT_EDITABLE;
		}
		// There can be no query when there are no columns
		if(this.queryChangeHandler.getQueryString() != null){
			this.queryChangeHandler.onQueryChange(null);
		}
		view.setQueryInputVisible(false);
		view.setQueryResultsVisible(false);
		view.showTableMessage(AlertType.INFO, message);
		view.setTableMessageVisible(true);
		view.setQueryResultsMessageVisible(false);
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

	@Override
	public void onPersistSuccess(EntityUpdatedEvent event) {
		this.queryChangeHandler.onPersistSuccess(event);
	}
	
}
