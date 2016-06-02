package org.sagebionetworks.web.client.widget.table.modal.fileview;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizard.TableType;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsWidget;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Wizard page to edit column schema
 * 
 * @author Jay
 *
 */
public class CreateTableViewWizardStep2 implements ModalPage, IsWidget {
	public static final String FINISH = "Finish";
	ColumnModelsEditorWidget editor;
	String tableId;
	ModalPresenter presenter;
	// the TableEntity or View
	Entity entity;
	TableType tableType;
	
	/*
	 * Set to true to indicate that change selections are in progress.  This allows selection change events to be ignored during this period.
	 */
	boolean changingSelection = false;
	/**
	 * New presenter with its view.
	 * @param view
	 */
	@Inject
	public CreateTableViewWizardStep2(ColumnModelsEditorWidget editor){
		this.editor = editor;
		this.editor.setAddAllAnnotationsButtonVisible(false);
		this.editor.setAddDefaultFileColumnsButtonVisible(false);
	}

	public void configure(Entity entity, TableType tableType) {
		configure(entity, tableType, new ArrayList<ColumnModel>());
	}
	
	public void configure(Entity entity, TableType tableType, List<ColumnModel> startingModels) {
		this.changingSelection = false;
		this.entity = entity;
		this.tableType = tableType;
		editor.configure(entity, startingModels);
		this.editor.setAddAllAnnotationsButtonVisible(TableType.view.equals(tableType));
		this.editor.setAddDefaultFileColumnsButtonVisible(TableType.view.equals(tableType));
	}

	@Override
	public Widget asWidget() {
		return editor.asWidget();
	}

	@Override
	public void setModalPresenter(ModalPresenter presenter) {
		this.presenter = presenter;
		presenter.setPrimaryButtonText(FINISH);
	}
	
	@Override
	public void onPrimary() {
		presenter.setLoading(true);
		// Save it the data is valid
		if(!editor.validate()){
			presenter.setErrorMessage(ColumnModelsWidget.SEE_THE_ERROR_S_ABOVE);
			presenter.setLoading(false);
			return;
		}
		// Get the models from the view and save them
		editor.setTableSchema(new AsyncCallback<Void>(){
			@Override
			public void onFailure(Throwable caught) {
				presenter.setLoading(false);
				presenter.setErrorMessage(caught.getMessage());
			}
			
			@Override
			public void onSuccess(Void result) {
				presenter.setLoading(false);
				presenter.onFinished();
			}}); 
	}
}
