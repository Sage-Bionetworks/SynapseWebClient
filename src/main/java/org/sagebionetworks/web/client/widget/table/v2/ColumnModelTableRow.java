package org.sagebionetworks.web.client.widget.table.v2;

import org.gwtbootstrap3.client.ui.CheckBoxButton;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelsView.ViewType;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * A ColumnModel rendered as a table row <tr>
 * 
 * @author John
 *
 */
public class ColumnModelTableRow extends TableRow {
	
	CheckBoxButton selectButton;
	String columnId;
	boolean isEditable;
	TextView nameField;
	ColumnTypeSelector columnTypeEditor;
	MaxSizeView columnMaxSize;
	TextView defaultField;
	
	/**
	 * Create a new 
	 * @param model
	 * @param isEditable
	 */
	public ColumnModelTableRow(String id, ViewType viewType, ColumnModel model, boolean isEditable) {
		this.setId(id);
		this.isEditable = isEditable;
		ColumnTypeViewEnum columnTypeView = ColumnTypeViewEnum.getViewForType(model.getColumnType());
		this.columnId = model.getId();
		TableData select = new TableData();
		TableData columnName = new TableData();
		TableData columnType = new TableData();
		this.columnMaxSize = new MaxSizeView(columnTypeView, isEditable, model.getMaximumSize());
		TableData columnDefault = new TableData();
		TableData controlls = new TableData();
		this.add(select);
		this.add(columnName);
		this.add(columnType);
		this.add(columnMaxSize);
		this.add(columnDefault);
		this.add(controlls);
		// All columns are can be selected for an editor.
		// Select
		selectButton = new CheckBoxButton();
		selectButton.setType(ButtonType.LINK);
		if(ViewType.EDITOR.equals(viewType)){
			// Only add it if it is an editor
			select.add(selectButton);
		}

		// Name
		nameField = new TextView(model.getName(), isEditable);
		columnName.add(nameField);
		
		// Type
		columnTypeEditor = new ColumnTypeSelector(columnTypeView, isEditable);
		columnType.add(columnTypeEditor);
		columnTypeEditor.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				// Get the current type
				ColumnTypeViewEnum newType = columnTypeEditor.getSelectedColumnType();
				columnMaxSize.onTypeChanged(newType);
			}
		});
		// default
		defaultField = new TextView(model.getDefaultValue(), isEditable);
		columnDefault.add(defaultField);
	}
	
	/**
	 * Extract all of the data from the view and put it into an new instances of ColumnModel.
	 * @return
	 */
	public ColumnModel getColumnModel(){
		ColumnModel model = new ColumnModel();
		model.setName(emptyAsNull(nameField.getText()));
		model.setId(this.columnId);
		model.setColumnType(columnTypeEditor.getSelectedColumnType().getType());
		String maxString = emptyAsNull(columnMaxSize.getText());
		if(maxString != null){
			model.setMaximumSize(Long.parseLong(maxString));
		}
		model.setDefaultValue(emptyAsNull(defaultField.getText()));
		return model;
	}
	
	/**
	 * Helper to treat empty strings as null
	 * @param in
	 * @return
	 */
	private static String emptyAsNull(String in){
		if(in == null) return null;
		if("".equals(in.trim())) return null;
		return in;
	}
	
	/**
	 * Set the select state of this row.
	 * @param selected
	 */
	public void select(boolean selected){
		selectButton.setActive(selected);
	}
	
	/**
	 * Listen to selection changes.
	 * @param hanlder
	 * @return
	 */
	public HandlerRegistration addSelectionListener(ValueChangeHandler<Boolean> hanlder){
		return selectButton.addValueChangeHandler(hanlder);
	}
	/**
	 * Is this row selected?
	 * @return
	 */
	public boolean isSelected(){
		return selectButton.isActive();
	}
	

}
