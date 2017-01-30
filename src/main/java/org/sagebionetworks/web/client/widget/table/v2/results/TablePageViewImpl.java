package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.List;

import org.gwtbootstrap3.client.shared.event.AlertCloseEvent;
import org.gwtbootstrap3.client.shared.event.AlertCloseHandler;
import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.TableHeader;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import org.sagebionetworks.web.client.widget.pagination.PaginationWidget;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * UiBound implementation of a TableView with zero business logic.
 * @author John
 *
 */
public class TablePageViewImpl implements TablePageView {
	
	public interface Binder extends UiBinder<Widget, TablePageViewImpl> {}
	
	@UiField
	TableRow header;
	@UiField
	TBody body;
	@UiField
	SimplePanel paginationPanel;
	@UiField
	SimplePanel editorPopupBuffer;
	@UiField
	Div facetsWidgetPanel;
	@UiField
	ScrollPanel facetsWidgetContainer;
	@UiField
	Div tablePanel;
	@UiField
	Alert viewModifiedRecentlyAlert;
	Widget widget;
	Presenter presenter;
	
	@Inject
	public TablePageViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
		viewModifiedRecentlyAlert.addCloseHandler(new AlertCloseHandler() {
			@Override
			public void onClose(AlertCloseEvent evt) {
				presenter.viewRecentlyModifiedAlertDismissed();
			}
		});
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setTableHeaders(List<IsWidget> headers) {
		header.clear();
		body.clear();
		// Blank header for the selection.
		header.add(new TableHeader());
		for(IsWidget inHeader: headers){
			header.add(inHeader);
		}
	}

	@Override
	public void addRow(RowWidget newRow) {
		body.add(newRow);
	}

	@Override
	public void removeRow(RowWidget row) {
		body.remove(row);
	}

	@Override
	public void setPaginationWidget(PaginationWidget paginationWidget) {
		this.paginationPanel.add(paginationWidget);
	}

	@Override
	public void setPaginationWidgetVisible(boolean visible) {
		this.paginationPanel.setVisible(visible);
	}

	@Override
	public void setEditorBufferVisible(boolean isEditable) {
		this.editorPopupBuffer.setVisible(isEditable);
	}

	@Override
	public void setFacetsWidget(Widget w) {
		facetsWidgetPanel.clear();
		facetsWidgetPanel.add(w);
	}
	
	@Override
	public void setFacetsVisible(boolean visible) {
		facetsWidgetContainer.setVisible(visible);
	}
	
	@Override
	public void setViewRecentlyModifiedAlertvisible(boolean visible) {
		viewModifiedRecentlyAlert.setVisible(visible);
	}
}
