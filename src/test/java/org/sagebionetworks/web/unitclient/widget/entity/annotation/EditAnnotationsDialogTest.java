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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.annotation.v2.Annotations;
import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValue;
import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValueType;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationEditor;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationTransformer;
import org.sagebionetworks.web.client.widget.entity.annotation.EditAnnotationsDialog;
import org.sagebionetworks.web.client.widget.entity.annotation.EditAnnotationsDialogView;
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
	SynapseJavascriptClient mockJsClient;
	@Mock
	AnnotationTransformer mockAnnotationTransformer;
	@Mock
	PortalGinInjector mockPortalGinInjector;
	@Mock
	EventBus mockEventBus;
	Annotations annotations;
	@Mock
	EntityBundle mockBundle;
	@Mock
	AnnotationEditor mockEditor;
	Map<String, AnnotationsValue> annotationsMap;

	@Before
	public void setUp() throws Exception {
		when(mockPortalGinInjector.getEventBus()).thenReturn(mockEventBus);
		dialog = new EditAnnotationsDialog(mockView, mockJsClient, mockPortalGinInjector);

		annotations = new Annotations();
		annotationsMap = new HashMap<String, AnnotationsValue>();
		annotations.setAnnotations(annotationsMap);
		AnnotationsValue annotationsValue = new AnnotationsValue();
		annotationsValue.setValue(Collections.EMPTY_LIST);
		annotationsValue.setType(AnnotationsValueType.STRING);
		annotationsMap.put("key1", annotationsValue);
		annotationsValue = new AnnotationsValue();
		annotationsValue.setType(AnnotationsValueType.STRING);
		annotationsValue.setValue(Collections.singletonList("foo"));
		annotationsMap.put("key2", annotationsValue);
		annotationsValue = new AnnotationsValue();
		annotationsValue.setType(AnnotationsValueType.LONG);
		annotationsValue.setValue(Collections.singletonList("42"));
		annotationsMap.put("key3", annotationsValue);

		when(mockBundle.getAnnotations()).thenReturn(annotations);
		Entity mockEntity = mock(Entity.class);
		when(mockBundle.getEntity()).thenReturn(mockEntity);
		when(mockEntity.getId()).thenReturn(ENTITY_ID);

		mockEditor = mock(AnnotationEditor.class);
		when(mockPortalGinInjector.getAnnotationEditor()).thenReturn(mockEditor);
		AsyncMockStubber.callSuccessWith(null).when(mockJsClient).updateAnnotations(anyString(), any(Annotations.class), any(AsyncCallback.class));
	}

	@Test
	public void testConfigureEmpty() {
		verify(mockView).setPresenter(dialog);
		annotationsMap.clear();
		dialog.configure(mockBundle);

		// verify a single annotation editor is still added by default
		verify(mockPortalGinInjector).getAnnotationEditor();
		verify(mockView).addAnnotationEditor(any(Widget.class));
		verify(mockView).showEditor();

		assertEquals(1, dialog.getAnnotationEditors().size());
	}

	@Test
	public void testConfigure() {
		dialog.configure(mockBundle);

		// verify the 3 annotation editors are created and added to the view
		verify(mockPortalGinInjector, times(3)).getAnnotationEditor();
		verify(mockView).showEditor();
		verify(mockView, times(3)).addAnnotationEditor(any(Widget.class));

		assertEquals(dialog.getAnnotationsCopy(), annotations);
		assertEquals(3, dialog.getAnnotationEditors().size());
	}

	@Test
	public void testCreateAnnotationEditorAnnotationDeleteCallback() {
		annotationsMap.clear();
		dialog.configure(mockBundle);

		assertEquals(1, dialog.getAnnotationEditors().size());

		AnnotationsValue annotationsValue = new AnnotationsValue();
		annotationsValue.setValue(Collections.EMPTY_LIST);
		annotationsValue.setType(AnnotationsValueType.STRING);
		String key = "1";
		dialog.createAnnotationEditor(key, annotationsValue);
		verify(mockPortalGinInjector, times(2)).getAnnotationEditor();
		ArgumentCaptor<Callback> deletedCallbackCaptor = ArgumentCaptor.forClass(Callback.class);

		verify(mockEditor).configure(eq(key), eq(annotationsValue), deletedCallbackCaptor.capture());
		Callback deletedCallback = deletedCallbackCaptor.getValue();

		// before delete, 2 editors
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
		// editor detects invalid entry, should not do async call
		verify(mockView, never()).setLoading();
		verify(mockView, never()).hideEditor();
		verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
	}


	@Test
	public void testOnSaveServiceCallFailure() {
		String errorMessage = "Failure detected on server";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockJsClient).updateAnnotations(anyString(), any(Annotations.class), any(AsyncCallback.class));

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
