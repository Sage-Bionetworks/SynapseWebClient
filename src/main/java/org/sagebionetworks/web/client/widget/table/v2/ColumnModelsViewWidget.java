package org.sagebionetworks.web.client.widget.table.v2;

import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelsView.ViewType;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * All business logic for the ColumnModelsView.
 * 
 * @author jmhill
 *
 */
public class ColumnModelsViewWidget implements ColumnModelsView.Presenter, SynapseWidgetPresenter{
	
	PortalGinInjector ginInjector;
	ColumnModelsViewBase baseView;
	ColumnModelsView viewer;
	ColumnModelsView editor;
	boolean isEditable;
	
	/**
	 * New presenter with its view.
	 * @param view
	 */
	@Inject
	public ColumnModelsViewWidget(ColumnModelsViewBase baseView, PortalGinInjector ginInjector){
		this.ginInjector = ginInjector;
		// we will always have a viewer
		this.baseView = baseView;
		// We need two copies of the view, one as an editor, and the other as a viewer.
		this.viewer = ginInjector.getColumnModelsView();
		this.viewer.setPresenter(this);
		this.editor = ginInjector.getColumnModelsView();
		this.editor.setPresenter(this);
		// Add all of the parts
		this.baseView.setViewer(this.viewer);
		this.baseView.setEditor(this.editor);
	}

	@Override
	public void configure(List<ColumnModel> models, boolean isEditable) {
		this.isEditable = isEditable;
		viewer.configure(ViewType.VIEWER, this.isEditable);
		editor.configure(ViewType.EDITOR, this.isEditable);
		// If this is 
		baseView.setEditable(isEditable);
		for(ColumnModel cm: models){
			// Add each column to the viewer.
			this.viewer.addColumn(cm, false);
			this.editor.addColumn(cm, false);
		}
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
			baseView.showError("This view is not editable");
		}
		// Create a new column
		ColumnModel cm = new ColumnModel();
		cm.setColumnType(ColumnType.STRING);
		cm.setMaximumSize(50L);
		// Assign an id to this column
		// New columns are editable
		editor.addColumn(cm, true);
	}
	

	@Override
	public Widget asWidget() {
		return baseView.asWidget();
	}

	@Override
	public void onEditColumns() {
		// Pass this to the base
		baseView.showEditor();
	}

	@Override
	public void columnSelectionChanged(String columnId, Boolean isSelected) {
		// TODO Auto-generated method stub
		
	}
}
