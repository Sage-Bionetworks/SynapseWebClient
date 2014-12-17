package org.sagebionetworks.web.client.widget.table.v2.results;

import org.sagebionetworks.repo.model.table.PartialRowSet;
import org.sagebionetworks.repo.model.table.QueryResultBundle;

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

	QueryResultEditorView view;
	TablePageWidget pageWidget;
	QueryResultBundle startingBundle;

	@Inject
	public QueryResultEditorWidget(QueryResultEditorView view,
			TablePageWidget pageWidget) {
		this.view = view;
		this.pageWidget = pageWidget;
		this.view.setTablePageWidget(pageWidget);
		this.view.setPresenter(this);
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
	public void configure(QueryResultBundle bundle) {
		this.startingBundle = bundle;
		this.view.setErrorMessageVisible(false);
		// configure the widget
		pageWidget.configure(bundle, null, null, true, this, null);
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
	 * @return
	 */
	public PartialRowSet extractDelta(){
		return RowSetUtils.buildDelta(startingBundle.getQueryResult().getQueryResults(), pageWidget.extractRowSet(), pageWidget.extractHeaders());
	}

	/**
	 * Show an error message
	 * @param message
	 */
	public void showError(String message) {
		this.view.showErrorMessage(message);
		this.view.setErrorMessageVisible(true);
	}
	
	/**
	 * Hide the error.
	 */
	public void hideError(){
		this.view.setErrorMessageVisible(false);
	}

	public boolean isValid() {
		// Are the results valid
		return pageWidget.isValid();
	}

}
