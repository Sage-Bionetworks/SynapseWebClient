package org.sagebionetworks.web.client.widget.table.v2;

import org.gwtbootstrap3.client.ui.CheckBoxButton;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelsView.ViewType;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;

/**
 * A view of a single ColumnModel.
 * 
 * @author John
 *
 */
class ColumnModelView extends TableRow {
	
	CheckBoxButton selectButton;
	String columnId;
	boolean isEditable;
	TextView nameField;
	ColumnTypeView columnTypeEditor;
	MaxSizeView columnMaxSize;
	TextView defaultField;
	
	/**
	 * Create a new 
	 * @param model
	 * @param isEditable
	 */
	public ColumnModelView(ViewType viewType, ColumnModel model, boolean isEditable) {
		this.isEditable = isEditable;
		ColumnTypeViewEnum columnTypeView = ColumnTypeViewEnum.getViewForType(model.getColumnType());
		this.columnId = model.getId();
		TableData select = new TableData();
		TableData columnName = new TableData();
		TableData columnType = new TableData();
		this.columnMaxSize = new MaxSizeView(columnTypeView, isEditable, model.getMaximumSize());
		TableData columnDefault = new TableData();
		this.add(select);
		this.add(columnName);
		this.add(columnType);
		this.add(columnMaxSize);
		this.add(columnDefault);
		// All columns are can be selected for an edtior.
		if(ViewType.EDITOR.equals(viewType)){
			// Select
			selectButton = new CheckBoxButton();
			selectButton.setType(ButtonType.LINK);
			select.add(selectButton);
		}

		// Name
		nameField = new TextView(model.getName(), isEditable);
		columnName.add(nameField);
		
		// Type
		columnTypeEditor = new ColumnTypeView(columnTypeView, isEditable);
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
	
	public ColumnModel getModel(){
		ColumnModel model = new ColumnModel();
		
		return model;
	}

}
