package org.sagebionetworks.web.client.widget.table.modal.fileview;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionRequest;
import org.sagebionetworks.repo.model.table.ViewType;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizard.TableType;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsWidget;
import org.sagebionetworks.web.shared.asynch.AsynchType;

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
	public static final String SCHEMA_UPDATE_CANCELLED = "Schema update cancelled";
	public static final String FINISH = "Finish";
	ColumnModelsEditorWidget editor;
	String tableId;
	ModalPresenter presenter;
	// the TableEntity or View
	Table entity;
	TableType tableType;
	SynapseClientAsync synapseClient;
	JobTrackingWidget jobTrackingWidget;
	CreateTableViewWizardStep2View view;
	
	/*
	 * Set to true to indicate that change selections are in progress.  This allows selection change events to be ignored during this period.
	 */
	boolean changingSelection = false;
	/**
	 * New presenter with its view.
	 * @param view
	 */
	@Inject
	public CreateTableViewWizardStep2(CreateTableViewWizardStep2View view,
			ColumnModelsEditorWidget editor, 
			SynapseClientAsync synapseClient, 
			JobTrackingWidget jobTrackingWidget){
		this.view = view;
		this.synapseClient = synapseClient;
		this.editor = editor;
		this.jobTrackingWidget = jobTrackingWidget;
		view.setJobTracker(jobTrackingWidget.asWidget());
		view.setEditor(editor.asWidget());
		editor.setOnAddDefaultViewColumnsCallback(new Callback() {
			@Override
			public void invoke() {
				getDefaultColumnsForView();
			}
		});
	}

	public void configure(Table entity, TableType tableType) {
		view.setJobTrackerVisible(false);
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
		return view.asWidget();
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
		synapseClient.getTableUpdateTransactionRequest(entity.getId(), new ArrayList<ColumnModel>(), newSchema, new AsyncCallback<TableUpdateTransactionRequest>(){
			@Override
			public void onFailure(Throwable caught) {
				presenter.setErrorMessage(caught.getMessage());
			}
			
			@Override
			public void onSuccess(TableUpdateTransactionRequest request) {
				if (request.getChanges().isEmpty()) {
					finished();
				} else {
					startTrackingJob(request);	
				}
			}}); 
	}
	public void finished() {
		// Hide the dialog
		presenter.setLoading(false);
		presenter.onFinished();
	}
	
	public void startTrackingJob(TableUpdateTransactionRequest request) {
		view.setJobTrackerVisible(true);
		presenter.setLoading(true);
		this.jobTrackingWidget.startAndTrackJob(ColumnModelsWidget.UPDATING_SCHEMA, false, AsynchType.TableTransaction, request, new AsynchronousProgressHandler() {
			@Override
			public void onFailure(Throwable failure) {
				view.setJobTrackerVisible(false);
				presenter.setErrorMessage(failure.getMessage());
			}
			@Override
			public void onComplete(AsynchronousResponseBody response) {
				view.setJobTrackerVisible(false);
				finished();
			}
			@Override
			public void onCancel() {
				view.setJobTrackerVisible(false);
				presenter.setErrorMessage(SCHEMA_UPDATE_CANCELLED);
			}
		});
	}
}
