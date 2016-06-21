package org.sagebionetworks.web.client.widget.table.v2.results;

import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.AppendableRowSetRequest;
import org.sagebionetworks.repo.model.table.PartialRowSet;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.shared.asynch.AsynchType;

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

	public static final String CREATING_THE_FILE = "Applying changes...";
	public static final String YOU_HAVE_UNSAVED_CHANGES = "You have unsaved changes. Do you want to discard your changes?";
	public static final String SEE_THE_ERRORS_ABOVE = "See the error(s) above.";

	QueryResultEditorView view;
	TablePageWidget pageWidget;
	QueryResultBundle startingBundle;
	JobTrackingWidget editJobTrackingWidget;
	GlobalApplicationState globalApplicationState;
	Callback callback;
	String tableId;

	@Inject
	public QueryResultEditorWidget(QueryResultEditorView view,
			TablePageWidget pageWidget,
			JobTrackingWidget editJobTrackingWidget,
			GlobalApplicationState globalApplicationState) {
		this.view = view;
		this.pageWidget = pageWidget;
		this.editJobTrackingWidget = editJobTrackingWidget;
		this.view.setTablePageWidget(pageWidget);
		this.view.setPresenter(this);
		this.view.setProgressWidget(editJobTrackingWidget);
		this.globalApplicationState = globalApplicationState;
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
	public void showEditor(QueryResultBundle bundle, Callback callback) {
		this.callback = callback;
		this.startingBundle = bundle;
		this.view.setErrorMessageVisible(false);
		// configure the widget
		pageWidget.configure(bundle, null, null, true, this, null);
		setJobRunning(false);
		this.globalApplicationState.setIsEditing(true);
		this.view.setSaveButtonLoading(false);
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
		return RowSetUtils.buildDelta(startingBundle.getQueryResult()
				.getQueryResults(), pageWidget.extractRowSet(), pageWidget
				.extractHeaders());
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
		view.setEditorPanelVisible(!isRunning);
		view.setProgressPanelVisible(isRunning);
		if (isRunning) {
			view.scrollProgressIntoView();
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

	@Override
	public void onSave() {
		view.setErrorMessageVisible(false);
		view.setSaveButtonLoading(true);

		// Are there any changes?
		PartialRowSet prs = extractDelta();
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
		AppendableRowSetRequest request = new AppendableRowSetRequest();
		request.setToAppend(prs);
		request.setEntityId(this.tableId);
		editJobTrackingWidget.startAndTrackJob("Applying changes...", false,
				AsynchType.TableAppendRowSet, request,
				new AsynchronousProgressHandler() {

					@Override
					public void onFailure(Throwable failure) {
						showError(failure.getMessage());
					}

					@Override
					public void onComplete(AsynchronousResponseBody response) {
						doHideEditor();
						callback.invoke();
					}

					@Override
					public void onCancel() {
						// If they cancel after the job starts, treat it as a
						// change.
						doHideEditor();
						callback.invoke();
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
	}

}
