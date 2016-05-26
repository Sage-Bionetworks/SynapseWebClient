package org.sagebionetworks.web.client.widget.table.modal.fileview;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizard.TableType;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRow;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowViewer;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelUtils;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView.ViewType;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsWidget;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Edit column models page
 * 
 * @author Jay
 *
 */
public class CreateTableViewWizardStep2 implements ColumnModelsView.Presenter, ColumnModelTableRow.SelectionPresenter, ModalPage, IsWidget {
	ColumnModelsView editor;
	PortalGinInjector ginInjector;
	SynapseClientAsync synapseClient;
	String tableId;
	List<ColumnModel> startingModels;
	List<ColumnModelTableRow> editorRows;
	KeyboardNavigationHandler keyboardNavigationHandler;
	ModalPresenter presenter;
	// the TableEntity or View
	Entity entity;
	TableType tableType;
	
	/*
	 * Set to true to indicate that change selections are in progress.  This allows selection change events to be ignored during this period.
	 */
	boolean changingSelection = false;
	/**
	 * New presenter with its view.
	 * @param view
	 */
	@Inject
	public CreateTableViewWizardStep2(PortalGinInjector ginInjector, SynapseClientAsync synapseClient){
		this.ginInjector = ginInjector;
		this.editor = ginInjector.createNewColumnModelsView();
		this.editor.setPresenter(this);
		this.synapseClient = synapseClient;
		this.editorRows = new LinkedList<ColumnModelTableRow>();
		keyboardNavigationHandler = ginInjector.createKeyboardNavigationHandler();
	}

	public void configure(Entity entity, TableType tableType) {
		configure(entity, tableType, new ArrayList<ColumnModel>());
	}
	
	public void configure(Entity entity, TableType tableType, List<ColumnModel> startingModels) {
		this.changingSelection = false;
		this.entity = entity;
		this.tableType = tableType;
		this.startingModels = startingModels;
		resetEditor();
	}

	@Override
	public List<ColumnModel> getEditedColumnModels() {
		return ColumnModelUtils.extractColumnModels(this.editorRows);
	}

	@Override
	public boolean validateModel() {
		return false;
	}

	@Override
	public ColumnModelTableRowEditorWidget addNewColumn() {
		// Create a new column
		ColumnModel cm = new ColumnModel();
		cm.setColumnType(ColumnModelsWidget.DEFAULT_NEW_COLUMN_TYPE);
		cm.setMaximumSize(ColumnModelsWidget.DEFAULT_STRING_MAX_SIZE);
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
		return editor.asWidget();
	}

	@Override
	public void onEditColumns() {
		// reset the editor to the starting state
		resetEditor();
	}
	
	/**
	 * Reset the editor.
	 */
	public void resetEditor(){
		// clear the current navigation editor
		if(this.keyboardNavigationHandler != null){
			this.keyboardNavigationHandler.removeAllRows();
		}
		this.editorRows.clear();
		editor.configure(ViewType.EDITOR, true);
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
	public void setModalPresenter(ModalPresenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void onPrimary() {
		presenter.setLoading(true);
		// Save it the data is valid
		if(!validate()){
			presenter.setErrorMessage(ColumnModelsWidget.SEE_THE_ERROR_S_ABOVE);
			presenter.setLoading(false);
			return;
		}
		// Get the models from the view and save them
		List<ColumnModel> newSchema = getEditedColumnModels();
		synapseClient.setTableSchema(entity, newSchema, new AsyncCallback<Void>(){
			@Override
			public void onFailure(Throwable caught) {
				presenter.setErrorMessage(caught.getMessage());
			}
			
			@Override
			public void onSuccess(Void result) {
				presenter.onFinished();
				
//				TODO: go to next page if view?  Can decide based on tableType
//				presenter.setLoading(false);
//				presenter.setNextActivePage(next);
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
