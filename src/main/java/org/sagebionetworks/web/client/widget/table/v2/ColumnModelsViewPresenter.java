package org.sagebionetworks.web.client.widget.table.v2;

import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;

/**
 * All business logic for the ColumnModelsView.
 * 
 * @author jmhill
 *
 */
public class ColumnModelsViewPresenter implements ColumnModelsView.Presenter {
	
	private long idSequence = -1;
	ColumnModelsView view;
	boolean isEditable;
	
	/**
	 * New presenter with its view.
	 * @param view
	 */
	public ColumnModelsViewPresenter(ColumnModelsView view){
		this.view = view;
	}

	@Override
	public void configure(String headerText, List<ColumnModel> models, boolean isEditable) {
		this.isEditable = isEditable;
		// Clear the view
		view.clear();
		
	}

	@Override
	public List<ColumnModel> getCurrentModels() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean validateModel() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addNewColumn() {
		if(!isEditable){
			view.showError("This view is not editable");
		}
		// Create a new column
		ColumnModel cm = new ColumnModel();
		cm.setColumnType(ColumnType.STRING);
		cm.setMaximumSize(50L);
		cm.setId(""+idSequence--);
		// Assign an id to this column
		// New columns are editable
		addColumnMode(cm, true);
	}
	
	private void addColumnMode(ColumnModel cm, boolean editable){
		view.addNewColumn(cm.getId());
		view.setName(cm.getId(), cm.getName(), isEditable);
		view.setColumnType(cm.getId(), cm.getColumnType(), isEditable);
		if(ColumnType.STRING.equals(cm.getColumnType())){
			// Strings have length.
			view.setColumnMaxSize(cm.getId(), cm.getMaximumSize().toString(), editable);
		}else{
			// This is not a string
			view.setColumnMaxSize(cm.getId(), "", false);
		}
		view.setColumnDefault(cm.getId(), cm.getDefaultValue(), editable);
	}
	

}
