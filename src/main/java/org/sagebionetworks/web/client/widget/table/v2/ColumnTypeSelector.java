package org.sagebionetworks.web.client.widget.table.v2;

import org.gwtbootstrap3.client.ui.FormControlStatic;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Wrapper for either an editable selector or non-editable static form control.
 * 
 * @author John
 *
 */
public class ColumnTypeSelector implements IsWidget{

	public static String FIXED_WIDTH = "100px";
	
	boolean isEditable;
	IsWidget widget;
	Select select;
	FormControlStatic controlStatic;
	
	public ColumnTypeSelector(ColumnTypeViewEnum type, boolean isEditable){
		this.isEditable = isEditable;
		if(isEditable){
			select = buildNewColumnTypeSelect();
			select.setValue(type.name());
			select.setWidth(FIXED_WIDTH);
			this.widget = select;
		}else{
			controlStatic = new FormControlStatic();
			controlStatic.setText(type.name());
			controlStatic.setWidth(FIXED_WIDTH);
			this.widget = controlStatic;
		}
	}

	@Override
	public Widget asWidget() {
		return widget.asWidget();
	}
	
	/**
	 * Build a new selector for a column type.
	 * @param currentType
	 * @return
	 */
	private static Select buildNewColumnTypeSelect(){
		Select select = new Select();
		for(ColumnTypeViewEnum type: ColumnTypeViewEnum.values()){
			Option op = new Option();
			op.setText(type.name());
			select.add(op);
		}
		return select;
	}

	/**
	 * Listen to select changes on this widget.
	 * @param handler
	 */
    public void addChangeHandler(final ChangeHandler handler) {
        if(isEditable){
        	select.addChangeHandler(handler);
        }
    }
    
    /**
     * Get the selected ColumnType.
     * @return
     */
    public ColumnTypeViewEnum getSelectedColumnType(){
    	String value = null;
    	if(isEditable){
    		value = select.getValue();
    	}else{
    		value = controlStatic.getText();
    	}
    	return ColumnTypeViewEnum.valueOf(value);
    }
}
