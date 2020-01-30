package org.sagebionetworks.web.unitclient.widget.entity.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValue;
import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValueType;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationCellFactory;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationEditor;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;

public class AnnotationEditorTest {
	AnnotationEditor editor;
	AnnotationEditorView mockView;
	AnnotationCellFactory mockAnnotationCellFactory;
	CellEditor mockCellEditor, mockCellEditor2, mockCellEditor3;
	AnnotationsValue annotation;
	static String ANNOTATION_KEY = "size";
	static String ANNOTATION_KEY_FROM_VIEW = "different_key";
	List<String> annotationValues;

	@Before
	public void setUp() throws Exception {
		mockView = mock(AnnotationEditorView.class);
		mockAnnotationCellFactory = mock(AnnotationCellFactory.class);
		editor = new AnnotationEditor(mockView, mockAnnotationCellFactory);
		annotationValues = new ArrayList<String>();
		mockCellEditor = mock(CellEditor.class);
		mockCellEditor2 = mock(CellEditor.class);
		mockCellEditor3 = mock(CellEditor.class);
		when(mockView.getKey()).thenReturn(ANNOTATION_KEY_FROM_VIEW);
		when(mockCellEditor.isValid()).thenReturn(true);
		when(mockCellEditor2.isValid()).thenReturn(true);
		when(mockAnnotationCellFactory.createEditor(any(AnnotationsValue.class))).thenReturn(mockCellEditor, mockCellEditor2, mockCellEditor3);
		annotation = new AnnotationsValue();
		annotation.setType(AnnotationsValueType.STRING);
		annotation.setValue(annotationValues);
	}

	@Test
	public void testConfigureSingleValue() {
		annotationValues.add("value 1");
		editor.configure(ANNOTATION_KEY, annotation, null);
		verify(mockAnnotationCellFactory).createEditor(any(AnnotationsValue.class));
		verify(mockView).addNewEditor(any(CellEditor.class));
		verify(mockView).setPresenter(editor);
		verify(mockView).configure(ANNOTATION_KEY, editor.getAnnotationTypes().indexOf(AnnotationsValueType.STRING));
	}

	@Test
	public void testConfigureManyValues() {
		// configure with many values
		annotationValues.add("value 1");
		annotationValues.add("value 2");
		editor.configure(ANNOTATION_KEY, annotation, null);
		verify(mockAnnotationCellFactory, times(2)).createEditor(any(AnnotationsValue.class));
		verify(mockView, times(2)).addNewEditor(any(CellEditor.class));
		verify(mockView).setPresenter(editor);
		verify(mockView).configure(ANNOTATION_KEY, editor.getAnnotationTypes().indexOf(AnnotationsValueType.STRING));
	}

	@Test
	public void testConfigureNoValues() {
		// configure with no values
		editor.configure(ANNOTATION_KEY, annotation, null);
		// should add a single editor
		verify(mockView).addNewEditor(any(CellEditor.class));
		verify(mockView).setPresenter(editor);
		verify(mockView).configure(ANNOTATION_KEY, editor.getAnnotationTypes().indexOf(AnnotationsValueType.STRING));
	}

	@Test
	public void testOnAddNewValue() {
		editor.configure(ANNOTATION_KEY, annotation, null);
		editor.onAddNewValue();
		verify(mockAnnotationCellFactory, times(2)).createEditor(any(AnnotationsValue.class));
		verify(mockView, times(2)).addNewEditor(any(CellEditor.class));
		verify(mockCellEditor2).setFocus(true);
	}

	@Test
	public void testCreateNewEditor() {
		editor.configure(ANNOTATION_KEY, annotation, null);
		CellEditor createdEditor = editor.createNewEditor();
		assertEquals(mockCellEditor2, createdEditor);
		ArgumentCaptor<KeyDownHandler> captor = ArgumentCaptor.forClass(KeyDownHandler.class);
		verify(mockCellEditor2).addKeyDownHandler(captor.capture());
		// test clicking ENTER adds a new editor
		KeyDownEvent mockEvent = mock(KeyDownEvent.class);
		when(mockEvent.getNativeKeyCode()).thenReturn(KeyCodes.KEY_ENTER);
		// addNewEditor() already called once from initializing with no values
		verify(mockView).addNewEditor(any(CellEditor.class));
		captor.getValue().onKeyDown(mockEvent);
		verify(mockView, times(2)).addNewEditor(any(CellEditor.class));
		verify(mockCellEditor3).setFocus(true);
	}

