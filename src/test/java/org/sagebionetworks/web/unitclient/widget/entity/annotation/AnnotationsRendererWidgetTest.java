package org.sagebionetworks.web.unitclient.widget.entity.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
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
	@Mock
	AnnotationsRendererWidgetView mockView;
	@Mock
	AnnotationTransformer mockAnnotationTransformer;
	@Mock
	EditAnnotationsDialog mockEditAnnotationsDialog;
	@Mock
	EntityBundle mockBundle;
	List<Annotation> annotationList;
	@Mock
	PreflightController mockPreflightController;
	@Mock
	PortalGinInjector mockPortalGinInjector;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(mockPortalGinInjector.getEditAnnotationsDialog()).thenReturn(mockEditAnnotationsDialog);
		widget = new AnnotationsRendererWidget(mockView, mockAnnotationTransformer, mockPreflightController, mockPortalGinInjector);
		mockBundle = mock(EntityBundle.class);
		annotationList = new ArrayList<Annotation>();
		annotationList.add(new Annotation(ANNOTATION_TYPE.STRING, "key", Collections.EMPTY_LIST));
		when(mockAnnotationTransformer.annotationsToList(any(Annotations.class))).thenReturn(annotationList);
	}

	@Test
	public void testConfigureEmptyAnnotations() {
		//also verify construction
		verify(mockView).setPresenter(widget);
		
		annotationList.clear();
		boolean canEdit = true;
		boolean isCurrentVersion = true;
		widget.configure(mockBundle, canEdit, isCurrentVersion);
		
		verify(mockView).showNoAnnotations();
		verify(mockView).setEditUIVisible(true);
		
		assertTrue(widget.isEmpty());
	}
	
	@Test
	public void testConfigureAnnotations() {
		boolean canEdit = false;
		boolean isCurrentVersion = true;
		widget.configure(mockBundle, canEdit, isCurrentVersion);
		
		verify(mockView).configure(annotationList);
		verify(mockView).setEditUIVisible(false);
		
		assertFalse(widget.isEmpty());
	}
	@Test
	public void testConfigureAnnotationsNotCurrentVersion() {
		boolean canEdit = true;
		boolean isCurrentVersion = false;
		widget.configure(mockBundle, canEdit, isCurrentVersion);
		
		verify(mockView).configure(annotationList);
		verify(mockView).setEditUIVisible(false);
		
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
		widget.configure(mockBundle, true, true);
		
		//test that on edit, we pass the bundle and update handler to the edit dialog
		widget.onEdit();
		
		ArgumentCaptor<EntityBundle> bundleCaptor = ArgumentCaptor.forClass(EntityBundle.class);
		
		verify(mockView).addEditorToPage(any(Widget.class));
		verify(mockEditAnnotationsDialog).configure(bundleCaptor.capture());
		
		assertEquals(bundleCaptor.getValue(), mockBundle);
	}
	
	@Test
	public void testOnEditFailedPreflight() {
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		widget.configure(mockBundle, true, true);
		//test that on edit, we pass the bundle and update handler to the edit dialog
		widget.onEdit();
		
		verify(mockEditAnnotationsDialog, never()).configure(any(EntityBundle.class));
	}
	

}