package org.sagebionetworks.web.client.widget.table.v2.results;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A UiBound query results view with zero business logic.
 * 
 * @author John
 *
 */
public class TableQueryResultViewImpl implements TableQueryResultView {
	
	public interface Binder extends UiBinder<Widget, TableQueryResultViewImpl> {}
	
	@UiField
	SimplePanel tablePanel;
	@UiField
	ScrollPanel rowEditorModalPanel;
	@UiField
	Alert errorAlert;
	@UiField
	Button editRowsButton;
	@UiField
	Modal editRowsModal;
	
	Widget widget;
	
	Presenter presenter;
	
	@Inject
	public TableQueryResultViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
	}
	
	@Override
	public void setPresenter(Presenter presenterin) {
		this.presenter = presenterin;
		editRowsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onEditRows();
			}
		});
	}

	@Override
	public void setTableVisible(boolean visible) {
		tablePanel.setVisible(visible);
	}

	@Override
	public void setPageWidget(TablePageWidget pageWidget) {
		tablePanel.add(pageWidget);
	}

	@Override
	public void showError(String message) {
		errorAlert.setText(message);
	}

	@Override
	public void setErrorVisible(boolean visible) {
		errorAlert.setVisible(visible);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setEditorWidget(QueryResultEditorWidget queryResultEditor) {
		rowEditorModalPanel.add(queryResultEditor);
	}

	@Override
	public void showEditor() {
		editRowsModal.show();
	}

}
