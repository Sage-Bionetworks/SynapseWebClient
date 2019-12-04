package org.sagebionetworks.web.client.widget.table.v2;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryExecutionListener;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryInputListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * This widget provides a text box for query input and a button to execute a query.
 * 
 * @author John
 *
 */
public class QueryInputWidget implements QueryInputView.Presenter, IsWidget, QueryExecutionListener {

	public static final String AN_EMPTY_QUERY_IS_NOT_VALID = "An empty query is not valid.";
	QueryInputView view;
	SynapseClientAsync synapseClient;
	QueryInputListener queryInputListener;
	String startQuery;

	@Inject
	public QueryInputWidget(QueryInputView view, SynapseClientAsync synapseClient) {
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.view.setPresenter(this);
	}

	/**
	 * Configure this widget.
	 * 
	 * @param startQuery
	 * @param queryInputListener
	 */
	public void configure(String startQuery, QueryInputListener queryInputListener, boolean isEditable) {
		this.startQuery = startQuery;
		this.queryInputListener = queryInputListener;
		this.view.setEditVisible(isEditable);
		onReset();
	}

	@Override
	public Widget asWidget() {
		return this.view.asWidget();
	}

	@Override
	public void onExecuteQuery() {
		// Get the query from the view
		view.setQueryInputLoading(true);
		String sql = view.getInputQueryString();
		validateAndSendQuery(sql);
	}

	/**
	 * Validate the given query.
	 * 
	 * @param sql
	 */
	private void validateAndSendQuery(final String sql) {
		if (sql == null || "".equals(sql.trim())) {
			view.showInputError(true);
			view.setInputErrorMessage(AN_EMPTY_QUERY_IS_NOT_VALID);
		} else {
			// validate the query
			synapseClient.validateTableQuery(sql, new AsyncCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					setQuery(sql);
				}

				@Override
				public void onFailure(Throwable caught) {
					setFailed(caught);
				}

			});
		}
	}

	/**
	 * Set a valid or modified query
	 * 
	 * @param sql
	 */
	private void setQuery(String sql) {
		view.showInputError(false);
		queryInputListener.onExecuteQuery(sql);
	}

	/**
	 * Service failure
	 * 
	 * @param caught
	 */
	private void setFailed(Throwable caught) {
		view.setQueryInputLoading(false);
		view.showInputError(true);
		view.setInputErrorMessage(caught.getMessage());
	}

	@Override
	public void queryExecutionStarted() {
		view.setQueryInputLoading(true);
		view.setEditEnabled(false);
		view.setDownloadEnabled(false);
	}

	@Override
	public void queryExecutionFinished(boolean wasSuccessful, boolean resultsEditable) {
		view.setQueryInputLoading(false);
		view.setEditEnabled(wasSuccessful && resultsEditable);
		view.setDownloadEnabled(wasSuccessful);
	}

	@Override
	public void onReset() {
		view.setInputQueryString(startQuery);
		view.showInputError(false);
		view.setQueryInputLoading(false);
		view.setEditEnabled(false);
		view.setDownloadEnabled(false);
	}

	@Override
	public void onEditResults() {
		queryInputListener.onEditResults();
	}

	@Override
	public void onExportTable() {
		queryInputListener.onDownloadResults();
	}

	public String getInputSQL() {
		return view.getInputQueryString();
	}

	@Override
	public void onShowQuery() {
		queryInputListener.onShowQuery();
	}

	public void setQueryInputVisible(boolean visible) {
		view.setQueryInputVisible(visible);
	}

	public void setShowQueryVisible(boolean visible) {
		view.setShowQueryVisible(visible);
	}

	public void setDownloadFilesVisible(boolean visible) {
		view.setDownloadFilesVisible(visible);
	}

	@Override
	public void onDownloadFilesProgrammatically() {
		queryInputListener.onShowDownloadFilesProgrammatically();
	}

	@Override
	public void onAddToDownloadList() {
		queryInputListener.onAddToDownloadList();
	}

	public void setVisible(boolean visible) {
		view.asWidget().setVisible(visible);
	}
}
