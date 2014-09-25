package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.web.client.widget.table.v2.results.cell.Cell;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;

/**
 * This implementation currently uses brute force to find the address of a
 * 
 * @author jhill
 *
 */
public class EditorNavigationHandlerImpl implements EditorNavigationHandler {

	ArrayList<TableRow> rows;
	Map<CellEditor, Address> cellAddressMap;
	int columnCount;

	public EditorNavigationHandlerImpl() {
		rows = new ArrayList<TableRow>();
		cellAddressMap = new HashMap<CellEditor, Address>();
	}

	@Override
	public void bindRow(TableRow row) {
		// Add this row.
		rows.add(row);
		// Listen to each row
		columnCount = 0;
		for (Cell cell : row.getCells()) {
			CellEditor editor = (CellEditor) cell;
			bindEditor(editor);
			columnCount++;
		}
	}

	private void bindEditor(final CellEditor editor) {
		editor.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				editorKeyPressed(editor, event);
			}
		});
	}

	@Override
	public void removeRow(TableRow row) {
		rows.remove(row);
	}

	@Override
	public void recalculateAddresses() {
		// Walk cells and calculate their address.
		int rowIndex = 0;
		for (TableRow row : rows) {
			int columnIndex = 0;
			for (Cell cell : row.getCells()) {
				CellEditor editor = (CellEditor) cell;
				Address address = cellAddressMap.get(editor);
				if (address == null) {
					address = new Address();
					cellAddressMap.put(editor, address);
				}
				address.columnIndex = columnIndex;
				address.rowIndex = rowIndex;
				columnIndex++;
			}
			rowIndex++;
		}
	}

	/**
	 * The address of a cell in the table.
	 *
	 */
	private static class Address {
		int columnIndex;
		int rowIndex;
		@Override
		public String toString() {
			return "Address [columnIndex=" + columnIndex + ", rowIndex="
					+ rowIndex + "]";
		}
	}

	/**
	 * Key pressed on this editor.
	 * 
	 * @param editor
	 * @param event
	 */
	private void editorKeyPressed(CellEditor editor, KeyPressEvent event) {
		switch (event.getNativeEvent().getKeyCode()) {
		case KeyCodes.KEY_ENTER:
			onDown(editor);
			break;
		case KeyCodes.KEY_DOWN:
			onDown(editor);
			break;
		case KeyCodes.KEY_UP:
			onUp(editor);
			break;			
		}
	}

	private void onDown(CellEditor editor) {
		Address current = cellAddressMap.get(editor);
		focusTo(current.columnIndex, current.rowIndex+1);
	}
	
	private void onUp(CellEditor editor) {
		Address current = cellAddressMap.get(editor);
		focusTo(current.columnIndex, current.rowIndex-1);
	}

	public void focusTo(int columnIndex, int rowIndex) {
		if (rowIndex < rows.size() && rowIndex > -1) {
			TableRow row = rows.get(rowIndex);
			if (columnIndex < row.getCellCount() && columnIndex > -1) {
				CellEditor editor = (CellEditor) row.getCell(columnIndex);
				setFocus(editor);
			}
		}
	}

	private void setFocus(final CellEditor widget) {
//		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
//			@Override
//			public void execute() {
				widget.setFocus(true);
//			}
//		});
	}

}
