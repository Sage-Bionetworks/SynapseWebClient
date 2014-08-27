package org.sagebionetworks.web.client.widget.table.v2.results;

import org.gwtbootstrap3.client.ui.ButtonToolBar;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * An implementation of the QueryResultEditorView with zero business logic.
 * 
 * @author John
 *
 */
public class QueryResultEditorViewImpl implements QueryResultEditorView {
	
	public interface Binder extends UiBinder<Widget, QueryResultEditorViewImpl> {}

	
	@UiField
	ButtonToolBar buttonToolbar;
	@UiField
	SimplePanel tablePanel;
	
	Presenter presenter;
	
	Widget widget;
	
	@Inject
	public QueryResultEditorViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setTablePageWidget(TablePageWidget pageWidget) {
		this.tablePanel.add(pageWidget);
	}

}
