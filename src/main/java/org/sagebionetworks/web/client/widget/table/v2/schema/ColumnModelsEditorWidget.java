package org.sagebionetworks.web.client.widget.table.v2.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.client.widget.table.modal.fileview.ViewDefaultColumns;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView.ViewType;
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
	Callback onAddDefaultViewColumnsCallback, onAddAnnotationColumnsCallback;
	Set<String> columnModelIds;
	CookieProvider cookies;

	/*
	 * Set to true to indicate that change selections are in progress. This allows selection change
	 * events to be ignored during this period.
	 */
	boolean changingSelection = false;
	ViewDefaultColumns fileViewDefaultColumns;
	TableType tableType;

	public AdapterFactory adapterFactory;
	public ImportTableViewColumnsButton addTableViewColumnsButton;

	@Inject
	public ColumnModelsEditorWidget(PortalGinInjector ginInjector, AdapterFactory adapterFactory, ViewDefaultColumns fileViewDefaultColumns) {
		columnModelIds = new HashSet<String>();
		this.ginInjector = ginInjector;
		this.editor = ginInjector.createNewColumnModelsView();
		this.fileViewDefaultColumns = fileViewDefaultColumns;
		this.editor.setPresenter(this);
		this.editorRows = new LinkedList<ColumnModelTableRow>();
		this.adapterFactory = adapterFactory;
		this.addTableViewColumnsButton = ginInjector.getImportTableViewColumnsButton();
		editor.addButton(addTableViewColumnsButton);
		cookies = ginInjector.getCookieProvider();
		addTableViewColumnsButton.configure(new CallbackP<List<ColumnModel>>() {
			@Override
			public void invoke(List<ColumnModel> columns) {
				addColumns(columns);
			}
		});
	}

	public void configure(TableType tableType, List<ColumnModel> startingModels) {
		this.changingSelection = false;
		this.startingModels = startingModels;
		this.tableType = tableType;
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
		if (this.keyboardNavigationHandler != null) {
			this.keyboardNavigationHandler.bindRow(rowEditor);
		}
		if (!TableType.table.equals(tableType)) {
			rowEditor.setCanHaveDefault(false);
			if (getDefaultColumnNames().contains(cm.getName())) {
				rowEditor.setToBeDefaultFileViewColumn();
			}
		}
		editor.addColumn(rowEditor);
		this.editorRows.add(rowEditor);
		rowEditor.configure(cm, this);
		checkSelectionState();
		return rowEditor;
	}

	/**
	 * Reset the editor.
	 */
	private void resetEditor() {
		columnModelIds.clear();
		// clear the current navigation editor
		this.keyboardNavigationHandler.removeAllRows();
		this.editorRows.clear();
		editor.configure(ViewType.EDITOR, true);
		addColumns(this.startingModels);
	}

	private Set<String> getDefaultColumnNames() {
		return fileViewDefaultColumns.getDefaultViewColumnNames(tableType.isIncludeFiles());
	}

	public void addColumns(List<ColumnModel> models) {
		List<ColumnModel> newColumns = new ArrayList<ColumnModel>(models.size());
		newColumns.addAll(models);
		List<ColumnModel> existingColumns = getEditedColumnModels();
		Set<ColumnModelKey> existingColumnNames = new HashSet<ColumnModelKey>();
		Map<ColumnModelKey, ColumnModel> newModels = new HashMap<ColumnModelKey, ColumnModel>();
		for (ColumnModel cm : newColumns) {
			ColumnModelKey key = new ColumnModelKey(cm.getName(), cm.getColumnType());
			newModels.put(key, cm);
		}
		for (ColumnModel cm : existingColumns) {
			ColumnModelKey key = new ColumnModelKey(cm.getName(), cm.getColumnType());
			existingColumnNames.add(key);
		}
		for (ColumnModelKey newModelName : newModels.keySet()) {
			if (existingColumnNames.contains(newModelName)) {
				newColumns.remove(newModels.get(newModelName));
			}
		}

		for (ColumnModel cm : newColumns) {
			String columnModelId = cm.getId();
			if (columnModelId == null || !columnModelIds.contains(columnModelId)) {
				createColumnModelEditorWidget(cm);
				columnModelIds.add(columnModelId);
			}
		}
		checkSelectionState();
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
		int newInex = toMove.index - 1;
		this.editorRows.add(newInex, toMove.row);
		this.editor.moveColumn(toMove.row, newInex);
		checkSelectionState();
	}

	@Override
	public void onMoveDown() {
		SelectedRow toMove = findFirstSelected();
		this.editorRows.remove(toMove.index);
		int newInex = toMove.index + 1;
		this.editorRows.add(newInex, toMove.row);
		this.editor.moveColumn(toMove.row, newInex);
		checkSelectionState();
	}

	@Override
	public void deleteSelected() {
		// delete all selected rows
		Iterator<ColumnModelTableRow> it = editorRows.iterator();
		while (it.hasNext()) {
			ColumnModelTableRow row = it.next();
			if (row.isSelected()) {
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
	 * 
	 * @return
	 */
	private SelectedRow findFirstSelected() {
		int index = 0;
		for (ColumnModelTableRow row : editorRows) {
			if (row.isSelected()) {
				return new SelectedRow(row, index);
			}
			index++;
		}
		throw new IllegalStateException("Nothing selected");
	}

	public static class SelectedRow {
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
	private void changeAllSelection(boolean select) {
		try {
			changingSelection = true;
			// Select all
			for (ColumnModelTableRow row : editorRows) {
				row.setSelected(select);
			}
		} finally {
			changingSelection = false;
		}
		checkSelectionState();
	}

	/**
	 * The current selection state determines which buttons are enabled.
	 */
	private void checkSelectionState() {
		if (!changingSelection) {
			int index = 0;
			int count = 0;
			int lastIndex = 0;
			for (ColumnModelTableRow row : editorRows) {
				if (row.isSelected()) {
					count++;
					lastIndex = index;
				}
				index++;
			}
			editor.setCanDelete(count > 0);
			editor.setCanMoveUp(count == 1 && lastIndex > 0);
			editor.setCanMoveDown(count == 1 && lastIndex < editorRows.size() - 1);
		}
	}

	/**
	 * Are any of the rows selected?
	 * 
	 * @return
	 */
	private boolean anyRowsSelected() {
		for (ColumnModelTableRow row : editorRows) {
			if (row.isSelected()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Validate each editor.
	 * 
	 * @return
	 */
	public boolean validate() {
		// Validate each editor row
		boolean valid = true;
		for (ColumnModelTableRow editor : editorRows) {
			if (editor instanceof ColumnModelTableRowEditorWidget) {
				ColumnModelTableRowEditorWidget widget = (ColumnModelTableRowEditorWidget) editor;
				if (!widget.validate()) {
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

	public void setAddAnnotationColumnsButtonVisible(boolean visible) {
		editor.setAddAnnotationColumnsButtonVisible(visible);
	}

	public void setOnAddAnnotationColumnsCallback(Callback onAddAnnotationColumnsCallback) {
		this.onAddAnnotationColumnsCallback = onAddAnnotationColumnsCallback;
	}

	@Override
	public void onAddAnnotationColumns() {
		if (onAddAnnotationColumnsCallback != null) {
			onAddAnnotationColumnsCallback.invoke();
		}
	}
}
