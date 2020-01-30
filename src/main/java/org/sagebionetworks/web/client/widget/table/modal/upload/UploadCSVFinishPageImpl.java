package org.sagebionetworks.web.client.widget.table.modal.upload;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.ColumnChange;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.TableSchemaChangeRequest;
import org.sagebionetworks.repo.model.table.TableUpdateRequest;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionRequest;
import org.sagebionetworks.repo.model.table.UploadToTableRequest;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRow;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelUtils;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UploadCSVFinishPageImpl implements UploadCSVFinishPage {

	private static final String INSTRUCTIONS = "Use the schema options button to make changes to the columns of the table.  Use the create button to finish building the table.";
	private static final String COL = "col";
	private static final String CREATE = "Create";
	public static final String APPLYING_CSV_TO_THE_TABLE = "Applying CSV to the Table...";
	public static final String CREATING_TABLE_COLUMNS = "Creating table columns...";
	public static final String CREATING_THE_TABLE = "Creating the table...";

	UploadCSVFinishPageView view;
	SynapseJavascriptClient jsClient;
	SynapseClientAsync synapseClient;
	PortalGinInjector portalGinInjector;
	JobTrackingWidget jobTrackingWidget;
	KeyboardNavigationHandler keyboardNavigationHandler;

	String parentId;
	UploadToTableRequest uploadtoTableRequest;
	ModalPresenter presenter;
	List<ColumnModelTableRow> editors;

	@Inject
	public UploadCSVFinishPageImpl(UploadCSVFinishPageView view, SynapseClientAsync synapseClient, SynapseJavascriptClient jsClient, PortalGinInjector portalGinInjector, JobTrackingWidget jobTrackingWidget, KeyboardNavigationHandler keyboardNavigationHandler) {
		super();
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.jsClient = jsClient;
		this.portalGinInjector = portalGinInjector;
		this.jobTrackingWidget = jobTrackingWidget;
		this.keyboardNavigationHandler = keyboardNavigationHandler;
		this.view.addTrackerWidget(jobTrackingWidget);
	}

	@Override
	public void onPrimary() {
		createColumns();
	}

	@Override
	public void setModalPresenter(ModalPresenter presenter) {
		this.presenter = presenter;
		this.presenter.setPrimaryButtonText(CREATE);
		this.view.setTrackerVisible(false);
		this.presenter.setInstructionMessage(INSTRUCTIONS);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void configure(String fileName, String parentId, UploadToTableRequest request, List<ColumnModel> suggestedSchema) {
		view.setTableName(fileName);
		this.parentId = parentId;
		this.uploadtoTableRequest = preProcessUploadToTableRequest(request);
		this.keyboardNavigationHandler.removeAllRows();
		// prepare the columns
		List<ColumnModel> columns = preProcessColumns(suggestedSchema);
		editors = new ArrayList<ColumnModelTableRow>(columns.size());
		for (ColumnModel cm : columns) {
			ColumnModelTableRowEditorWidget editor = portalGinInjector.createColumnModelEditorWidget();
			editors.add(editor);
			this.keyboardNavigationHandler.bindRow(editor);
			editor.configure(cm, null);
			editor.setSelectVisible(false);
		}
		view.setColumnEditor(editors);
	}

	private void createColumns() {
		try {
			presenter.setLoading(true);

			List<ColumnModel> schema = getCurrentSchema();
			// Create the columns
			synapseClient.createTableColumns(schema, new AsyncCallback<List<ColumnModel>>() {

				@Override
				public void onFailure(Throwable caught) {
					presenter.setErrorMessage(caught.getMessage());
				}

				@Override
				public void onSuccess(List<ColumnModel> schema) {
					createTable(schema);
				}
			});
		} catch (IllegalArgumentException e) {
			presenter.setErrorMessage(e.getMessage());
		}
	}

	public void createTable(final List<ColumnModel> schema) {
		// Get the column model ids.
		TableEntity table = new TableEntity();
		table.setParentId(this.parentId);
		table.setName(this.view.getTableName());
		// Create the table
		jsClient.createEntity(table, new AsyncCallback<Entity>() {

			@Override
			public void onSuccess(Entity result) {
				applyCSVToTable((TableEntity) result, schema);
			}

			@Override
			public void onFailure(Throwable caught) {
				presenter.setErrorMessage(caught.getMessage());
			}
		});
	}

	/**
	 * Apply the CSV to the table.
	 * 
	 * @param table
	 */
	public void applyCSVToTable(final TableEntity table, List<ColumnModel> schema) {
		// Get the preview request.
		this.uploadtoTableRequest.setTableId(table.getId());
		this.view.setTrackerVisible(true);

		TableUpdateTransactionRequest transactionRequest = new TableUpdateTransactionRequest();
		transactionRequest.setEntityId(uploadtoTableRequest.getTableId());
		List<TableUpdateRequest> changes = new ArrayList<TableUpdateRequest>();
		TableSchemaChangeRequest changeRequest = new TableSchemaChangeRequest();
		List<ColumnChange> columnChanges = new ArrayList<ColumnChange>(schema.size());
		for (ColumnModel cm : schema) {
			ColumnChange cc = new ColumnChange();
			cc.setOldColumnId(null);
			cc.setNewColumnId(cm.getId());
			columnChanges.add(cc);
		}
		changeRequest.setChanges(columnChanges);
		changes.add(changeRequest);
		changes.add(uploadtoTableRequest);
		transactionRequest.setChanges(changes);


		this.jobTrackingWidget.startAndTrackJob(APPLYING_CSV_TO_THE_TABLE, false, AsynchType.TableTransaction, transactionRequest, new AsynchronousProgressHandler() {

			@Override
			public void onFailure(Throwable failure) {
				presenter.setErrorMessage(failure.getMessage());
			}

			@Override
			public void onComplete(AsynchronousResponseBody response) {
				presenter.onFinished();
			}

			@Override
			public void onCancel() {
				presenter.onCancel();
			}
		});
	}

	/**
	 * Pre-process the passed columns. Returns a cloned list of ColumnModels, each modified as needed.
	 * 
	 * @param adapter
	 * @param columns
	 * @return
	 */
	public static List<ColumnModel> preProcessColumns(List<ColumnModel> columns) {
		for (int i = 0; i < columns.size(); i++) {
			ColumnModel cm = columns.get(i);
			// Set a default name
			if (cm.getName() == null) {
				cm.setName(COL + (i + 1));
			}
		}
		return columns;
	}

	/**
	 * This method will create a clone of the input object and change some of the values if needed.
	 * 
	 * @param request
	 * @return
	 */
	public static UploadToTableRequest preProcessUploadToTableRequest(UploadToTableRequest request) {
		UploadToTableRequest clone = UploadRequestUtils.cloneUploadToTableRequest(request);
		/*
		 * If the first line is a header, then we want to skip it. This allows the table's schema to have
		 * different names than the headers in the original CSV file.
		 */
		if (clone.getCsvTableDescriptor() != null && clone.getCsvTableDescriptor().getIsFirstLineHeader() != null) {
			if (clone.getCsvTableDescriptor().getIsFirstLineHeader()) {
				if (clone.getLinesToSkip() == null) {
					clone.setLinesToSkip(Long.valueOf(1L));
				} else {
					clone.setLinesToSkip(Long.valueOf(clone.getLinesToSkip() + 1L));
				}
				clone.getCsvTableDescriptor().setIsFirstLineHeader(Boolean.FALSE);
			}
		}
		return clone;
	}

	/**
	 * Extract the current schema.
	 */
	public List<ColumnModel> getCurrentSchema() {
		return ColumnModelUtils.extractColumnModels(editors);
	}

	/**
	 * Extract the current upload request.
	 * 
	 * @return
	 */
	public UploadToTableRequest getUploadToTableRequest() {
		return this.uploadtoTableRequest;
	}
}
