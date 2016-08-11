package org.sagebionetworks.web.client.widget.table.modal.fileview;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.repo.model.table.ViewType;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
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
	Table entity;
	TableType tableType;
	SynapseClientAsync synapseClient;
	
	/*
	 * Set to true to indicate that change selections are in progress.  This allows selection change events to be ignored during this period.
	 */
	boolean changingSelection = false;
	/**
	 * New presenter with its view.
	 * @param view
	 */
	@Inject
	public CreateTableViewWizardStep2(ColumnModelsEditorWidget editor, SynapseClientAsync synapseClient){
		this.synapseClient = synapseClient;
		this.editor = editor;
		editor.setOnAddDefaultViewColumnsCallback(new Callback() {
			@Override
			public void invoke() {
				getDefaultColumnsForView();
			}
		});
	}

	public void configure(Table entity, TableType tableType) {
		this.changingSelection = false;
		this.entity = entity;
		this.tableType = tableType;
		
		editor.configure(new ArrayList<ColumnModel>());
		if (TableType.view.equals(tableType)) {
			// start with the default file columns
			this.editor.setAddDefaultViewColumnsButtonVisible(true);
			getDefaultColumnsForView();
		} else {
			this.editor.setAddDefaultViewColumnsButtonVisible(false);
		}
	}
	
	public void getDefaultColumnsForView() {
		ViewType type = ((EntityView)entity).getType(); 
		synapseClient.getDefaultColumnsForView(type, new AsyncCallback<List<ColumnModel>>() {
			@Override
			public void onFailure(Throwable caught) {
				presenter.setErrorMessage(caught.getMessage());
			}
			@Override
			public void onSuccess(List<ColumnModel> columns) {
				editor.addColumns(columns);
			}
		});
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
			return;
		}
		// Get the models from the view and save them
		List<ColumnModel> newSchema = editor.getEditedColumnModels();
		synapseClient.setTableSchema(entity, newSchema, new AsyncCallback<Void>(){
			@Override
			public void onFailure(Throwable caught) {
				presenter.setErrorMessage(caught.getMessage());
			}
			
			@Override
			public void onSuccess(Void result) {
				presenter.setLoading(false);
				presenter.onFinished();
			}}); 
	}
}
