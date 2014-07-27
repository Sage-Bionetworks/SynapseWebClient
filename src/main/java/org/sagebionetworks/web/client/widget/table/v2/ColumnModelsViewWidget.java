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
public class ColumnModelsViewWidget implements ColumnModelsView.Presenter, ColumnModelsViewBase.Presenter, SynapseWidgetPresenter{
	
	PortalGinInjector ginInjector;
	ColumnModelsViewBase baseView;
	ColumnModelsView viewer;
	ColumnModelsView editor;
	boolean isEditable;
	TableModelUtils tableModelUtils;
	SynapseClientAsync synapseClient;
	String tableId;
	List<ColumnModelTableRow> editorRows;
	/**
	 * New presenter with its view.
	 * @param view
	 */
	@Inject
	public ColumnModelsViewWidget(ColumnModelsViewBase baseView, PortalGinInjector ginInjector, SynapseClientAsync synapseClient, TableModelUtils tableModelUtils){
		this.ginInjector = ginInjector;
		// we will always have a viewer
		this.baseView = baseView;
		this.baseView.setPresenter(this);
		// We need two copies of the view, one as an editor, and the other as a viewer.
		this.viewer = ginInjector.getColumnModelsView();
		this.viewer.setPresenter(this);
		this.editor = ginInjector.getColumnModelsView();
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
		this.editorRows.clear();
		viewer.configure(ViewType.VIEWER, this.isEditable);
		editor.configure(ViewType.EDITOR, this.isEditable);
		// If this is 
		baseView.setEditable(isEditable);
		for(ColumnModel cm: models){
			// Create a viewer
			ColumnModelTableRowViewer rowViewer = ginInjector.createColumnModelTableRowViewer();
			ColumnModelUtils.applyColumnModelToRow(cm, rowViewer);
			rowViewer.setSelectable(false);
			viewer.addColumn(rowViewer);
			// If this is editable then also build up the editor
			if(isEditable){
				ColumnModelTableRowViewer rowEditor = ginInjector.createColumnModelTableRowViewer();
				ColumnModelUtils.applyColumnModelToRow(cm, rowEditor);
				rowEditor.setSelectable(true);
				editor.addColumn(rowEditor);
				this.editorRows.add(rowEditor);
			}
		}
	}

	@Override
	public List<ColumnModel> getCurrentModels() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean validateModel() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addNewColumn() {
		if(!isEditable){
			baseView.showError("This view is not editable");
		}
		// Create a new column
		ColumnModel cm = new ColumnModel();
		cm.setColumnType(ColumnType.STRING);
		cm.setMaximumSize(50L);
		// Assign an id to this column
		ColumnModelTableRowEditor rowEditor = ginInjector.createColumnModelTableRowEditor();
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
		// Pass this to the base
		baseView.showEditor();
	}

	@Override
	public void columnSelectionChanged(String columnId, Boolean isSelected) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSave() {
		// Get the models from the view and save them
		List<ColumnModel> newSchema = ColumnModelUtils.extractColumnModels(this.editorRows);
		List<String> json;
		try {
			json = tableModelUtils.toJSONList(newSchema);
			synapseClient.setTableSchema(tableId, json, new AsyncCallback<List<String>>(){

				@Override
				public void onFailure(Throwable caught) {
					editor.showError(caught.getMessage());
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
						editor.showError(e.getMessage());
					}
					
				}});
		} catch (JSONObjectAdapterException e) {
			editor.showError(e.getMessage());
		}
		
	}
}
