package org.sagebionetworks.web.unitclient.widget.entity.annotation;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationEditor;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationTransformer;
import org.sagebionetworks.web.client.widget.entity.annotation.EditAnnotationsDialog;
import org.sagebionetworks.web.client.widget.entity.annotation.EditAnnotationsDialogView;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;

import com.google.gwt.user.client.ui.Widget;

import static org.mockito.Mockito.*;

public class EditAnnotationsDialogTest {
	private static final String ENTITY_ID = "88888888";

	EditAnnotationsDialog dialog;
	
	EditAnnotationsDialogView mockView; 
	SynapseClientAsync mockSynapseClient; 
	AnnotationTransformer mockAnnotationTransformer; 
	PortalGinInjector mockPortalGinInjector; 
	JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
	Annotations annotations;
	EntityBundle mockBundle;
	EntityUpdatedHandler mockUpdateHandler;
	List<Annotation> annotationList;
	
	AnnotationEditor mockEditor;
	
	@Before
	public void setUp() throws Exception {
		mockView = mock(EditAnnotationsDialogView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockAnnotationTransformer = mock(AnnotationTransformer.class);
		mockPortalGinInjector = mock(PortalGinInjector.class);
		dialog = new EditAnnotationsDialog(mockView, mockSynapseClient, mockAnnotationTransformer, mockPortalGinInjector, adapter);
		
		annotations = new Annotations();
		annotations.initailzeMaps();
		annotations.getStringAnnotations().put("key1", Collections.EMPTY_LIST);
		annotations.getStringAnnotations().put("key2", Collections.singletonList("foo"));
		annotations.getLongAnnotations().put("key3", Collections.singletonList(42L));
		
		mockBundle = mock(EntityBundle.class);
		when(mockBundle.getAnnotations()).thenReturn(annotations);
		Entity mockEntity = mock(Entity.class);
		when(mockBundle.getEntity()).thenReturn(mockEntity);
		when(mockEntity.getId()).thenReturn(ENTITY_ID);
		
		annotationList = new ArrayList<Annotation>();
		annotationList.add(new Annotation(ANNOTATION_TYPE.STRING, "key1", Collections.EMPTY_LIST));
		annotationList.add(new Annotation(ANNOTATION_TYPE.STRING, "key2", Collections.singletonList("foo")));
		annotationList.add(new Annotation(ANNOTATION_TYPE.LONG, "key3", Collections.singletonList("42")));
		when(mockAnnotationTransformer.annotationsToList(any(Annotations.class))).thenReturn(annotationList);
		
		mockUpdateHandler = mock(EntityUpdatedHandler.class);
		mockEditor = mock(AnnotationEditor.class);
		when(mockPortalGinInjector.getAnnotationEditor()).thenReturn(mockEditor);
	}

	@Test
	public void testConfigureEmpty() {
		verify(mockView).setPresenter(dialog);
		annotationList.clear();
		dialog.configure(mockBundle, mockUpdateHandler);
		
		//verify a single annotation editor is still added by default
		verify(mockPortalGinInjector).getAnnotationEditor();
		verify(mockView).addAnnotationEditor(any(Widget.class));
		verify(mockView).showEditor();
		
		assertEquals(1, dialog.getAnnotationEditors().size());
	}
	
	@Test
	public void testConfigure() {
		dialog.configure(mockBundle, mockUpdateHandler);
		
		//verify the 3 annotation editors are created and added to the view
		verify(mockPortalGinInjector, times(3)).getAnnotationEditor();
		verify(mockView).showEditor();
		verify(mockView, times(3)).addAnnotationEditor(any(Widget.class));
		
		assertEquals(dialog.getAnnotationsCopy(), annotations);
		assertEquals(3, dialog.getAnnotationEditors().size());
	}

	@Test
	public void testCreateAnnotationEditorAnnotationDeleteCallback() {
		annotationList.clear();
		dialog.configure(mockBundle, mockUpdateHandler);
		
		assertEquals(1, dialog.getAnnotationEditors().size());
		
		Annotation annotation = new Annotation(ANNOTATION_TYPE.LONG, "l", Collections.EMPTY_LIST);
		dialog.createAnnotationEditor(annotation);
		verify(mockPortalGinInjector, times(2)).getAnnotationEditor();
		ArgumentCaptor<Callback> deletedCallbackCaptor = ArgumentCaptor.forClass(Callback.class);
		
		verify(mockEditor).configure(eq(annotation), any(CallbackP.class), deletedCallbackCaptor.capture());
		Callback deletedCallback = deletedCallbackCaptor.getValue();
		
		//before delete, 2 editors
		assertEquals(2, dialog.getAnnotationEditors().size());
		deletedCallback.invoke();
		verify(mockView).removeAnnotationEditor(any(Widget.class));
		
		assertEquals(1, dialog.getAnnotationEditors().size());
	}
	
	@Test
	public void testCreateAnnotationEditorAnnotationTypeChangeCallback() {
		annotationList.clear();
		dialog.configure(mockBundle, mockUpdateHandler);
		
		assertEquals(1, dialog.getAnnotationEditors().size());
		
		Annotation annotation = new Annotation(ANNOTATION_TYPE.LONG, "l", Collections.EMPTY_LIST);
		dialog.createAnnotationEditor(annotation);
		verify(mockPortalGinInjector, times(2)).getAnnotationEditor();
		ArgumentCaptor<CallbackP> typeChangeCallbackCaptor = ArgumentCaptor.forClass(CallbackP.class);
		
		verify(mockEditor).configure(eq(annotation), typeChangeCallbackCaptor.capture(), any(Callback.class));
		CallbackP<ANNOTATION_TYPE> typeChangeCallback = typeChangeCallbackCaptor.getValue();
		
		
		//before type change, 2 editors
		assertEquals(2, dialog.getAnnotationEditors().size());
		typeChangeCallback.invoke(ANNOTATION_TYPE.STRING);
		verify(mockView).replaceAnnotationEditor(any(Widget.class), any(Widget.class));
		
		assertEquals(2, dialog.getAnnotationEditors().size());
	}

	@Test
	public void testOnCancel() {
		dialog.onCancel();
		verify(mockView).hideEditor();
	}

	@Test
	public void testOnSave() {
		
	}

	@Test
	public void testAsWidget() {
		dialog.asWidget();
		verify(mockView).asWidget();
	}

}
