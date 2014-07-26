package org.sagebionetworks.web.client.widget.table.v2;

import org.sagebionetworks.web.client.view.bootstrap.table.TableData;

/**
 * A column max size view changes based on the column type.
 * @author John
 *
 */
public class MaxSizeView extends TableData {
	
	ColumnTypeViewEnum type;
	boolean isEditable;
	TextView sizeTextView;
	Long maxSize;
	
	
	public MaxSizeView(ColumnTypeViewEnum type, boolean isEditable, Long maxSize) {
		super();
		this.type = type;
		this.isEditable = isEditable;
		this.maxSize = maxSize;
		setNewType(type, isEditable, maxSize);
	}

	/**
	 * @param type
	 * @param isEditable
	 * @param maxSize
	 */
	public void setNewType(ColumnTypeViewEnum type, boolean isEditable,	Long maxSize) {
		this.clear();
		if(ColumnTypeViewEnum.String.equals(type)){
			String maxString = null;
			if(maxSize!= null){
				maxString = maxSize.toString();
			}
			// Strings can be editable
			sizeTextView = new TextView(maxString, isEditable);
		}else{
			// For not text views just use an empty non-editable field
			sizeTextView = new TextView(null, false);
		}
		sizeTextView.asWidget().setWidth("75px");
		this.add(sizeTextView);
	}
	
	/**
	 * Called when there is a column type change.
	 * @param newType
	 */
	public void onTypeChanged(ColumnTypeViewEnum newType){
		// Is this a type change?
		if(!this.type.equals(newType)){
			// Set the new type
			setNewType(newType, isEditable, maxSize);
		}
		// Save the new type
		this.type = newType;
	}
	
	/**
	 * Get the text from this view.
	 */
	public String getText(){
		return sizeTextView.getText();
	}
	
}
