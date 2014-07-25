package org.sagebionetworks.web.client.widget.table.v2;

import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * This is the base of a list of ColumnModels.  It consists of a view of columns and
 * if editable it also includes a button for launching a editor in a modal.
 * 
 * @author John
 *
 */
public class ColumnModelsViewBaseImpl extends Composite implements ColumnModelsViewBase{
	
	public interface Binder extends UiBinder<Widget, ColumnModelsViewBaseImpl> {	}

	@UiField
	SimplePanel viewerPanel;
	@UiField
	Modal editModal;
	@UiField
	ModalBody columnEditorModalPanel;

	
	@Inject
	public ColumnModelsViewBaseImpl(final Binder uiBinder){
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setPresenter(Presenter presenter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showError(String string) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setViewer(ColumnModelsView viewer) {
		this.viewerPanel.add(viewer);
	}

	@Override
	public void setEditor(ColumnModelsView editor) {
		this.columnEditorModalPanel.add(editor);
	}

	@Override
	public void setEditable(boolean isEditable) {

	}

	@Override
	public void showEditor() {
		editModal.show();
	}

}
