package org.sagebionetworks.web.client.widget.table.v2.schema;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler;
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
public class ColumnModelsWidget implements ColumnModelsView.Presenter, ColumnModelsViewBase.Presenter, ColumnModelTableRow.SelectionPresenter, SynapseWidgetPresenter{
	
	public static final String SEE_THE_ERROR_S_ABOVE = "See the error(s) above.";
	public static final ColumnType DEFAULT_NEW_COLUMN_TYPE = ColumnType.STRING;
	public static final long DEFAULT_STRING_MAX_SIZE = 50L;
	PortalGinInjector ginInjector;
	ColumnModelsViewBase baseView;
	ColumnModelsView viewer;
	ColumnModelsView editor;
	boolean isEditable;
	SynapseClientAsync synapseClient;
	String tableId;
	List<ColumnModel> startingModels;
	List<ColumnModelTableRow> editorRows;
	EntityBundle bundle;
	EntityUpdatedHandler updateHandler;
	KeyboardNavigationHandler keyboardNavigationHandler;
	
	/*
	 * Set to true to indicate that change selections are in progress.  This allows selection change events to be ignored during this period.
	 */
	boolean changingSelection = false;
	/**
	 * New presenter with its view.
	 * @param view
	 */
	@Inject
	public ColumnModelsWidget(ColumnModelsViewBase baseView, PortalGinInjector ginInjector, SynapseClientAsync synapseClient){
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
		this.editorRows = new LinkedList<ColumnModelTableRow>();
	}

