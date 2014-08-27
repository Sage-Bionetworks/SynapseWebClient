package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.CheckBox;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellFactory;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnTypeViewEnum;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * The UiBound implementation of RowView with zero business logic.
 * 
 * @author John
 *
 */
public class RowViewImpl implements RowView {
	
	public interface Binder extends UiBinder<TableRow, RowViewImpl> {	}
	
	@UiField
	CheckBox select;
	
	TableRow row;
	CellFactory factory;
	List<ToggleCell> cells;
	Long id;
	Long version;
	boolean isEditing;
	
	@Inject
	public RowViewImpl(Binder binder, CellFactory factory){
		row = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return row;
	}

	@Override
	public Long getID() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public Long getVersion() {
		return this.version;
	}

	@Override
	public Iterable<String> getValues() {
		List<String> values = new ArrayList<String>(cells.size());
		for(ToggleCell cell: cells){
			values.add(cell.getValue());
		}
		return values;
	}

	@Override
	public boolean isSelected() {
		return select.getValue();
	}

	@Override
	public void setEditing(boolean isEditing) {
		this.isEditing = isEditing;
		// Toggle the edit of each cell
		for(ToggleCell cell: this.cells){
			cell.toggleEdit(isEditing);
		}
	}

	@Override
	public void initializeRow(List<ColumnTypeViewEnum> types) {
		// the types determine the editors and renderer for this row.
		this.row.clear();
		this.cells = new ArrayList<ToggleCell>(types.size());
		// Setup each row
		for(ColumnTypeViewEnum type: types){
			TableData td = new TableData();
			row.add(td);
			// Create each cell
			ToggleCell cell = new ToggleCellPresenter(type, this.factory, td);
			this.cells.add(cell);
		}
	}

	@Override
	public void setRowData(Long rowId, Long version, List<String> cellValues, boolean isSelectable) {
		this.id = rowId;
		this.version = version;
		this.select.setVisible(isSelectable);
		// Fill in the values
		int index =0;
		for(String value: cellValues){
			ToggleCell cell =  this.cells.get(index);
			cell.setValue(value);
			index++;
		}
	}

	@Override
	public boolean isEditing() {
		return this.isEditing;
	}

	@Override
	public void setSelected(boolean isSelected) {
		this.select.setValue(isSelected);
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.select.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSelected(select.getValue());
			}
		});
	}

}
