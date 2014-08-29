package org.sagebionetworks.web.client.widget.table.v2.results;

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
		// configure the widget
		pageWidget.configure(bundle, true, this);
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

}
