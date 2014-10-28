package org.sagebionetworks.web.client.widget.table.v2;

import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.TableBundle;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget.ActionListener;
import org.sagebionetworks.web.client.widget.table.QueryChangeHandler;
import org.sagebionetworks.web.client.widget.table.modal.download.DownloadTableQueryModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadTableModalWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryInputListener;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultsListner;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWidget;

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
public class TableEntityWidget implements IsWidget, TableEntityWidgetView.Presenter, QueryResultsListner, QueryInputListener, ActionListener {
	
	private static final Action[] ACTIONS = new Action[]{Action.TABLE_SCHEMA, Action.UPLOAD_TABLE_DATA, Action.EDIT_TABLE_DATA, Action.DOWNLOAD_TABLE_QUERY_RESULTS};
	
	public static final long DEFAULT_OFFSET = 0L;
	public static final String SELECT_FROM = "SELECT * FROM ";
	public static final String NO_COLUMNS_EDITABLE = "This table does not have any columns.  Select the Table Schema to add columns to the this table.";
	public static final String NO_COLUMNS_NOT_EDITABLE = "This table does not have any columns.";
	public static final long DEFAULT_LIMIT = 10L;
	
	DownloadTableQueryModalWidget downloadTableQueryModalWidget;
	UploadTableModalWidget uploadTableModalWidget;
	TableEntityWidgetView view;
	ActionMenuWidget actionMenu;
	
	String tableId;
	TableBundle  tableBundle;
	boolean canEdit;
	QueryChangeHandler queryChangeHandler;
	TableQueryResultWidget queryResultsWidget;
	QueryInputWidget queryInputWidget;
	Query currentQuery;
	
	@Inject
	public TableEntityWidget(TableEntityWidgetView view, TableQueryResultWidget queryResultsWidget, QueryInputWidget queryInputWidget, DownloadTableQueryModalWidget downloadTableQueryModalWidget,
			UploadTableModalWidget uploadTableModalWidget){
		this.view = view;
		this.downloadTableQueryModalWidget = downloadTableQueryModalWidget;
		this.uploadTableModalWidget = uploadTableModalWidget;
		this.queryResultsWidget = queryResultsWidget;
		this.queryInputWidget = queryInputWidget;
		this.view.setPresenter(this);
		this.view.setQueryResultsWidget(this.queryResultsWidget);
		this.view.setQueryInputWidget(this.queryInputWidget);
		this.view.setDownloadTableQueryModalWidget(this.downloadTableQueryModalWidget);
		this.view.setUploadTableModalWidget(this.uploadTableModalWidget);
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
	public void configure(EntityBundle bundle, boolean canEdit, QueryChangeHandler qch, ActionMenuWidget actionMenu) {
		this.tableId = bundle.getEntity().getId();
		this.tableBundle = bundle.getTableBundle();
		this.canEdit = canEdit;
		this.queryChangeHandler = qch;
		this.view.configure(bundle, this.canEdit);
		this.actionMenu = actionMenu;
		configureActions();
		checkState();
	}

	private void configureActions(){
		for(Action action: ACTIONS){
			this.actionMenu.addActionListener(action, this);
			this.actionMenu.setActionVisible(action, true);
		}
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
			Query startQuery = queryChangeHandler.getQueryString();
			if(startQuery == null){
				// use a default query
				startQuery = getDefaultQuery();
			}
			setQuery(startQuery);
		}
	}
	
	/**
	 * Set the query used by this widget.
	 * @param sql
	 */
	private void setQuery(Query query){
		this.currentQuery = query;
		this.queryInputWidget.configure(query.getSql(), this, this.canEdit);
		this.view.setQueryResultsVisible(true);
		this.view.setTableMessageVisible(false);
		this.queryResultsWidget.configure(query, this.canEdit, this);
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
	}
	
	/**
	 * Build the default query based on the current table data.
	 * @return
	 */
	public Query getDefaultQuery(){
		StringBuilder builder = new StringBuilder();
		builder.append(SELECT_FROM);
		builder.append(this.tableId);
		Query query = new Query();
		query.setSql(builder.toString());
		query.setOffset(DEFAULT_OFFSET);
		query.setLimit(DEFAULT_LIMIT);
		query.setIsConsistent(true);
		return query;
	}
	
	/**
	 * Get the default page size based on the current state of the table.
	 * @return
	 */
	public long getDefaultPageSize(){
		if(this.tableBundle.getMaxRowsPerPage() == null){
			return DEFAULT_LIMIT;
		}
		long maxRowsPerPage = this.tableBundle.getMaxRowsPerPage();
		long maxTwoThirds = maxRowsPerPage - maxRowsPerPage/3l;
		return Math.min(maxTwoThirds, DEFAULT_LIMIT);
	}

	@Override
	public void onPersistSuccess(EntityUpdatedEvent event) {
		this.queryChangeHandler.onPersistSuccess(event);
	}

	@Override
	public void queryExecutionStarted() {
		// Pass this along to the input widget.
		this.queryInputWidget.queryExecutionStarted();
	}

	@Override
	public void queryExecutionFinished(boolean wasSuccessful) {
		// Pass this along to the input widget.
		this.queryInputWidget.queryExecutionFinished(wasSuccessful);
		// Set this as the query if it was successful
		if(wasSuccessful){
			this.queryChangeHandler.onQueryChange(this.currentQuery);
		}
	}

	/**
	 * Called when the user executes a new query from the query input box.
	 * When the SQL changes reset back to the first page.
	 */
	@Override
	public void onExecuteQuery(String sql) {
		this.currentQuery.setSql(sql);
		this.currentQuery.setLimit(DEFAULT_LIMIT);
		this.currentQuery.setOffset(DEFAULT_OFFSET);
		setQuery(this.currentQuery);
	}

	@Override
	public void onPageChange(Long newOffset) {
		this.currentQuery.setOffset(newOffset);
		setQuery(this.currentQuery);
	}

	@Override
	public void onEditResults() {
		queryResultsWidget.onEditRows();
	}

	@Override
	public void onDownloadResults(String sql) {
		this.downloadTableQueryModalWidget.configure(sql);
		downloadTableQueryModalWidget.showModal();
	}

	@Override
	public void onAction(Action action) {
		view.setTableMessageVisible(true);
		view.showTableMessage(AlertType.DANGER, "Action clicked: "+action);
	}
	
}
