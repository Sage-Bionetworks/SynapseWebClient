package org.sagebionetworks.web.client.widget.table.v2;

import java.util.LinkedList;
import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelsView.ViewType;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * All business logic for the ColumnModelsView.
 * 
 * @author jmhill
 *
 */
public class ColumnModelsWidget implements ColumnModelsView.Presenter, ColumnModelsViewBase.Presenter, SynapseWidgetPresenter{
	
	public static final ColumnType DEFAULT_NEW_COLUMN_TYPE = ColumnType.STRING;
	public static final long DEFAULT_STRING_MAX_SIZE = 50L;
	PortalGinInjector ginInjector;
	ColumnModelsViewBase baseView;
	ColumnModelsView viewer;
	ColumnModelsView editor;
	boolean isEditable;
	TableModelUtils tableModelUtils;
	SynapseClientAsync synapseClient;
	String tableId;
	List<ColumnModel> startingModels;
	List<ColumnModelTableRow> editorRows;
	/**
	 * New presenter with its view.
	 * @param view
	 */
	@Inject
	public ColumnModelsWidget(ColumnModelsViewBase baseView, PortalGinInjector ginInjector, SynapseClientAsync synapseClient, TableModelUtils tableModelUtils){
		this.ginInjector = ginInjector;
		// we will always have a viewer
		this.baseView = baseView;
		this.baseView.setPresenter(this);
		// We need two copies of the view, one as an editor, and the other as a viewer.
		this.viewer = ginInjector.createNewColumnModelsView();
		this.viewer.setPresenter(this);
		this.editor = ginInjector.createNewColumnModelsView();
		this.editor.setPresenter(this);
		// Add all of the parts
		this.baseView.setViewer(this.viewer);
		this.baseView.setEditor(this.editor);
		this.synapseClient = synapseClient;
		this.tableModelUtils = tableModelUtils;
		this.editorRows = new LinkedList<ColumnModelTableRow>();
	}

	@Override
	public void configure(String tableId, List<ColumnModel> models, boolean isEditable) {
		this.tableId = tableId;
		this.isEditable = isEditable;
		this.startingModels = models;
		viewer.configure(ViewType.VIEWER, this.isEditable);
		for(ColumnModel cm: models){
			// Create a viewer
			ColumnModelTableRowViewer rowViewer = ginInjector.createNewColumnModelTableRowViewer();
			ColumnModelUtils.applyColumnModelToRow(cm, rowViewer);
			rowViewer.setSelectable(false);
			viewer.addColumn(rowViewer);
		}
	}

	@Override
	public List<ColumnModel> getEditedColumnModels() {
		if(!isEditable){
			throw new IllegalStateException("Can only be called on an editable view.");
		}
		return ColumnModelUtils.extractColumnModels(this.editorRows);
	}

	@Override
	public boolean validateModel() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addNewColumn() {
		// Create a new column
		ColumnModel cm = new ColumnModel();
		cm.setColumnType(DEFAULT_NEW_COLUMN_TYPE);
		cm.setMaximumSize(DEFAULT_STRING_MAX_SIZE);
		// Assign an id to this column
		ColumnModelTableRowEditor rowEditor = ginInjector.createNewColumnModelTableRowEditor();
		ColumnModelUtils.applyColumnModelToRow(cm, rowEditor);
		editor.addColumn(rowEditor);
		this.editorRows.add(rowEditor);
		// Setup a presenter for this row
		new ColumnModelTableRowEditorPresenter(rowEditor);
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
		// reset the editor to the starting state
		resetEditor();
		// Pass this to the base
		baseView.showEditor();
	}
	/**
	 * Reset the 
	 */
	private void resetEditor(){
		this.editorRows.clear();
		editor.configure(ViewType.EDITOR, this.isEditable);
		for(ColumnModel cm: this.startingModels){
			ColumnModelTableRowViewer rowEditor = ginInjector.createNewColumnModelTableRowViewer();
			ColumnModelUtils.applyColumnModelToRow(cm, rowEditor);
			rowEditor.setSelectable(true);
			editor.addColumn(rowEditor);
			this.editorRows.add(rowEditor);
		}
	}

	@Override
	public void columnSelectionChanged(String columnId, Boolean isSelected) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSave() {
		
		// Get the models from the view and save them
		List<ColumnModel> newSchema = getEditedColumnModels();
		List<String> json;
		try {
			baseView.setLoading();
			json = tableModelUtils.toJSONList(newSchema);
			synapseClient.setTableSchema(tableId, json, new AsyncCallback<List<String>>(){

				@Override
				public void onFailure(Throwable caught) {
					baseView.showError(caught.getMessage());
				}

				@Override
				public void onSuccess(List<String> result) {
					// Convert back
					try {
						List<ColumnModel> results = tableModelUtils.columnModelFromJSON(result);
						// Hide the dialog
						baseView.hideEditor();
						// Reconfigure the view
						configure(tableId, results, isEditable);
					} catch (JSONObjectAdapterException e) {
						baseView.showError(e.getMessage());
					}
				}});
		} catch (JSONObjectAdapterException e) {
			baseView.showError(e.getMessage());
		}
	}
}
