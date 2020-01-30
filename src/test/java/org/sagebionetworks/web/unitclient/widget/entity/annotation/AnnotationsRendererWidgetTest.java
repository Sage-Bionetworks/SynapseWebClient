package org.sagebionetworks.web.unitclient.widget.entity.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.annotation.v2.Annotations;
import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValue;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationTransformer;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationsRendererWidget;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationsRendererWidgetView;
import org.sagebionetworks.web.client.widget.entity.annotation.EditAnnotationsDialog;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
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
	Map<String, AnnotationsValue> annotationMap;
	@Mock
	PreflightController mockPreflightController;
	@Mock
	PortalGinInjector mockPortalGinInjector;
	@Mock
	Annotations mockAnnotations;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(mockPortalGinInjector.getEditAnnotationsDialog()).thenReturn(mockEditAnnotationsDialog);
		widget = new AnnotationsRendererWidget(mockView, mockPreflightController, mockPortalGinInjector);
		annotationMap = new HashMap<String, AnnotationsValue>();
		AnnotationsValue value = new AnnotationsValue();
		value.setValue(Collections.EMPTY_LIST);
		annotationMap.put("key", value);
		when(mockBundle.getAnnotations()).thenReturn(mockAnnotations);
		when(mockAnnotations.getAnnotations()).thenReturn(annotationMap);
	}

	@Test
	public void testConfigureEmptyAnnotations() {
		// also verify construction
		verify(mockView).setPresenter(widget);

		annotationMap.clear();
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

		verify(mockView).configure(annotationMap);
		verify(mockView).setEditUIVisible(false);

		assertFalse(widget.isEmpty());
	}

	@Test
	public void testConfigureAnnotationsNotCurrentVersion() {
		boolean canEdit = true;
		boolean isCurrentVersion = false;
		widget.configure(mockBundle, canEdit, isCurrentVersion);

		verify(mockView).configure(annotationMap);
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

		// test that on edit, we pass the bundle and update handler to the edit dialog
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
		// test that on edit, we pass the bundle and update handler to the edit dialog
		widget.onEdit();

		verify(mockEditAnnotationsDialog, never()).configure(any(EntityBundle.class));
	}


}
