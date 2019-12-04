package org.sagebionetworks.web.client.widget.entity.annotation;

import java.util.ArrayList;
import java.util.List;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValue;
import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValueType;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.StringUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationEditorView.Presenter;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AnnotationEditor implements Presenter {

	private static final String DATE = "Date";
	private static final String TEXT = "Text";
	private static final String INTEGER = "Integer";
	private static final String FLOATING_POINT = "Floating Point";
	private AnnotationEditorView view;
	private Callback deletedCallback;
	private List<CellEditor> cellEditors;
	AnnotationCellFactory factory;
	AnnotationsValue newAnnotation;
	private List<AnnotationsValueType> annotationTypes;

	@Inject
	public AnnotationEditor(AnnotationEditorView view, AnnotationCellFactory factory) {
		this.view = view;
		this.factory = factory;
		view.setPresenter(this);

		List<String> displayTypes = new ArrayList<String>();
		annotationTypes = new ArrayList<AnnotationsValueType>();
		annotationTypes.add(AnnotationsValueType.STRING);
		annotationTypes.add(AnnotationsValueType.LONG);
		annotationTypes.add(AnnotationsValueType.DOUBLE);
		annotationTypes.add(AnnotationsValueType.TIMESTAMP_MS);
		for (AnnotationsValueType type : annotationTypes) {
			displayTypes.add(getDisplayName(type));
		}
		view.setTypeOptions(displayTypes);
	}

	public void configure(String key, AnnotationsValue annotation, Callback deletedCallback) {
		newAnnotation = new AnnotationsValue();
		newAnnotation.setType(annotation.getType());
		this.deletedCallback = deletedCallback;
		cellEditors = new ArrayList<CellEditor>();
		if (annotation.getValue() == null) {
			annotation.setValue(new ArrayList<String>());
		}
		for (String value : annotation.getValue()) {
			// create an editor for each value
			CellEditor editor = createNewEditor();
			editor.setValue(value);
			view.addNewEditor(editor);
		}

		// if no values, then add a single editor (allows edit or delete of annotation)
		if (annotation.getValue().isEmpty()) {
			CellEditor editor = createNewEditor();
			view.addNewEditor(editor);
		}
		view.configure(key, annotationTypes.indexOf(annotation.getType()));
	}

	@Override
	public void onAddNewValue() {
		CellEditor editor = createNewEditor();
		view.addNewEditor(editor);
		// after attaching, set focus to the new editor
		editor.setFocus(true);
	}

	public CellEditor createNewEditor() {
		CellEditor editor = factory.createEditor(newAnnotation);
		editor.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				// on enter, add a new field (empty fields are ignored on save)
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
		// clear values, add an appropriate
		newAnnotation.setType(annotationTypes.get(typeIndex));
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
		String value = StringUtils.emptyAsNull(view.getKey());
		if (!DisplayUtils.isDefined(value)) {
			view.setKeyValidationState(ValidationState.ERROR);
			view.setKeyHelpText("You must provide a key");
			return false;
		}
		view.setKeyValidationState(ValidationState.NONE);
		view.setKeyHelpText("");
		return true;
	}

	public AnnotationsValue getUpdatedAnnotation() {
		// get the values from the current cell editors
		List<String> updatedValues = new ArrayList<String>();
		for (CellEditor cellEditor : cellEditors) {
			String value = cellEditor.getValue();
			if (DisplayUtils.isDefined(value))
				updatedValues.add(value);
		}
		newAnnotation.setValue(updatedValues);
		return newAnnotation;
	}

	public String getUpdatedKey() {
		return view.getKey();
	}

	@Override
	public void onValueDeleted(CellEditor editor) {
		cellEditors.remove(editor);
		if (cellEditors.size() == 0) {
			onDelete();
		}
	}

	public Widget asWidget() {
		return view.asWidget();
	}

	public List<AnnotationsValueType> getAnnotationTypes() {
		return annotationTypes;
	}

	public AnnotationsValue getAnnotation() {
		return newAnnotation;
	}

	public static final String getDisplayName(AnnotationsValueType type) {
		String displayName = "Unknown";
		switch (type) {
			case DOUBLE:
				displayName = FLOATING_POINT;
				break;
			case LONG:
				displayName = INTEGER;
				break;
			case STRING:
				displayName = TEXT;
				break;
			case TIMESTAMP_MS:
				displayName = DATE;
				break;
			default:
				break;
		}
		return displayName;
	}

	public static final AnnotationsValueType getType(String displayName) {
		if (FLOATING_POINT.equals(displayName)) {
			return AnnotationsValueType.DOUBLE;
		} else if (INTEGER.equals(displayName)) {
			return AnnotationsValueType.LONG;
		} else if (DATE.equals(displayName)) {
			return AnnotationsValueType.TIMESTAMP_MS;
		} else {
			return AnnotationsValueType.STRING;
		}
	}
}
