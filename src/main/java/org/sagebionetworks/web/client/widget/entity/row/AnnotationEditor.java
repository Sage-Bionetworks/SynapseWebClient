package org.sagebionetworks.web.client.widget.entity.row;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.StringUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;
import org.sagebionetworks.web.client.widget.entity.row.AnnotationEditorView.Presenter;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AnnotationEditor implements Presenter {
	
	private AnnotationEditorView view;
	private Annotation annotation;
	private CallbackP<ANNOTATION_TYPE> typeChangeCallback;
	private Callback deletedCallback;
	private List<CellEditor> cellEditors;
	AnnotationCellFactory factory;
	@Inject
	public AnnotationEditor(AnnotationEditorView view, AnnotationCellFactory factory) {
		this.view = view;
		this.factory = factory;
	}
	
	@Override
	public void configure(Annotation annotation, CallbackP<ANNOTATION_TYPE> typeChangeCallback, Callback deletedCallback) {
		this.annotation = annotation;
		this.typeChangeCallback = typeChangeCallback;
		this.deletedCallback = deletedCallback;
		cellEditors = new ArrayList<CellEditor>();
		for (String value : annotation.getValues()) {
			//create an editor for each value
			CellEditor editor = createNewEditor();
			editor.setValue(value);
			view.addNewEditor(editor);
		}
		
		//also add a blank editor
		view.addNewEditor(createNewEditor());
		view.configure(annotation.getKey(), annotation.getType());
	}
	
	@Override
	public void onAddNewValue() {
		view.addNewEditor(createNewEditor());
	}

	public CellEditor createNewEditor(){
		CellEditor editor = factory.createEditor(annotation);
		editor.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				//on enter, add a new field (empty fields are ignored on save)
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					onAddNewValue();
				}
			}
		});
		cellEditors.add(editor);
		return editor;
	}

	@Override
	public void onDelete() {
		if (deletedCallback != null)
			deletedCallback.invoke();
	}

	@Override
	public void onTypeChange(ANNOTATION_TYPE newType) {
		if (typeChangeCallback != null)
			typeChangeCallback.invoke(newType);
	}

	@Override
	public boolean isValid() {
		boolean allValid = true;
		for (CellEditor cellEditor : cellEditors) {
			boolean isValid = cellEditor.isValid();
			if (!isValid)
				allValid = false;
		}
		if (!isKeyValid())
			allValid = false;
		return allValid;
	}

	public boolean isKeyValid() {
		String value = StringUtils.trimWithEmptyAsNull(view.getKey());
		if(!DisplayUtils.isDefined(value)){
			view.setKeyValidationState(ValidationState.ERROR);
			view.setKeyHelpText("You must provide a key");
			return false;
		}
		view.setKeyValidationState(ValidationState.NONE);
		view.setKeyHelpText("");
		return true;
	}
	
	@Override
	public Annotation getUpdatedAnnotation() {
		//get the values from the current cell editors
		List<String> updatedValues = new ArrayList<String>();
		for (CellEditor cellEditor : cellEditors) {
			String value = cellEditor.getValue();
			if (DisplayUtils.isDefined(value))
				updatedValues.add(value);
		}
		Annotation updatedAnnotation = new Annotation(annotation.getType(), view.getKey(), updatedValues);
		return updatedAnnotation;
	}

	@Override
	public void onValueDeleted(CellEditor editor) {
		cellEditors.remove(editor);
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}

}