	@Test
	public void testOnDelete() {
		Callback mockDeletedCallback = mock(Callback.class);
		editor.configure(ANNOTATION_KEY, annotation, mockDeletedCallback);
		editor.onDelete();
		verify(mockDeletedCallback).invoke();
	}

	@Test
	public void testIsValid() {
		annotationValues.add("value 1");
		editor.configure(ANNOTATION_KEY, annotation, null);
		// by default, valid key is returned from view and cell editor says that it is valid
		assertTrue(editor.isValid());
		// but if the key is undefined, then annotation is not valid
		when(mockView.getKey()).thenReturn("");
		assertFalse(editor.isValid());

		// or if the cell editor reports invalid, then it is not valid
		when(mockView.getKey()).thenReturn(ANNOTATION_KEY_FROM_VIEW);
		when(mockCellEditor.isValid()).thenReturn(false);
		assertFalse(editor.isValid());
	}

	@Test
	public void testIsKeyValid() {
		assertTrue(editor.isKeyValid());
		verify(mockView).setKeyValidationState(ValidationState.NONE);
		verify(mockView).setKeyHelpText("");
	}

	@Test
	public void testIsKeyNotValid() {
		when(mockView.getKey()).thenReturn("");
		assertFalse(editor.isKeyValid());
		verify(mockView).setKeyValidationState(ValidationState.ERROR);
		verify(mockView).setKeyHelpText(anyString());
	}

	@Test
	public void testGetUpdatedAnnotation() {
		annotationValues.add("value 1");
		editor.configure(ANNOTATION_KEY, annotation, null);

		String modifiedValue = "There can be only one.";
		when(mockCellEditor.getValue()).thenReturn(modifiedValue);

		AnnotationsValue updatedAnnotation = editor.getUpdatedAnnotation();
		assertEquals(annotation.getType(), updatedAnnotation.getType());
		assertEquals(ANNOTATION_KEY_FROM_VIEW, editor.getUpdatedKey());
		List<String> updatedValues = updatedAnnotation.getValue();
		assertEquals(1, updatedValues.size());
		assertEquals(modifiedValue, updatedValues.get(0));
	}

	@Test
	public void testOnValueDeleted() {
		// also verify that when all values are deleted, then the annotation deleted callback in invoked
		Callback mockDeletedCallback = mock(Callback.class);
		annotationValues.add("value 1");
		annotationValues.add("value 2");
		editor.configure(ANNOTATION_KEY, annotation, mockDeletedCallback);
		editor.onValueDeleted(mockCellEditor);
		verify(mockDeletedCallback, never()).invoke();

		editor.onValueDeleted(mockCellEditor2);
		verify(mockDeletedCallback).invoke();

		AnnotationsValue updatedAnnotation = editor.getUpdatedAnnotation();
		assertEquals(0, updatedAnnotation.getValue().size());

	}

	@Test
	public void testAsWidget() {
		editor.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testOnTypeChange() {
		annotationValues.add("value 1");
		editor.configure(ANNOTATION_KEY, annotation, null);
		verify(mockView).addNewEditor(any(CellEditor.class));

		int dateIndex = editor.getAnnotationTypes().indexOf(AnnotationsValueType.TIMESTAMP_MS);
		editor.onTypeChange(dateIndex);
		verify(mockView).clearValueEditors();
		verify(mockView, times(2)).addNewEditor(any(CellEditor.class));
		verify(mockCellEditor2).setFocus(true);

		assertEquals(AnnotationsValueType.TIMESTAMP_MS, editor.getAnnotation().getType());
		// after type change, should clear values
		AnnotationsValue a = editor.getUpdatedAnnotation();
		assertEquals(0, a.getValue().size());
	}

}
