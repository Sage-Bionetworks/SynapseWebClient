package org.sagebionetworks.web.client.widget.entity.row;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
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
	@UiField
	Button deleteAnnotationButton;
	@UiField
	FormGroup formGroup;
	@UiField
	HelpBlock helpBlock;

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
		deleteAnnotationButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onDelete();
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
	public void addNewEditor(final CellEditor editor) {
		final FlowPanel editorAndDelete = new FlowPanel();
		editorAndDelete.add(editor.asWidget());
		Button deleteButton = new Button("", IconType.TIMES, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				editorsContainer.remove(editorAndDelete);
				presenter.onValueDeleted(editor);
			}
		});
		deleteButton.setPull(Pull.RIGHT);
		editorAndDelete.add(deleteButton);
		editorsContainer.add(editorAndDelete);
	}
	
	@Override
	public String getKey() {
		return keyField.getValue();
	}
	

	@Override
	public void setKeyValidationState(ValidationState state) {
		this.formGroup.setValidationState(state);
	}

	@Override
	public void setKeyHelpText(String help) {
		this.helpBlock.setText(help);
	}
}
