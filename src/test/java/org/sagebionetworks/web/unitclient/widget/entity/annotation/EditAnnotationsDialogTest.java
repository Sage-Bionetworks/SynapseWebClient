package org.sagebionetworks.web.unitclient.widget.entity.annotation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationEditor;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationTransformer;
import org.sagebionetworks.web.client.widget.entity.annotation.EditAnnotationsDialog;
import org.sagebionetworks.web.client.widget.entity.annotation.EditAnnotationsDialogView;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

@RunWith(MockitoJUnitRunner.class)
public class EditAnnotationsDialogTest {
	private static final String ENTITY_ID = "88888888";

	EditAnnotationsDialog dialog;
	@Mock
	EditAnnotationsDialogView mockView; 
	@Mock
	SynapseClientAsync mockSynapseClient; 
	@Mock
	AnnotationTransformer mockAnnotationTransformer; 
	@Mock
	PortalGinInjector mockPortalGinInjector; 
	@Mock
	EventBus mockEventBus;
	Annotations annotations;
	@Mock
	EntityBundle mockBundle;
	List<Annotation> annotationList;
	@Mock
	AnnotationEditor mockEditor;
	
	@Before
	public void setUp() throws Exception {
		when(mockPortalGinInjector.getEventBus()).thenReturn(mockEventBus);
		dialog = new EditAnnotationsDialog(mockView, mockSynapseClient, mockAnnotationTransformer, mockPortalGinInjector);
		
		annotations = new Annotations();
		annotations.initailzeMaps();
		annotations.getStringAnnotations().put("key1", Collections.EMPTY_LIST);
		annotations.getStringAnnotations().put("key2", Collections.singletonList("foo"));
		annotations.getLongAnnotations().put("key3", Collections.singletonList(42L));
		
		when(mockBundle.getAnnotations()).thenReturn(annotations);
		Entity mockEntity = mock(Entity.class);
		when(mockBundle.getEntity()).thenReturn(mockEntity);
		when(mockEntity.getId()).thenReturn(ENTITY_ID);
		
		annotationList = new ArrayList<Annotation>();
		annotationList.add(new Annotation(ANNOTATION_TYPE.STRING, "key1", Collections.EMPTY_LIST));
		annotationList.add(new Annotation(ANNOTATION_TYPE.STRING, "key2", Collections.singletonList("foo")));
		annotationList.add(new Annotation(ANNOTATION_TYPE.LONG, "key3", Collections.singletonList("42")));
		when(mockAnnotationTransformer.annotationsToList(any(Annotations.class))).thenReturn(annotationList);
		
		mockEditor = mock(AnnotationEditor.class);
		when(mockPortalGinInjector.getAnnotationEditor()).thenReturn(mockEditor);
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).updateAnnotations(anyString(), any(Annotations.class), any(AsyncCallback.class));
	}

	@Test
	public void testConfigureEmpty() {
		verify(mockView).setPresenter(dialog);
		annotationList.clear();
		dialog.configure(mockBundle);
		
		//verify a single annotation editor is still added by default
		verify(mockPortalGinInjector).getAnnotationEditor();
		verify(mockView).addAnnotationEditor(any(Widget.class));
		verify(mockView).showEditor();
		
		assertEquals(1, dialog.getAnnotationEditors().size());
	}
	
	@Test
	public void testConfigure() {
		dialog.configure(mockBundle);
		
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
		dialog.configure(mockBundle);
		
		assertEquals(1, dialog.getAnnotationEditors().size());
		
		Annotation annotation = new Annotation(ANNOTATION_TYPE.LONG, "l", Collections.EMPTY_LIST);
		dialog.createAnnotationEditor(annotation);
		verify(mockPortalGinInjector, times(2)).getAnnotationEditor();
		ArgumentCaptor<Callback> deletedCallbackCaptor = ArgumentCaptor.forClass(Callback.class);
		
		verify(mockEditor).configure(eq(annotation), deletedCallbackCaptor.capture());
		Callback deletedCallback = deletedCallbackCaptor.getValue();
		
		//before delete, 2 editors
		assertEquals(2, dialog.getAnnotationEditors().size());
		deletedCallback.invoke();
		verify(mockView).removeAnnotationEditor(any(Widget.class));
		
		assertEquals(1, dialog.getAnnotationEditors().size());
	}

	@Test
	public void testOnCancel() {
		dialog.onCancel();
		verify(mockView).hideEditor();
	}

	@Test
	public void testOnSaveHappyCase() {
		dialog.configure(mockBundle);
		when(mockEditor.isValid()).thenReturn(true);
		dialog.onSave();
		verify(mockView).setLoading();
		verify(mockView).showInfo(anyString());
		verify(mockView).hideEditor();
		verify(mockEventBus).fireEvent(any(EntityUpdatedEvent.class));
	}


	@Test
	public void testOnSaveInvalidEditor() {
		dialog.configure(mockBundle);
		when(mockEditor.isValid()).thenReturn(false);
		dialog.onSave();
		verify(mockView).showError(anyString());
		//editor detects invalid entry, should not do async call
		verify(mockView, never()).setLoading();
		verify(mockView, never()).hideEditor();
		verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
	}
	

	@Test
	public void testOnSaveServiceCallFailure() {
		String errorMessage = "Failure detected on server";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockSynapseClient).updateAnnotations(anyString(), any(Annotations.class), any(AsyncCallback.class));
		
		dialog.configure(mockBundle);
		when(mockEditor.isValid()).thenReturn(true);
		dialog.onSave();
		verify(mockView).setLoading();
		verify(mockView).showError(errorMessage);
		verify(mockView, never()).hideEditor();
		verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
	}
	
	@Test
	public void testAsWidget() {
		dialog.asWidget();
		verify(mockView).asWidget();
	}

}
