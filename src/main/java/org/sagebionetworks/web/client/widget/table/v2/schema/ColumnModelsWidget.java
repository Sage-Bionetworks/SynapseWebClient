package org.sagebionetworks.web.client.widget.table.v2.schema;

import java.util.List;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView.ViewType;

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
	
	/**
	 * New presenter with its view.
	 * @param view
	 */
	@Inject
	public ColumnModelsWidget(ColumnModelsViewBase baseView, PortalGinInjector ginInjector, SynapseClientAsync synapseClient, ColumnModelsEditorWidget editor){
		this.ginInjector = ginInjector;
		// we will always have a viewer
		this.baseView = baseView;
		this.baseView.setPresenter(this);
		// We need two copies of the view, one as an editor, and the other as a viewer.
		this.viewer = ginInjector.createNewColumnModelsView();
		this.viewer.setEditHandler(this);
		this.editor = editor;
		// Add all of the parts
		this.baseView.setViewer(this.viewer);
		this.baseView.setEditor(this.editor);
		this.synapseClient = synapseClient;
	}

	@Override
	public void configure(EntityBundle bundle, boolean isEditable, EntityUpdatedHandler updateHandler) {
		this.isEditable = isEditable;
		this.bundle = bundle;
		List<ColumnModel> startingModels = bundle.getTableBundle().getColumnModels();
		this.updateHandler = updateHandler;
		viewer.configure(ViewType.VIEWER, this.isEditable);
		for(ColumnModel cm: startingModels){
			// Create a viewer
			ColumnModelTableRowViewer rowViewer = ginInjector.createNewColumnModelTableRowViewer();
			ColumnModelUtils.applyColumnModelToRow(cm, rowViewer);
			rowViewer.setSelectable(false);
			viewer.addColumn(rowViewer);
		}
		if(isEditable){
			editor.configure(bundle.getEntity(), startingModels);
		}
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
		editor.configure(bundle.getEntity(), bundle.getTableBundle().getColumnModels());
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
		editor.setTableSchema(new AsyncCallback<Void>(){

			@Override
			public void onFailure(Throwable caught) {
				baseView.showError(caught.getMessage());
			}
			
			@Override
			public void onSuccess(Void result) {
				// Hide the dialog
				baseView.hideEditor();
				updateHandler.onPersistSuccess(new EntityUpdatedEvent());
			}}); 
	}
}
