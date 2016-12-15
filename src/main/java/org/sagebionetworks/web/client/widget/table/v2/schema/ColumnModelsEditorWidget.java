package org.sagebionetworks.web.client.widget.table.v2.schema;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView.ViewType;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ColumnModelsEditorWidget implements ColumnModelsView.Presenter, ColumnModelTableRow.SelectionPresenter, IsWidget {
	public static final ColumnType DEFAULT_NEW_COLUMN_TYPE = ColumnType.STRING;
	public static final long DEFAULT_STRING_MAX_SIZE = 50L;
	
	PortalGinInjector ginInjector;
	ColumnModelsView editor;
	List<ColumnModel> startingModels;
	List<ColumnModelTableRow> editorRows;
	String tableId;
	KeyboardNavigationHandler keyboardNavigationHandler;
	Callback onAddDefaultViewColumnsCallback;
	Set<String> columnModelIds;
	CookieProvider cookies;
	/*
	 * Set to true to indicate that change selections are in progress.  This allows selection change events to be ignored during this period.
	 */
	boolean changingSelection = false;
	
	public static List<ColumnModel> nonEditableColumns = null;
	public SynapseClientAsync synapseClient;
	public AdapterFactory adapterFactory;
	@Inject
	public ColumnModelsEditorWidget(PortalGinInjector ginInjector, SynapseClientAsync synapseClient, AdapterFactory adapterFactory) {
		columnModelIds = new HashSet<String>();
		this.ginInjector = ginInjector;
		this.editor = ginInjector.createNewColumnModelsView();
		this.editor.setPresenter(this);
		this.editorRows = new LinkedList<ColumnModelTableRow>();
		this.synapseClient = synapseClient;
		this.adapterFactory = adapterFactory;
		cookies = ginInjector.getCookieProvider();
	}
	
	public void configure(List<ColumnModel> startingModels) {
		this.changingSelection = false;
		this.startingModels = startingModels;
		keyboardNavigationHandler = ginInjector.createKeyboardNavigationHandler();
		resetEditor();
	}
	
	@Override
	public List<ColumnModel> getEditedColumnModels() {
		return ColumnModelUtils.extractColumnModels(this.editorRows);
	}

	@Override
	public Widget asWidget() {
		return editor.asWidget();
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
		return createColumnModelEditorWidget(cm);
	}
	
	private ColumnModelTableRowEditorWidget createColumnModelEditorWidget(ColumnModel cm) {
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
	
	private ColumnModelTableRowViewer createColumnModelViewerWidget(ColumnModel cm) {
		ColumnModelTableRowViewer rowViewer = ginInjector.createNewColumnModelTableRowViewer();
		ColumnModelUtils.applyColumnModelToRow(cm, rowViewer);
		rowViewer.setSelectable(true);
		rowViewer.setSelectionPresenter(this);
		editor.addColumn(rowViewer);
		this.editorRows.add(rowViewer);
		checkSelectionState();
		return rowViewer;
	}
	
	/**
	 * Reset the editor.
	 */
	private void resetEditor(){
		columnModelIds.clear();
		// clear the current navigation editor
		this.keyboardNavigationHandler.removeAllRows();
		this.editorRows.clear();
		editor.configure(ViewType.EDITOR, true);
		addColumns(this.startingModels);
	}
	
	public void addColumns(final List<ColumnModel> models) {
		if (nonEditableColumns == null) {
			initNonEditableColumns(new Callback() {
				@Override
				public void invoke() {
					addColumnsAfterInit(models);					
				}
			});
		} else {
			addColumnsAfterInit(models);	
		}
	}
	
	public void initNonEditableColumns(final Callback callback) {
		synapseClient.getDefaultColumnsForView(org.sagebionetworks.repo.model.table.ViewType.file, new AsyncCallback<List<ColumnModel>>() {
			@Override
			public void onFailure(Throwable caught) {
				editor.showErrorMessage(caught.getMessage());
			}
			@Override
			public void onSuccess(List<ColumnModel> columns) {
				nonEditableColumns = new ArrayList<ColumnModel>();
				for (ColumnModel cm : columns) {
					nonEditableColumns.add(cm);
				}
				callback.invoke();
			}
		});
	}
	
	public void addColumnsAfterInit(final List<ColumnModel> models) {
		List<ColumnModel> existingColumns = getEditedColumnModels();
		for (ColumnModel cm : existingColumns) {
			cm.setId(null);
			if (models.contains(cm)) {
				models.remove(cm);
			}
		}
		for(ColumnModel cm: models){
			String columnModelId = cm.getId();
			if (columnModelId == null || !columnModelIds.contains(columnModelId)) {
				// default column model ids are cleared on the servlet.
				ColumnModel cmCopy = copyColumnModel(cm);
				cmCopy.setId(null);
				if (!nonEditableColumns.contains(cmCopy)) {
					createColumnModelEditorWidget(cm);	
				} else {
					createColumnModelViewerWidget(cm);
				}
				columnModelIds.add(columnModelId);
			}
		}
		checkSelectionState();
	}
	
	private ColumnModel copyColumnModel(ColumnModel cm) {
		try {
			return new ColumnModel(cm.writeToJSONObject(adapterFactory.createNew()));
		} catch (JSONObjectAdapterException e) {
			editor.showErrorMessage(e.getMessage());
			return null;
		}
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
		// delete all selected rows
		Iterator<ColumnModelTableRow> it = editorRows.iterator();
		while(it.hasNext()){
			ColumnModelTableRow row = it.next();
			if(row.isSelected()){
				row.delete();
				it.remove();
				columnModelIds.remove(row.getId());
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
	
	public static class SelectedRow{
		public ColumnModelTableRow row;
		public int index;
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

	/**
	 * Validate each editor.
	 * @return
	 */
	public boolean validate() {
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
	
	public void setAddDefaultViewColumnsButtonVisible(boolean visible) {
		editor.setAddDefaultViewColumnsButtonVisible(visible);
	}
	
	public void setOnAddDefaultViewColumnsCallback(Callback onAddDefaultViewColumnsCallback) {
		this.onAddDefaultViewColumnsCallback = onAddDefaultViewColumnsCallback;
	}
	
	@Override
	 public void onAddDefaultViewColumns() {
		if (onAddDefaultViewColumnsCallback != null) {
			onAddDefaultViewColumnsCallback.invoke();
		}
	 }
}
