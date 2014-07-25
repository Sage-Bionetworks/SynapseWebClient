package org.sagebionetworks.web.client.widget.table.v2;

import org.gwtbootstrap3.client.ui.FormControlStatic;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;

/**
 * A column max size view changes based on the column type.
 * @author John
 *
 */
public class MaxSizeView extends TableData {
	
	ColumnTypeViewEnum type;
	boolean isEditable;
	TextBox sizeEditor;
	Long maxSize;
	
	
	public MaxSizeView(ColumnTypeViewEnum type, boolean isEditable, Long maxSize) {
		super();
		this.type = type;
		this.isEditable = isEditable;
		this.maxSize = maxSize;
		if(ColumnTypeViewEnum.String.equals(type)){
			convertToStringType();
		}
	}

	/**
	 * Convert the view to a string type.
	 */
	private void convertToStringType() {
		if(isEditable){
			// Add the editor
			sizeEditor = new TextBox();
			sizeEditor.setWidth("75px");
			if(this.maxSize != null){
				sizeEditor.setText(this.maxSize.toString());
			}
			add(sizeEditor);
		}else{
			if(this.maxSize != null){
				FormControlStatic fs = new FormControlStatic();
				fs.setText(this.maxSize.toString());
				fs.setWidth("75px");
				this.add(fs);
			}
		}
	}
	
	/**
	 * Called when there is a column type change.
	 * @param newType
	 */
	public void onTypeChanged(ColumnTypeViewEnum newType){
		// Is this a type change?
		if(!this.type.equals(newType)){
			// If the type is not a string the just clear the view
			if(!ColumnTypeViewEnum.String.equals(newType)){
				this.clear();
			}else{
				// Convert the view to a string type.
				convertToStringType();
			}
		}
		// Save the new type
		this.type = newType;
	}
	
}
