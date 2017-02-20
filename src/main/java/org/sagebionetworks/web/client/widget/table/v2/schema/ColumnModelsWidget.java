package org.sagebionetworks.web.client.widget.table.v2.schema;

import java.util.List;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnModelPage;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionRequest;
import org.sagebionetworks.repo.model.table.ViewScope;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.FileViewDefaultColumns;
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
public class ColumnModelsWidget implements ColumnModelsViewBase.Presenter, ColumnModelsView.EditHandler, SynapseWidgetPresenter{
	public static final String SEE_THE_ERROR_S_ABOVE = "See the error(s) above.";
	PortalGinInjector ginInjector;
	ColumnModelsViewBase baseView;
	ColumnModelsView viewer;
	ColumnModelsEditorWidget editor;
	boolean isEditable;
	SynapseClientAsync synapseClient;
	String tableId;
	EntityBundle bundle;
	EntityUpdatedHandler updateHandler;
	JobTrackingWidget jobTrackingWidget;
	FileViewDefaultColumns fileViewDefaultColumns;
	
	public static final String UPDATING_SCHEMA = "Updating the table schema...";
	/**
	 * New presenter with its view.
	 * @param view
	 */
	@Inject
	public ColumnModelsWidget(ColumnModelsViewBase baseView, 
			PortalGinInjector ginInjector, 
			SynapseClientAsync synapseClient, 
			ColumnModelsEditorWidget editor, 
			JobTrackingWidget jobTrackingWidget,
			FileViewDefaultColumns fileViewDefaultColumns){
		this.ginInjector = ginInjector;
		// we will always have a viewer
		this.baseView = baseView;
		this.jobTrackingWidget = jobTrackingWidget;
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
	}

	@Override
	public void configure(EntityBundle bundle, boolean isEditable, EntityUpdatedHandler updateHandler) {
		this.isEditable = isEditable;
		this.bundle = bundle;
		List<ColumnModel> startingModels = bundle.getTableBundle().getColumnModels();
		this.updateHandler = updateHandler;
		viewer.configure(ViewType.VIEWER, this.isEditable);
		boolean isEditableView = isEditable && bundle.getEntity() instanceof EntityView;
		editor.setAddDefaultViewColumnsButtonVisible(isEditableView);
		editor.setAddAnnotationColumnsButtonVisible(isEditableView);
		for(ColumnModel cm: startingModels){
			// Create a viewer
			ColumnModelTableRowViewer rowViewer = ginInjector.createNewColumnModelTableRowViewer();
			ColumnModelUtils.applyColumnModelToRow(cm, rowViewer);
			rowViewer.setSelectable(false);
			viewer.addColumn(rowViewer);
		}
	}

	public void getDefaultColumnsForView() {
		baseView.hideErrors();
		boolean isClearIds = true;
		fileViewDefaultColumns.getDefaultColumns(isClearIds, new AsyncCallback<List<ColumnModel>>() {
			@Override
			public void onFailure(Throwable caught) {
				baseView.showError(caught.getMessage());
			}
			@Override
			public void onSuccess(List<ColumnModel> columns) {
				editor.addColumns(columns);
			}
		});
	}
	
	public void getPossibleColumnModelsForViewScope(String nextPageToken) {
		baseView.hideErrors();
		ViewScope scope = new ViewScope();
		scope.setScope(((EntityView)bundle.getEntity()).getScopeIds());
		synapseClient.getPossibleColumnModelsForViewScope(scope, nextPageToken, new AsyncCallback<ColumnModelPage>() {
			@Override
			public void onFailure(Throwable caught) {
				baseView.showError(caught.getMessage());
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
		return baseView.asWidget();
	}
	
	@Override
	public void onEditColumns() {
		if(!this.isEditable){
			throw new IllegalStateException("Cannot call onEditColumns() for a read-only widget");
		}
		editor.configure(bundle.getTableBundle().getColumnModels());
		// Pass this to the base
		baseView.showEditor();
	}
	
	@Override
	public void onSave() {
		// Save it the data is valid
		if(!editor.validate()){
			baseView.showError(SEE_THE_ERROR_S_ABOVE);
			return;
		}else{
			baseView.hideErrors();
		}
		// Get the models from the view and save them
		baseView.setLoading();
		List<ColumnModel> newSchema = editor.getEditedColumnModels();
		synapseClient.getTableUpdateTransactionRequest(bundle.getEntity().getId(), bundle.getTableBundle().getColumnModels(), newSchema, new AsyncCallback<TableUpdateTransactionRequest>(){

			@Override
			public void onFailure(Throwable caught) {
				baseView.showError(caught.getMessage());
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
		baseView.setJobTrackingWidgetVisible(false);
		// Hide the dialog
		baseView.hideEditor();
		updateHandler.onPersistSuccess(new EntityUpdatedEvent());
	}
	
	public void startTrackingJob(TableUpdateTransactionRequest request) {
		this.baseView.setJobTrackingWidgetVisible(true);
		this.jobTrackingWidget.startAndTrackJob(UPDATING_SCHEMA, false, AsynchType.TableTransaction, request, new AsynchronousProgressHandler() {
			@Override
			public void onFailure(Throwable failure) {
				baseView.setJobTrackingWidgetVisible(false);
				baseView.showError(failure.getMessage());
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
