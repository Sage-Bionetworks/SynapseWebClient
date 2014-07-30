package org.sagebionetworks.web.client.widget.table.v2;

/**
 * Control logic for a single ColumnModelTableRowEditor.
 * 
 * @author John
 *
 */
public class ColumnModelTableRowEditorPresenter implements ColumnModelTableRowEditor.Presenter {

	ColumnModelTableRowEditor editor;
	ColumnTypeViewEnum currentType;
	String maxSize = null;
	
	public ColumnModelTableRowEditorPresenter(ColumnModelTableRowEditor editor){
		this.editor = editor;
		currentType = editor.getColumnType();
		maxSize = editor.getMaxSize();
		editor.setPresenter(this);
	}
	
	@Override
	public void onTypeChanged() {
		// Is this a change
		ColumnTypeViewEnum newType = editor.getColumnType();
		if(!currentType.equals(newType)){
			// This is change
			if(ColumnTypeViewEnum.String.equals(newType)){
				editor.setMaxSize(maxSize);
				editor.setSizeFieldVisible(true);
			}else{
				editor.setMaxSize(null);
				editor.setSizeFieldVisible(false);
			}
			this.currentType = newType;
		}
	}

}
