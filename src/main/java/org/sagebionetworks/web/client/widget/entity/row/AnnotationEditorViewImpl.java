package org.sagebionetworks.web.client.widget.entity.row;

import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AnnotationEditorViewImpl implements AnnotationEditorView {
	public interface Binder extends UiBinder<Widget, AnnotationEditorViewImpl> {	}
	
	private Presenter presenter;
	private Widget widget;
	@UiField
	ListBox typeComboBox;
	@UiField
	TextBox keyField;
	@UiField
	Column editorsContainer;
	
	@Inject
	public AnnotationEditorViewImpl(Binder uiBinder){
		widget = uiBinder.createAndBindUi(this);
		typeComboBox.addItem(ANNOTATION_TYPE.STRING.getDisplayText());
		typeComboBox.addItem(ANNOTATION_TYPE.LONG.getDisplayText());
		typeComboBox.addItem(ANNOTATION_TYPE.DOUBLE.getDisplayText());
		typeComboBox.addItem(ANNOTATION_TYPE.DATE.getDisplayText());
		typeComboBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				String selectedValue = typeComboBox.getValue(typeComboBox.getSelectedIndex());
				presenter.onTypeChange(ANNOTATION_TYPE.getTypeForDisplay(selectedValue));
				
			}
		});
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void configure(String key, ANNOTATION_TYPE type) {
		keyField.setValue(key);
		for (int i = 0; i < typeComboBox.getItemCount(); i++) {
			if (type.getDisplayText().equals(typeComboBox.getItemText(i))) {
				typeComboBox.setSelectedIndex(i);
				break;
			}
		}
	}

	@Override
	public void addNewEditor(CellEditor editor) {
		editorsContainer.add(editor.asWidget());
	}
}
