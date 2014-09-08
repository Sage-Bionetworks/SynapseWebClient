package org.sagebionetworks.web.client.widget.table.v2.results;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonToolBar;
import org.gwtbootstrap3.client.ui.Modal;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;

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
	SimplePanel progressPanel;
	@UiField
	ScrollPanel rowEditorModalPanel;
	@UiField
	Alert errorAlert;
	@UiField
	ButtonToolBar resultsToolBar;
	@UiField
	Button editRowsButton;
	@UiField
	Button saveRowsButton;
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
		saveRowsButton.addClickHandler(new ClickHandler() {	
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSave();
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

	@Override
	public void setSaveButtonLoading(boolean isLoading) {
		if(isLoading){
			this.saveRowsButton.state().loading();
		}else{
			this.saveRowsButton.state().reset();
		}
	}

	@Override
	public void hideEditor() {
		editRowsModal.hide();
	}

	@Override
	public void setToolbarVisible(boolean visible) {
		this.resultsToolBar.setVisible(visible);
	}

	@Override
	public void setEditEnabled(boolean isEditable) {
		this.editRowsButton.setVisible(isEditable);
	}

	@Override
	public void setProgressWidget(AsynchronousProgressWidget progressWidget) {
		this.progressPanel.add(progressWidget);
	}

	@Override
	public void setProgressWidgetVisible(boolean visible) {
		this.progressPanel.setVisible(visible);
	}

}
