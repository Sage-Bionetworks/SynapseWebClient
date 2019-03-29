package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.AppendableRowSetRequest;
import org.sagebionetworks.repo.model.table.EntityUpdateResult;
import org.sagebionetworks.repo.model.table.EntityUpdateResults;
import org.sagebionetworks.repo.model.table.PartialRow;
import org.sagebionetworks.repo.model.table.PartialRowSet;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.TableUpdateRequest;
import org.sagebionetworks.repo.model.table.TableUpdateResponse;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionRequest;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionResponse;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.shared.asynch.AsynchType;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * This widget wraps a TablePageWidget and provides edit functionality.
 * 
 * @author John
 * 
 */
public class QueryResultEditorWidget implements
		QueryResultEditorView.Presenter, IsWidget, RowSelectionListener {

	public static final String VIEW_RECENTLY_CHANGED_KEY = "_view_recently_changed_etag";
	public static final String CREATING_THE_FILE = "Applying changes...";
	public static final String YOU_HAVE_UNSAVED_CHANGES = "You have unsaved changes. Do you want to discard your changes?";
	public static final String SEE_THE_ERRORS_ABOVE = "See the error(s) above.";
	public static final long MESSAGE_EXPIRE_TIME = 1000*60*10;  //10 minutes
	QueryResultEditorView view;
	TablePageWidget pageWidget;
	QueryResultBundle startingBundle;
	ClientCache clientCache;
	JobTrackingWidget editJobTrackingWidget;
	GlobalApplicationState globalApplicationState;
	String tableId;
	TableType tableType;
	EventBus eventBus;
	
	@Inject
	public QueryResultEditorWidget(QueryResultEditorView view,
			TablePageWidget pageWidget,
			JobTrackingWidget editJobTrackingWidget,
			GlobalApplicationState globalApplicationState, 
			ClientCache clientCache,
			EventBus eventBus) {
		this.view = view;
		this.pageWidget = pageWidget;
		pageWidget.setTableVisible(true);
		this.editJobTrackingWidget = editJobTrackingWidget;
		this.view.setTablePageWidget(pageWidget);
		this.view.setPresenter(this);
		this.view.setProgressWidget(editJobTrackingWidget);
		this.globalApplicationState = globalApplicationState;
		this.clientCache = clientCache;
		this.eventBus = eventBus;
	}

	@Override
	public Widget asWidget() {
		return this.view.asWidget();
	}

	/**
	 * Configure this widget with a single page of a query result.
	 * 
	 * @param bundle
	 */
	public void showEditor(QueryResultBundle bundle, TableType tableType) {
		this.startingBundle = bundle;
		this.tableType = tableType;
		this.view.setErrorMessageVisible(false);
		// configure the widget
		pageWidget.configure(bundle, null, null, true, tableType, this, null, null);
		setJobRunning(false);
		this.globalApplicationState.setIsEditing(true);
		this.view.setSaveButtonLoading(false);
		boolean isTable = TableType.table.equals(tableType);
		view.setAddRowButtonVisible(isTable);
		view.setButtonToolbarVisible(isTable);
		view.showEditor();
		this.tableId = QueryBundleUtils.getTableId(bundle);
	}

	@Override
	public void onAddRow() {
		pageWidget.onAddNewRow();
	}

	@Override
	public void onToggleSelect() {
		pageWidget.onToggleSelect();
	}

	@Override
	public void onSelectAll() {
		pageWidget.onSelectAll();
	}

	@Override
	public void onSelectNone() {
		this.pageWidget.onSelectNone();
	}

	@Override
	public void onDeleteSelected() {
		pageWidget.onDeleteSelected();
	}

	@Override
	public void onSelectionChanged() {
		// the delete button should only be enabled when there is at least one
		// row selected.
		this.view.setDeleteButtonEnabled(pageWidget
				.isOneRowOrMoreRowsSelected());
	}

	/**
	 * Extract the new RowSet to be saved.
	 * 
	 * @return
	 */
	private PartialRowSet extractDelta() {
		PartialRowSet prs = RowSetUtils.buildDelta(startingBundle.getQueryResult()
				.getQueryResults(), pageWidget.extractRowSet(), pageWidget
				.extractHeaders());
		return prs;
	}

	/**
	 * Show an error message
	 * 
	 * @param message
	 */
	private void showError(String message) {
		this.view.showErrorMessage(message);
		this.view.setErrorMessageVisible(true);
		this.view.setSaveButtonLoading(false);
		setJobRunning(false);
	}

	/**
	 * While running a job all editors are hidden and the job is shown.
	 * 
	 * @param isRunning
	 */
	private void setJobRunning(boolean isRunning) {
		if (isRunning) {
			view.hideEditor();
			view.showProgress();
		} else {
			view.hideProgress();
			view.showEditor();
		}
	}

	/**
	 * Does the editor have unsaved changes?
	 * 
	 * @return
	 */
	private boolean hasUnsavedChanges() {
		return hasUnsavedChanges(extractDelta());
	}

	private static boolean hasUnsavedChanges(PartialRowSet prs) {
		if (prs != null && prs.getRows() != null) {
			return !prs.getRows().isEmpty();
		}
		return false;
	}

	/**
	 * @param response
	 * @return Returns an EntityUpdateResults (if response contains one).  Otherwise this method returns null.
	 */
	public static EntityUpdateResults getEntityUpdateResults(AsynchronousResponseBody response) {
		if (response instanceof TableUpdateTransactionResponse) {
			List<TableUpdateResponse> results = ((TableUpdateTransactionResponse) response).getResults();
			if (results != null) {
				for (TableUpdateResponse tableUpdateResponse : results) {
					if (tableUpdateResponse instanceof EntityUpdateResults) {
						return (EntityUpdateResults)tableUpdateResponse;
					}
				}
			}
		}
		return null;
	}
	
	public static String getEntityUpdateResultsFailures(AsynchronousResponseBody response) {
		EntityUpdateResults results = getEntityUpdateResults(response);
		StringBuilder sb = new StringBuilder();
		if (results != null) {
			for (EntityUpdateResult result : results.getUpdateResults()) {
				if (result.getFailureCode() != null) {
					sb.append("<p>");
					sb.append(result.getEntityId());
					sb.append(" (");
					sb.append(result.getFailureCode());
					sb.append("): ");
					if (result.getFailureMessage() != null) {
						sb.append(result.getFailureMessage());	
					}
					sb.append("</p>");
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * @param response
	 * @return first index in EntityUpdateResult list that does not contain a failure code.  -1 if not found
	 */
	public static int getFirstIndexOfEntityUpdateResultSuccess(AsynchronousResponseBody response) {
		EntityUpdateResults results = getEntityUpdateResults(response);
		if (results != null) {
			List<EntityUpdateResult> resultList = results.getUpdateResults();
			for (int i = 0; i < resultList.size(); i++) {
				if (resultList.get(i).getFailureCode() == null) {
					return i;
				}
			}
		}
		return -1;
	}
	
	@Override
	public void onSave() {
		view.setErrorMessageVisible(false);
		view.setSaveButtonLoading(true);

		// Are there any changes?
		final PartialRowSet prs = extractDelta();
		if (!hasUnsavedChanges(prs)) {
			// There is nothing to save so hide the editor
			doHideEditor();
			return;
		}

		// Are the changes valid?
		if (!pageWidget.isValid()) {
			this.showError(SEE_THE_ERRORS_ABOVE);
			return;
		}
		// We have changes and they are valid so start the append job.
		setJobRunning(true);
		TableUpdateTransactionRequest request = new TableUpdateTransactionRequest();
		AppendableRowSetRequest rowSetRequest = new AppendableRowSetRequest();
		rowSetRequest.setToAppend(prs);
		rowSetRequest.setEntityId(this.tableId);
		request.setEntityId(this.tableId);
		List<TableUpdateRequest> changes = new ArrayList<TableUpdateRequest>();
		changes.add(rowSetRequest);
		request.setChanges(changes);
		editJobTrackingWidget.startAndTrackJob("Applying changes...", false,
				AsynchType.TableTransaction, request,
				new AsynchronousProgressHandler() {

					@Override
					public void onFailure(Throwable failure) {
						showError(failure.getMessage());
					}

					@Override
					public void onComplete(AsynchronousResponseBody response) {
						String errors = QueryResultEditorWidget.getEntityUpdateResultsFailures(response);
						if (!errors.isEmpty()){
							view.showErrorDialog("<h4>Unable to update the following files</h4>" + errors);
						}
						if (!TableType.table.equals(tableType)) {
							int successIndex = getFirstIndexOfEntityUpdateResultSuccess(response);
							if (successIndex > -1) {
								PartialRow pr = prs.getRows().get(successIndex);
								String etag = pr.getEtag();
								Date now = new Date();
								clientCache.put(tableId + VIEW_RECENTLY_CHANGED_KEY, etag, now.getTime() + MESSAGE_EXPIRE_TIME);
							}
						}
						fireEntityUpdatedEvent();
					}

					@Override
					public void onCancel() {
						// If they cancel after the job starts, treat it as a
						// change.
						fireEntityUpdatedEvent();
					}
					
					private void fireEntityUpdatedEvent() {
						doHideEditor();
						eventBus.fireEvent(new EntityUpdatedEvent());
					}
				});
	}
	
	@Override
	public void onCancel() {
		// Are there changes?
		if (hasUnsavedChanges()) {
			// Confirm close.
			view.showConfirmDialog(YOU_HAVE_UNSAVED_CHANGES, new Callback() {
				@Override
				public void invoke() {
					doHideEditor();
				}
			});
		} else {
			doHideEditor();
		}
	}
	
	/**
	 * Hide the modal editor.
	 */
	private void doHideEditor() {
		this.globalApplicationState.setIsEditing(false);
		this.view.hideEditor();
		this.view.hideProgress();
	}

}
