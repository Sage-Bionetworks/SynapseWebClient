package org.sagebionetworks.web.client.widget.entity.annotation;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.StringUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationEditorView.Presenter;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AnnotationEditor implements Presenter {
	
	private AnnotationEditorView view;
	private Annotation annotation;
	private Callback deletedCallback;
	private List<CellEditor> cellEditors;
	AnnotationCellFactory factory;
	private List<ANNOTATION_TYPE> annotationTypes;
	@Inject
	public AnnotationEditor(AnnotationEditorView view, AnnotationCellFactory factory) {
		this.view = view;
		this.factory = factory;
		view.setPresenter(this);
		
		List<String> displayTypes = new ArrayList<String>();
		annotationTypes = new ArrayList<ANNOTATION_TYPE>();
		annotationTypes.add(ANNOTATION_TYPE.STRING);
		annotationTypes.add(ANNOTATION_TYPE.LONG);
		annotationTypes.add(ANNOTATION_TYPE.DOUBLE);
		annotationTypes.add(ANNOTATION_TYPE.DATE);
		for (ANNOTATION_TYPE type : annotationTypes) {
			displayTypes.add(type.getDisplayText());
		}
		view.setTypeOptions(displayTypes);
	}
	
	@Override
	public void configure(Annotation annotation, Callback deletedCallback) {
		this.annotation = annotation;
		this.deletedCallback = deletedCallback;
		cellEditors = new ArrayList<CellEditor>();
		for (String value : annotation.getValues()) {
			//create an editor for each value
			CellEditor editor = createNewEditor();
			editor.setValue(value);
			view.addNewEditor(editor);
		}
		view.configure(annotation.getKey(), annotationTypes.indexOf(annotation.getType()));
	}
	
	@Override
	public void onAddNewValue() {
		CellEditor editor = createNewEditor();
		view.addNewEditor(editor);
		//after attaching, set focus to the new editor
		editor.setFocus(true);
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
	public void onTypeChange(int typeIndex) {
		//clear values, add an appropriate
		annotation.setType(annotationTypes.get(typeIndex));
		cellEditors = new ArrayList<CellEditor>();
		view.clearValueEditors();
		onAddNewValue();
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
	
	public List<ANNOTATION_TYPE> getAnnotationTypes() {
		return annotationTypes;
	}
	public Annotation getAnnotation() {
		return annotation;
	}
}
