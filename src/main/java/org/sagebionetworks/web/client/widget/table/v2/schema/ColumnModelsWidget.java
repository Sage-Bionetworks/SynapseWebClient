package org.sagebionetworks.web.client.widget.table.v2.schema;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.Dataset;
import org.sagebionetworks.repo.model.table.DatasetItem;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.SubmissionView;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionRequest;
import org.sagebionetworks.repo.model.table.ViewColumnModelRequest;
import org.sagebionetworks.repo.model.table.ViewColumnModelResponse;
import org.sagebionetworks.repo.model.table.ViewEntityType;
import org.sagebionetworks.repo.model.table.ViewScope;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.client.widget.table.modal.fileview.ViewDefaultColumns;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView.ViewType;
import org.sagebionetworks.web.shared.asynch.AsynchType;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * All business logic for the ColumnModelsView.
 * 
 * @author jmhill
 *
 */
public class ColumnModelsWidget implements ColumnModelsViewBase.Presenter, ColumnModelsView.EditHandler, SynapseWidgetPresenter {
	public static final String SEE_THE_ERROR_S_ABOVE = "See the error(s) above.";
	PortalGinInjector ginInjector;
	ColumnModelsViewBase baseView;
	ColumnModelsView viewer;
	ColumnModelsEditorWidget editor;
	boolean isEditable;
	SynapseClientAsync synapseClient;
	String tableId;
	EntityBundle bundle;
	JobTrackingWidget jobTrackingWidget;
	ViewDefaultColumns fileViewDefaultColumns;
	TableType tableType;
	SynapseAlert synAlert;
	public static final String UPDATING_SCHEMA = "Updating the table schema...";
	public static final String RETRIEVING_DATA = "Retrieving data...";

	/**
	 * New presenter with its view.
	 * 
	 * @param fileview
	 */
	@Inject
	public ColumnModelsWidget(ColumnModelsViewBase baseView, PortalGinInjector ginInjector, SynapseClientAsync synapseClient, ColumnModelsEditorWidget editor, JobTrackingWidget jobTrackingWidget, ViewDefaultColumns fileViewDefaultColumns, SynapseAlert synAlert) {
		this.ginInjector = ginInjector;
		// we will always have a viewer
		this.baseView = baseView;
		this.jobTrackingWidget = jobTrackingWidget;
		this.synAlert = synAlert;
		this.baseView.setPresenter(this);
		// We need two copies of the view, one as an editor, and the other as a viewer.
		this.viewer = ginInjector.createNewColumnModelsView();
		this.viewer.setEditHandler(this);
		this.editor = editor;
		// Add all of the parts
		this.baseView.setViewer(this.viewer);
		this.baseView.setEditor(this.editor);
		this.baseView.setJobTrackingWidget(jobTrackingWidget);
		this.baseView.setJobTrackingWidgetVisible(false);
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.fileViewDefaultColumns = fileViewDefaultColumns;
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
		baseView.setSynAlert(synAlert);
	}

	@Override
	public void configure(EntityBundle bundle, boolean isEditable) {
		this.isEditable = isEditable;
		this.bundle = bundle;
		List<ColumnModel> startingModels = bundle.getTableBundle().getColumnModels();
		viewer.configure(ViewType.VIEWER, this.isEditable);
		// We can get the default columns/annotations for views and datasets
		boolean isEditableViewOrDataset = isEditable && (bundle.getEntity() instanceof EntityView || bundle.getEntity() instanceof SubmissionView || bundle.getEntity() instanceof Dataset);
		tableType = TableType.getTableType(bundle.getEntity());
		editor.setAddDefaultColumnsButtonVisible(isEditableViewOrDataset);
		editor.setAddAnnotationColumnsButtonVisible(isEditableViewOrDataset);
		List<ColumnModelTableRow> rowViewers = new ArrayList<>();
		for (ColumnModel cm : startingModels) {
			// Create a viewer
			ColumnModelTableRowViewer rowViewer = ginInjector.createNewColumnModelTableRowViewer();
			ColumnModelUtils.applyColumnModelToRow(cm, rowViewer);
			rowViewer.setSelectable(false);
			rowViewers.add(rowViewer);
		}
		viewer.addColumns(rowViewers);
	}

	public void getDefaultColumnsForView() {
		synAlert.clear();
		List<ColumnModel> defaultColumns = fileViewDefaultColumns.getDefaultViewColumns(tableType);
		editor.addColumns(defaultColumns);
	}

