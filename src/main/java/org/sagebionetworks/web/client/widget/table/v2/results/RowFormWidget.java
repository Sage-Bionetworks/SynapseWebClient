package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.Cell;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellFactory;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.TakesAddressCell;
import org.sagebionetworks.web.shared.table.CellAddress;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A widget for creating a new single row of a table entity (gather values as a form).
 *
 * @author Jay
 *
 */
public class RowFormWidget implements IsWidget, RowFormView.Presenter, KeyboardNavigationHandler.RowOfWidgets {
	
	RowFormView view;
	List<Cell> cells;
	CellFactory cellFactory;
	
	@Inject
	public RowFormWidget(RowFormView view, CellFactory cellFactory){
		this.view = view;
		this.cellFactory = cellFactory;
		view.setPresenter(this);
	}
	
	/**
	 * Configure this row with new row data.
	 * @param types The types determines the cells types for this row.
	 */
	public void configure(String tableId, List<ColumnModel> types){
		this.cells = new ArrayList<Cell>(types.size());
		// Setup each cell
		for(ColumnModel type: types){
			// Create each cell
			Cell cell = cellFactory.createFormEditor(type);
			this.cells.add(cell);
			this.view.addCell(type.getName(), cell);
			// Pass the address to cells the need it.
			if(cell instanceof TakesAddressCell){
				TakesAddressCell takesAddress = (TakesAddressCell) cell;
				takesAddress.setCellAddresss(new CellAddress(tableId, type, null, null));
			}
		}
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	/**
	 * Extract the row data from this widget.
	 * @return
	 */
	public Row getRow(){
		// Pull the values from the cells.
		List<String> values = new ArrayList<String>(this.cells.size());
		for(Cell cell: cells){
			values.add(cell.getValue());
		}
		// Create the row.
		Row row = new Row();
		row.setValues(values);
		return row;
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
	 * Is this row valid?
	 * Note: This must only be called on an editor.
	 * @return
	 */
	public boolean isValid() {
		boolean valid = true;
		for(Cell cell: cells){
			if(!((CellEditor)cell).isValid()){
				valid = false;
			}
		}
		return valid;
	}
	
	public void clear() {
		view.clear();
	}

}
