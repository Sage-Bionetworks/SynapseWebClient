package org.sagebionetworks.web.unitclient.widget.entity.annotation;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationTransformer;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationsRendererWidget;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationsRendererWidgetView;
import org.sagebionetworks.web.client.widget.entity.annotation.EditAnnotationsDialog;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.ui.Widget;

public class AnnotationsRendererWidgetTest {
	
	AnnotationsRendererWidget widget;
	
	AnnotationsRendererWidgetView mockView;
	AnnotationTransformer mockAnnotationTransformer;
	EditAnnotationsDialog mockEditAnnotationsDialog;
	EntityBundle mockBundle;
	List<Annotation> annotationList;
	PreflightController mockPreflightController;
	@Before
	public void setUp() throws Exception {
		mockEditAnnotationsDialog = mock(EditAnnotationsDialog.class);
		mockView = mock(AnnotationsRendererWidgetView.class);
		mockAnnotationTransformer = mock(AnnotationTransformer.class);
		mockPreflightController = mock(PreflightController.class);
		widget = new AnnotationsRendererWidget(mockView, mockAnnotationTransformer, mockEditAnnotationsDialog, mockPreflightController);
		mockBundle = mock(EntityBundle.class);
		annotationList = new ArrayList<Annotation>();
		annotationList.add(new Annotation(ANNOTATION_TYPE.STRING, "key", Collections.EMPTY_LIST));
		when(mockAnnotationTransformer.annotationsToList(any(Annotations.class))).thenReturn(annotationList);
	}

	@Test
	public void testConfigureEmptyAnnotations() {
		//also verify construction
		verify(mockView).setPresenter(widget);
		verify(mockView).addEditorToPage(any(Widget.class));
		
		annotationList.clear();
		boolean canEdit = true;
		widget.configure(mockBundle, canEdit);
		
		verify(mockView).showNoAnnotations();
		verify(mockView).setEditUIVisible(canEdit);
		
		assertTrue(widget.isEmpty());
	}
	
	@Test
	public void testConfigureAnnotations() {
		boolean canEdit = false;
		widget.configure(mockBundle, canEdit);
		
		verify(mockView).configure(annotationList);
		verify(mockView).setEditUIVisible(canEdit);
		
		assertFalse(widget.isEmpty());
	}


	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testOnEdit() {
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		EntityUpdatedHandler updateHandler = mock(EntityUpdatedHandler.class);
		widget.configure(mockBundle, true);
		widget.setEntityUpdatedHandler(updateHandler);
		
		//test that on edit, we pass the bundle and update handler to the edit dialog
		widget.onEdit();
		
		ArgumentCaptor<EntityUpdatedHandler> updateCaptor = ArgumentCaptor.forClass(EntityUpdatedHandler.class);
		ArgumentCaptor<EntityBundle> bundleCaptor = ArgumentCaptor.forClass(EntityBundle.class);
		
		verify(mockEditAnnotationsDialog).configure(bundleCaptor.capture(), updateCaptor.capture());
		
		assertEquals(updateCaptor.getValue(), updateHandler);
		assertEquals(bundleCaptor.getValue(), mockBundle);
	}
	
	@Test
	public void testOnEditFailedPreflight() {
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		widget.configure(mockBundle, true);
		//test that on edit, we pass the bundle and update handler to the edit dialog
		widget.onEdit();
		
		verify(mockEditAnnotationsDialog, never()).configure(any(EntityBundle.class), any(EntityUpdatedHandler.class));
	}
	

}
