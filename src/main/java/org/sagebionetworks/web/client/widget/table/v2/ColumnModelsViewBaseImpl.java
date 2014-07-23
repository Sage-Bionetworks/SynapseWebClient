package org.sagebionetworks.web.client.widget.table.v2;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.PanelFooter;
import org.gwtbootstrap3.client.ui.PanelHeader;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
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
	PanelHeader panelHeader;
	@UiField
	PanelFooter panelFooter;
	@UiField
	PanelBody columnViewerPanel;
	@UiField
	ModalBody columnEditorModalPanel;
	@UiField
	Button editColumnsButton;
	
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
		this.columnViewerPanel.add(viewer);
	}

	@Override
	public void setEditor(ColumnModelsView editor) {
		this.columnEditorModalPanel.add(editor);
	}

	@Override
	public void setEditable(boolean isEditable) {
		panelFooter.setVisible(isEditable);
		editColumnsButton.setEnabled(isEditable);
	}

}
