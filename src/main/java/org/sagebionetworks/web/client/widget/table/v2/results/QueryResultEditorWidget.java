package org.sagebionetworks.web.client.widget.table.v2.results;

import org.sagebionetworks.repo.model.table.QueryResultBundle;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * This widget wraps a TablePageWidget and provides edit functionaltiy..
 * 
 * @author John
 *
 */
public class QueryResultEditorWidget  implements QueryResultEditorView.Presenter, IsWidget, RowSelectionListener{

	QueryResultEditorView view;
	TablePageWidget pageWidget;
	
	@Inject
	public QueryResultEditorWidget(QueryResultEditorView view, TablePageWidget pageWidget){
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
	public void configure(QueryResultBundle bundle){
		// configure the widget
		pageWidget.configure(bundle, true, this);
	}

	@Override
	public void onAddRow() {
		pageWidget.addNewRow();
	}

	@Override
	public void onToggleSelect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSelectAll() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSelectNone() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDeleteSelected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSelectedChanged(RowView selected) {
		// TODO Auto-generated method stub
		
	}
}
