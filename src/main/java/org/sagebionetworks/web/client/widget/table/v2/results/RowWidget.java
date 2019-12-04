package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.client.widget.table.modal.fileview.ViewDefaultColumns;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.Cell;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellFactory;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.TakesAddressCell;
import org.sagebionetworks.web.shared.table.CellAddress;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A widget for a single row of a table entity.
 *
 * @author John
 *
 */
public class RowWidget implements IsWidget, RowView.Presenter, KeyboardNavigationHandler.RowOfWidgets {

	RowView view;
	RowSelectionListener rowSelectionListener;
	List<Cell> cells;
	CellFactory cellFactory;
	Long rowId;
	Long rowVersion;
	ViewDefaultColumns fileViewDefaultColumns;

	@Inject
	public RowWidget(RowView view, CellFactory cellFactory, ViewDefaultColumns fileViewDefaultColumns) {
		this.view = view;
		this.cellFactory = cellFactory;
		this.fileViewDefaultColumns = fileViewDefaultColumns;
		view.setPresenter(this);
	}

	/**
	 * Configure this row with new row data.
	 * 
	 * @param types The types determines the cells types for this row.
	 * @param isEditor When true this row will be setup with editors. When false renderer will be used.
	 * @param row The row contains the data for this row.
	 * @param rowSelectionListener A listener to row selection changes. When null, the row will not be
	 *        selectable.
	 */
	public void configure(final String tableId, final List<ColumnModel> types, final boolean isEditor, TableType tableType, final Row row, RowSelectionListener rowSelectionListener) {
		this.rowSelectionListener = rowSelectionListener;
		this.view.setSelectVisible(rowSelectionListener != null && TableType.table.equals(tableType));
		this.rowId = row.getRowId();
		this.rowVersion = row.getVersionNumber();
		this.cells = new ArrayList<Cell>(types.size());
		configureAfterInit(tableId, types, isEditor, tableType, row);
	}

	private void configureAfterInit(String tableId, List<ColumnModel> types, boolean isEditor, TableType tableType, Row row) {
		// Setup each cell
		List<ColumnModel> defaultColumns = null;
		if (isEditor) {
			boolean clearIds = false;
			defaultColumns = fileViewDefaultColumns.getDefaultViewColumns(tableType.isIncludeFiles(), clearIds);
		}
		for (ColumnModel type : types) {
			// Create each cell
			Cell cell = null;
			if (isEditor && (TableType.table.equals(tableType) || !defaultColumns.contains(type))) {
				cell = cellFactory.createEditor(type);
			} else {
				cell = cellFactory.createRenderer(type);
			}
			this.cells.add(cell);
			this.view.addCell(cell);
			// Pass the address to cells the need it.
			if (cell instanceof TakesAddressCell) {
				TakesAddressCell takesAddress = (TakesAddressCell) cell;
				takesAddress.setCellAddresss(new CellAddress(tableId, type, rowId, rowVersion, tableType));
			}
		}
		// Set each cell with the data from the row.
		if (row.getValues() != null) {
			for (int i = 0; i < row.getValues().size(); i++) {
				Cell cell = cells.get(i);
				String value = row.getValues().get(i);
				cell.setValue(value);
			}
		}
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}


	@Override
	public void onSelectionChanged() {
		// Nothing to do unless we were passed a listener
		if (this.rowSelectionListener != null) {
			this.rowSelectionListener.onSelectionChanged();
		}
	}

	/**
	 * Extract the row data from this widget.
	 * 
	 * @return
	 */
	public Row getRow() {
		// Pull the values from the cells.
		List<String> values = new ArrayList<String>(this.cells.size());
		for (Cell cell : cells) {
			values.add(cell.getValue());
		}
		// Create the row.
		Row row = new Row();
		row.setRowId(this.rowId);
		row.setVersionNumber(this.rowVersion);
		row.setValues(values);
		return row;
	}

	/**
	 * Is this row selected?
	 * 
	 * @return
	 */
	public boolean isSelected() {
		return view.isSelected();
	}

	/**
	 * Select (true) or de-select (false) this row.
	 * 
	 * @param isSelected
	 */
	public void setSelected(boolean isSelected) {
		view.setSelected(isSelected);
	}

	@Override
	public IsWidget getWidget(int index) {
		return cells.get(index);
	}

	@Override
	public int getWidgetCount() {
		return cells.size();
	}

	/**
	 * Is this row valid? Note: This must only be called on an editor.
	 * 
	 * @return
	 */
	public boolean isValid() {
		boolean valid = true;
		for (Cell cell : cells) {
			if (cell instanceof CellEditor && !((CellEditor) cell).isValid()) {
				valid = false;
			}
		}
		return valid;
	}

}
