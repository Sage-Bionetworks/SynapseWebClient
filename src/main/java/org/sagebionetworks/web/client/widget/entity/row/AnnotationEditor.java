package org.sagebionetworks.web.client.widget.entity.row;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;
import org.sagebionetworks.web.client.widget.entity.row.AnnotationEditorView.Presenter;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;

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
	public void onEnterClicked() {
		view.addNewEditor(createNewEditor());
	}

	public CellEditor createNewEditor(){
		CellEditor editor = factory.createEditor(annotation);
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
		return allValid;
	}

	@Override
	public List<String> getUpdatedValues() {
		//get the values from the current cell editors
		List<String> updatedValues = new ArrayList<String>();
		for (CellEditor cellEditor : cellEditors) {
			updatedValues.add(cellEditor.getValue());
		}

		return updatedValues;
	}

	@Override
	public void onValueDeleted(CellEditor editor) {
		cellEditors.remove(editor);
	}

}
