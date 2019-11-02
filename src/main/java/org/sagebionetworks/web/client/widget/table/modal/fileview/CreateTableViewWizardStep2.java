package org.sagebionetworks.web.client.widget.table.modal.fileview;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnModelPage;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionRequest;
import org.sagebionetworks.repo.model.table.ViewScope;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget;
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
	public static final String DELETE_PLACEHOLDER_FAILURE_MESSAGE = "Unable to delete table/view ";
	public static final String DELETE_PLACEHOLDER_SUCCESS_MESSAGE = "User cancelled creation of table/view.  Deleted placeholder: ";
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
	SynapseJavascriptClient jsClient;
	CreateTableViewWizardStep2View view;
	SynapseJSNIUtils jsniUtils;

	/*
	 * Set to true to indicate that change selections are in progress. This allows selection change
	 * events to be ignored during this period.
	 */
	boolean changingSelection = false;
	ViewDefaultColumns fileViewDefaultColumns;

	/**
	 * New presenter with its view.
	 * 
	 * @param view
	 */
	@Inject
	public CreateTableViewWizardStep2(CreateTableViewWizardStep2View view, ColumnModelsEditorWidget editor, SynapseClientAsync synapseClient, JobTrackingWidget jobTrackingWidget, ViewDefaultColumns fileViewDefaultColumns, SynapseJavascriptClient jsClient, SynapseJSNIUtils jsniUtils) {
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.editor = editor;
		this.jobTrackingWidget = jobTrackingWidget;
		this.fileViewDefaultColumns = fileViewDefaultColumns;
		this.jsClient = jsClient;
		this.jsniUtils = jsniUtils;
		view.setJobTracker(jobTrackingWidget.asWidget());
		view.setEditor(editor.asWidget());
		editor.setOnAddDefaultViewColumnsCallback(new Callback() {
			@Override
			public void invoke() {
				getDefaultColumnsForView();
			}
		});
		editor.setOnAddAnnotationColumnsCallback(new Callback() {
			@Override
			public void invoke() {
				getPossibleColumnModelsForViewScope(null);
			}
		});
	}

	public void configure(Table entity, TableType tableType) {
		view.setJobTrackerVisible(false);
		this.changingSelection = false;
		this.entity = entity;
		this.tableType = tableType;

		editor.configure(tableType, new ArrayList<ColumnModel>());

		boolean isView = !TableType.table.equals(tableType);
		this.editor.setAddDefaultViewColumnsButtonVisible(isView);
		this.editor.setAddAnnotationColumnsButtonVisible(isView);
		if (isView) {
			// start with the default file columns
			getDefaultColumnsForView();
		}
	}

	public void getDefaultColumnsForView() {
		boolean clearIds = true;
		List<ColumnModel> defaultColumns = fileViewDefaultColumns.getDefaultViewColumns(tableType.isIncludeFiles(), clearIds);
		editor.addColumns(defaultColumns);
	}

	public void getPossibleColumnModelsForViewScope(String nextPageToken) {
		presenter.clearErrors();
		ViewScope scope = new ViewScope();
		scope.setScope(((EntityView) entity).getScopeIds());
		scope.setViewTypeMask(tableType.getViewTypeMask().longValue());
		synapseClient.getPossibleColumnModelsForViewScope(scope, nextPageToken, new AsyncCallback<ColumnModelPage>() {
			@Override
			public void onFailure(Throwable caught) {
				presenter.setError(caught);
			}

			@Override
			public void onSuccess(ColumnModelPage columnPage) {
				editor.addColumns(columnPage.getResults());
				if (columnPage.getNextPageToken() != null) {
					getPossibleColumnModelsForViewScope(columnPage.getNextPageToken());
				}
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

		((ModalWizardWidget) presenter).addCallback(new ModalWizardWidget.WizardCallback() {
			@Override
			public void onFinished() {}

			@Override
			public void onCanceled() {
				onCancel();
			}
		});
	}

	public void onCancel() {
		// user decided not to create the table/view. clean it up.
		String entityId = entity.getId();
		jsClient.deleteEntityById(entityId, true, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				jsniUtils.consoleError(DELETE_PLACEHOLDER_FAILURE_MESSAGE + entityId + ": " + caught.getMessage());
			}

			@Override
			public void onSuccess(Void result) {
				jsniUtils.consoleLog(DELETE_PLACEHOLDER_SUCCESS_MESSAGE + entityId);
			}
		});
	}

	@Override
	public void onPrimary() {
		presenter.setLoading(true);
		// Save it the data is valid
		if (!editor.validate()) {
			presenter.setErrorMessage(ColumnModelsWidget.SEE_THE_ERROR_S_ABOVE);
			return;
		}
		// Get the models from the view and save them
		List<ColumnModel> newSchema = editor.getEditedColumnModels();
		presenter.clearErrors();
		synapseClient.getTableUpdateTransactionRequest(entity.getId(), new ArrayList<ColumnModel>(), newSchema, new AsyncCallback<TableUpdateTransactionRequest>() {
			@Override
			public void onFailure(Throwable caught) {
				presenter.setError(caught);
			}

			@Override
			public void onSuccess(TableUpdateTransactionRequest request) {
				if (request.getChanges().isEmpty()) {
					finished();
				} else {
					startTrackingJob(request);
				}
			}
		});
	}

	public void finished() {
		// Hide the dialog
		presenter.setLoading(false);
		presenter.onFinished();
	}

	public void startTrackingJob(TableUpdateTransactionRequest request) {
		view.setJobTrackerVisible(true);
		presenter.setLoading(true);
		presenter.clearErrors();
		this.jobTrackingWidget.startAndTrackJob(ColumnModelsWidget.UPDATING_SCHEMA, false, AsynchType.TableTransaction, request, new AsynchronousProgressHandler() {
			@Override
			public void onFailure(Throwable failure) {
				view.setJobTrackerVisible(false);
				presenter.setError(failure);
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