	@Override
	public void configure(EntityBundle bundle, boolean isEditable, EntityUpdatedHandler updateHandler) {
		this.changingSelection = false;
		this.isEditable = isEditable;
		this.bundle = bundle;
		this.startingModels = bundle.getTableBundle().getColumnModels();
		this.updateHandler = updateHandler;
		viewer.configure(ViewType.VIEWER, this.isEditable);
		for(ColumnModel cm: this.startingModels){
			// Create a viewer
			ColumnModelTableRowViewer rowViewer = ginInjector.createNewColumnModelTableRowViewer();
			ColumnModelUtils.applyColumnModelToRow(cm, rowViewer);
			rowViewer.setSelectable(false);
			viewer.addColumn(rowViewer);
		}
		if(isEditable){
			keyboardNavigationHandler = ginInjector.createKeyboardNavigationHandler();
		}else{
			keyboardNavigationHandler = null;
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
	public ColumnModelTableRowEditorWidget addNewColumn() {
		// Create a new column
		ColumnModel cm = new ColumnModel();
		cm.setColumnType(DEFAULT_NEW_COLUMN_TYPE);
		cm.setMaximumSize(DEFAULT_STRING_MAX_SIZE);
		// Assign an id to this column
		ColumnModelTableRowEditorWidget rowEditor = ginInjector.createColumnModelEditorWidget();
		// bind this row for navigation.
		if(this.keyboardNavigationHandler != null){
			this.keyboardNavigationHandler.bindRow(rowEditor);
		}
		editor.addColumn(rowEditor);
		this.editorRows.add(rowEditor);
		rowEditor.configure(cm, this);
		checkSelectionState();
		return rowEditor;
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
	 * Reset the editor.
	 */
	private void resetEditor(){
		// clear the current navigation editor
		if(this.keyboardNavigationHandler != null){
			this.keyboardNavigationHandler.removeAllRows();
		}
		this.editorRows.clear();
		editor.configure(ViewType.EDITOR, this.isEditable);
		for(ColumnModel cm: this.startingModels){
			ColumnModelTableRowViewer rowViewer = ginInjector.createNewColumnModelTableRowViewer();
			ColumnModelUtils.applyColumnModelToRow(cm, rowViewer);
			rowViewer.setSelectable(true);
			rowViewer.setSelectionPresenter(this);
			editor.addColumn(rowViewer);
			this.editorRows.add(rowViewer);
		}
		checkSelectionState();
	}

	@Override
	public void onSave() {
		// Save it the data is valid
		if(!validate()){
			baseView.showError(SEE_THE_ERROR_S_ABOVE);
			return;
		}else{
			baseView.hideErrors();
		}
		// Get the models from the view and save them
		List<ColumnModel> newSchema = getEditedColumnModels();
		baseView.setLoading();
		TableEntity entity = (TableEntity) this.bundle.getEntity();
		synapseClient.setTableSchema(entity, newSchema, new AsyncCallback<Void>(){

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
	
	/**
	 * Validate each editor.
	 * @return
	 */
	private boolean validate(){
		// Validate each editor row
		boolean valid = true;
		for(ColumnModelTableRow editor: editorRows){
			if(editor instanceof ColumnModelTableRowEditorWidget){
				ColumnModelTableRowEditorWidget widget = (ColumnModelTableRowEditorWidget) editor;
				if(!widget.validate()){
					valid = false;
				}
			}
		}
		return valid;
	}

	@Override
	public void toggleSelect() {
		changeAllSelection(!anyRowsSelected());
	}

	@Override
	public void selectAll() {
		changeAllSelection(true);
	}

	@Override
	public void selectNone() {
		changeAllSelection(false);
	}


	@Override
	public void onMoveUp() {
		SelectedRow toMove = findFirstSelected();
		this.editorRows.remove(toMove.row);
		int newInex = toMove.index-1;
		this.editorRows.add(newInex, toMove.row);
		this.editor.moveColumn(toMove.row, newInex);
		checkSelectionState();
	}

	@Override
	public void onMoveDown() {
		SelectedRow toMove = findFirstSelected();
		this.editorRows.remove(toMove.index);
		int newInex = toMove.index+1;
		this.editorRows.add(newInex, toMove.row);
		this.editor.moveColumn(toMove.row, newInex);
		checkSelectionState();
	}

	@Override
	public void deleteSelected() {
		// Select all 
		Iterator<ColumnModelTableRow> it = editorRows.iterator();
		while(it.hasNext()){
			ColumnModelTableRow row = it.next();
			if(row.isSelected()){
				row.delete();
				it.remove();
			}
		}
		// Check the selection state when done.
		checkSelectionState();
	}

	/**
	 * Find the first selected row.
	 * @return
	 */
	private SelectedRow findFirstSelected(){
		int index = 0;
		for(ColumnModelTableRow row: editorRows){
			if(row.isSelected()){
				return new SelectedRow(row, index);
			}
			index++;
		}
		throw new IllegalStateException("Nothing selected");
	}
	
	private static class SelectedRow{
		ColumnModelTableRow row;
		int index;
		public SelectedRow(ColumnModelTableRow row, int index) {
			super();
			this.row = row;
			this.index = index;
		}
	}
	
	@Override
	public void selectionChanged(boolean isSelected) {
		checkSelectionState();
	}
	
	/**
	 * Change the selection state of all rows to the passed value.
	 * 
	 * @param select
	 */
	private void changeAllSelection(boolean select){
		try{
			changingSelection = true;
			// Select all 
			for(ColumnModelTableRow row: editorRows){
				row.setSelected(select);
			}
		}finally{
			changingSelection = false;
		}
		checkSelectionState();
	}
	
	/**
	 * The current selection state determines which buttons are enabled.
	 */
	private void checkSelectionState(){
		if(!changingSelection){
			int index = 0;
			int count = 0;
			int lastIndex = 0;
			for(ColumnModelTableRow row: editorRows){
				if(row.isSelected()){
					count++;
					lastIndex = index;
				}
				index++;
			}
			editor.setCanDelete(count > 0);
			editor.setCanMoveUp(count == 1 && lastIndex > 0);
			editor.setCanMoveDown(count == 1 && lastIndex < editorRows.size()-1);
		}
	}
	
	/**
	 * Are any of the rows selected?
	 * @return
	 */
	private boolean anyRowsSelected(){
		for(ColumnModelTableRow row: editorRows){
			if(row.isSelected()){
				return true;
			}
		}
		return false;
	}
}
