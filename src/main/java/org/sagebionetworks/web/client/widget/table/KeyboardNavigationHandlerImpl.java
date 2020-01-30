package org.sagebionetworks.web.client.widget.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

/**
 * This implementation keeps track of the address of each widget and listens to key down events.
 * When navigation key event occurs, the address of the source widget is used to calculate the new
 * focus widget.
 * 
 * @author jhill
 *
 */
public class KeyboardNavigationHandlerImpl implements KeyboardNavigationHandler {

	ArrayList<RowOfWidgets> rows;
	Map<IsWidget, Address> cellAddressMap;
	Map<RowOfWidgets, List<HandlerRegistration>> registrationMap;
	int columnCount;
	boolean needsRecalcualteAdressess;
	FocusSetter focusSetter;

	@Inject
	public KeyboardNavigationHandlerImpl(FocusSetter focusSetter) {
		rows = new ArrayList<RowOfWidgets>();
		cellAddressMap = new HashMap<IsWidget, Address>();
		this.registrationMap = new HashMap<RowOfWidgets, List<HandlerRegistration>>();
		this.focusSetter = focusSetter;
	}

	@Override
	public void bindRow(RowOfWidgets row) {
		// Make sure this row is not already bound.
		removeRow(row);
		// Add this row.
		rows.add(row);
		// Listen to each row
		columnCount = 0;
		List<HandlerRegistration> registration = new ArrayList<HandlerRegistration>(row.getWidgetCount());
		for (int i = 0; i < row.getWidgetCount(); i++) {
			IsWidget cell = row.getWidget(i);
			HandlerRegistration hr = bindEditor(cell);
			if (hr != null) {
				registration.add(hr);
			}
			columnCount++;
		}
		// keep track of the handler registration so we can do future cleanup.
		this.registrationMap.put(row, registration);
		needsRecalcualteAdressess = true;
	}

	/**
	 * Unbind a row. Does nothing if the row is not currently bound.
	 * 
	 * @param row
	 */
	private void unBindRow(RowOfWidgets row) {
		List<HandlerRegistration> current = this.registrationMap.remove(row);
		if (current != null) {
			for (HandlerRegistration hr : current) {
				hr.removeHandler();
			}
		}
	}

	/**
	 * Register for key down events for this widget.
	 * 
	 * @param editor
	 * @return
	 */
	private HandlerRegistration bindEditor(final IsWidget editor) {
		// Can only listen to widget that implement HasKeyDownHandlers
		if (editor instanceof HasKeyDownHandlers) {
			HasKeyDownHandlers keyDownCell = (HasKeyDownHandlers) editor;
			return keyDownCell.addKeyDownHandler(new KeyDownHandler() {
				@Override
				public void onKeyDown(KeyDownEvent event) {
					editorKeyPressed(editor, event);
				}
			});
		}
		// There is no handler if this is not an instanceof HasKeyDownHandlers
		return null;
	}

	@Override
	public void removeRow(RowOfWidgets row) {
		unBindRow(row);
		rows.remove(row);
		needsRecalcualteAdressess = true;
	}

	@Override
	public void removeAllRows() {
		// Unbind all rows
		for (RowOfWidgets row : rows) {
			unBindRow(row);
		}
		this.rows.clear();
		this.needsRecalcualteAdressess = true;
	}

	/**
	 * Recalculate the address of each cell if needed. Recalculation is needed after rows are added or
	 * removed from the table.
	 */
	private void recalculateAddressesIfNeeded() {
		if (needsRecalcualteAdressess) {
			// Start with a clean address map
			cellAddressMap.clear();
			// Walk cells and calculate their address.
			int rowIndex = 0;
			for (RowOfWidgets row : rows) {
				int columnIndex = 0;
				for (int i = 0; i < row.getWidgetCount(); i++) {
					IsWidget widget = row.getWidget(i);
					Address address = cellAddressMap.get(widget);
					if (address == null) {
						address = new Address();
						cellAddressMap.put(widget, address);
					}
					address.columnIndex = columnIndex;
					address.rowIndex = rowIndex;
					columnIndex++;
				}
				rowIndex++;
			}
		}
		needsRecalcualteAdressess = false;
	}

	/**
	 * Key pressed on this editor.
	 * 
	 * @param editor
	 * @param event
	 */
	private void editorKeyPressed(IsWidget editor, KeyDownEvent event) {
		// Check the addresses. If they are stale then they will need to be
		// recalculated before proceeding.
		recalculateAddressesIfNeeded();
		// Event Switch.
		switch (event.getNativeKeyCode()) {
			case KeyCodes.KEY_ENTER:
				onDown(editor);
				break;
			case KeyCodes.KEY_DOWN:
				onDown(editor);
				break;
			case KeyCodes.KEY_UP:
				onUp(editor);
				break;
			case KeyCodes.KEY_LEFT:
				onLeft(editor);
				break;
			case KeyCodes.KEY_RIGHT:
				onRight(editor);
				break;
		}
	}

	/**
	 * Give focus to the cell that is right of the passed cell.
	 * 
	 * @param editor
	 */
	private void onRight(IsWidget editor) {
		Address current = cellAddressMap.get(editor);
		if (current != null) {
			attemptSetFocus(current.columnIndex + 1, current.rowIndex);
		}
	}

	/**
	 * Give focus to the cell that is left of the passed cell.
	 * 
	 * @param editor
	 */
	private void onLeft(IsWidget editor) {
		Address current = cellAddressMap.get(editor);
		if (current != null) {
			attemptSetFocus(current.columnIndex - 1, current.rowIndex);
		}
	}

	/**
	 * Give focus to the cell that is bellow the passed cell.
	 * 
	 * @param editor
	 */
	private void onDown(IsWidget editor) {
		Address current = cellAddressMap.get(editor);
		if (current != null) {
			attemptSetFocus(current.columnIndex, current.rowIndex + 1);
		}
	}

	/**
	 * Give focus to the cell that is above the passed cell.
	 * 
	 * @param editor
	 */
	private void onUp(IsWidget editor) {
		Address current = cellAddressMap.get(editor);
		if (current != null) {
			attemptSetFocus(current.columnIndex, current.rowIndex - 1);
		}
	}

	/**
	 * Attempt to set the focus on the cell at the given address. If the requested address is
	 * out-of-range of the table
	 * 
	 * @param columnIndex
	 * @param rowIndex
	 */
	public void attemptSetFocus(int columnIndex, int rowIndex) {
		if (rowIndex < rows.size() && rowIndex > -1) {
			RowOfWidgets row = rows.get(rowIndex);
			if (columnIndex < row.getWidgetCount() && columnIndex > -1) {
				// set the focus on the target.
				IsWidget editor = row.getWidget(columnIndex);
				boolean shouldSelectAll = true;
				this.focusSetter.attemptSetFocus(editor, shouldSelectAll);
			}
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
			return "Address [columnIndex=" + columnIndex + ", rowIndex=" + rowIndex + "]";
		}
	}

}