	public void getPossibleColumnModelsForViewScope(String nextPageToken) {
		synAlert.clear();
		
		ViewScope scope = new ViewScope();
		List<String> scopeIds = null;
		Entity entity = bundle.getEntity();
		if (entity instanceof EntityView) {
			scopeIds = ((EntityView) entity).getScopeIds();
			scope.setViewTypeMask(((EntityView) entity).getViewTypeMask());
			scope.setViewEntityType(ViewEntityType.entityview);
		} else if (entity instanceof SubmissionView) {
			scopeIds = ((SubmissionView) entity).getScopeIds();
			scope.setViewEntityType(ViewEntityType.submissionview);
		} else if (entity instanceof Dataset) {
			scopeIds = new ArrayList<>();
			for (DatasetItem item : ((Dataset) entity).getItems()) {
				scopeIds.add(item.getEntityId());
			}
			scope.setViewEntityType(ViewEntityType.dataset);
			scope.setViewTypeMask(Long.valueOf(TableType.dataset.getViewTypeMask())); // https://sagebionetworks.jira.com/browse/PLFM-6999
		}
		scope.setScope(scopeIds);
		
		ViewColumnModelRequest request = new ViewColumnModelRequest();
		request.setViewScope(scope);
		request.setNextPageToken(nextPageToken);
		
		this.baseView.setJobTrackingWidgetVisible(true);
		this.jobTrackingWidget.startAndTrackJob(RETRIEVING_DATA, false, AsynchType.ViewColumnModelRequest, request, new AsynchronousProgressHandler() {
			@Override
			public void onFailure(Throwable failure) {
				baseView.setJobTrackingWidgetVisible(false);
				synAlert.handleException(failure);
				baseView.resetSaveButton();
			}

			@Override
			public void onComplete(AsynchronousResponseBody response) {
				ViewColumnModelResponse viewColumnModelResponse = (ViewColumnModelResponse) response;
				editor.addColumns(viewColumnModelResponse.getResults());
				if (viewColumnModelResponse.getNextPageToken() != null) {
					getPossibleColumnModelsForViewScope(viewColumnModelResponse.getNextPageToken());
				} else {
					baseView.setJobTrackingWidgetVisible(false);
				}
			}

			@Override
			public void onCancel() {
				baseView.setJobTrackingWidgetVisible(false);				
			}
		});
	}

	@Override
	public Widget asWidget() {
		return baseView.asWidget();
	}

	@Override
	public void onEditColumns() {
		if (!this.isEditable) {
			throw new IllegalStateException("Cannot call onEditColumns() for a read-only widget");
		}
		editor.configure(tableType, bundle.getTableBundle().getColumnModels());
		// Pass this to the base
		baseView.showEditor();
	}

	@Override
	public void onSave() {
		// Save it the data is valid
		if (!editor.validate()) {
			synAlert.showError(SEE_THE_ERROR_S_ABOVE);
			baseView.resetSaveButton();
			return;
		} else {
			synAlert.clear();
		}
		// Get the models from the view and save them
		baseView.setLoading();
		List<ColumnModel> newSchema = editor.getEditedColumnModels();
		synapseClient.getTableUpdateTransactionRequest(bundle.getEntity().getId(), bundle.getTableBundle().getColumnModels(), newSchema, new AsyncCallback<TableUpdateTransactionRequest>() {

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
				baseView.resetSaveButton();
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
		baseView.setJobTrackingWidgetVisible(false);
		// Hide the dialog
		baseView.hideEditor();
		ginInjector.getEventBus().fireEvent(new EntityUpdatedEvent());
	}

	public void startTrackingJob(TableUpdateTransactionRequest request) {
		this.baseView.setJobTrackingWidgetVisible(true);
		this.jobTrackingWidget.startAndTrackJob(UPDATING_SCHEMA, false, AsynchType.TableTransaction, request, new AsynchronousProgressHandler() {
			@Override
			public void onFailure(Throwable failure) {
				baseView.setJobTrackingWidgetVisible(false);
				synAlert.handleException(failure);
				baseView.resetSaveButton();
			}

			@Override
			public void onComplete(AsynchronousResponseBody response) {
				finished();
			}

			@Override
			public void onCancel() {
				baseView.setJobTrackingWidgetVisible(false);
				baseView.showEditor();
			}
		});
	}
}
