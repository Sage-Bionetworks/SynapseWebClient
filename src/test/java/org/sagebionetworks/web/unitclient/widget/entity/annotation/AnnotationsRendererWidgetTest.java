package org.sagebionetworks.web.unitclient.widget.entity.annotation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationTransformer;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationsRendererWidget;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationsRendererWidgetView;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;

public class AnnotationsRendererWidgetTest {
	
	AnnotationsRendererWidget widget;
	
	AnnotationsRendererWidgetView mockView;
	AnnotationTransformer mockAnnotationTransformer;
	EntityBundle mockBundle;
	List<Annotation> annotationList;
	@Before
	public void setUp() throws Exception {
		mockView = mock(AnnotationsRendererWidgetView.class);
		mockAnnotationTransformer = mock(AnnotationTransformer.class);
		widget = new AnnotationsRendererWidget(mockView, mockAnnotationTransformer);
		mockBundle = mock(EntityBundle.class);
		annotationList = new ArrayList<Annotation>();
		annotationList.add(new Annotation(ANNOTATION_TYPE.STRING, "key", Collections.EMPTY_LIST));
		when(mockAnnotationTransformer.annotationsToList(any(Annotations.class))).thenReturn(annotationList);
	}

	@Test
	public void testConfigureEmptyAnnotations() {
		//also verify construction
		annotationList.clear();
		widget.configure(mockBundle);
		
		verify(mockView).showNoAnnotations();
		
		assertTrue(widget.isEmpty());
	}
	
	@Test
	public void testConfigureAnnotations() {
		widget.configure(mockBundle);
		
		verify(mockView).configure(annotationList);
		
		assertFalse(widget.isEmpty());
	}
	
	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
}
