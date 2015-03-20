package org.sagebionetworks.web.unitclient.widget.entity.annotation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationCellFactoryImpl;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;


public class AnnotationCellFactoryImplTest {
	PortalGinInjector mockPortalGinInjector;
	AnnotationCellFactoryImpl factory;
	
	@Before
	public void setUp() throws Exception {
		mockPortalGinInjector = mock(PortalGinInjector.class);
		factory = new AnnotationCellFactoryImpl(mockPortalGinInjector);
	}

	@Test
	public void testCreateEditorLong() {
		Annotation annotation = new Annotation(ANNOTATION_TYPE.LONG, "key", null);
		factory.createEditor(annotation);
		verify(mockPortalGinInjector).createIntegerCellEditor();
	}
	
	@Test
	public void testCreateEditorDouble() {
		Annotation annotation = new Annotation(ANNOTATION_TYPE.DOUBLE, "key", null);
		factory.createEditor(annotation);
		verify(mockPortalGinInjector).createDoubleCellEditor();
	}
	
	@Test
	public void testCreateEditorDate() {
		Annotation annotation = new Annotation(ANNOTATION_TYPE.DATE, "key", null);
		factory.createEditor(annotation);
		verify(mockPortalGinInjector).createDateCellEditor();
	}
	@Test
	public void testCreateEditorString() {
		Annotation annotation = new Annotation(ANNOTATION_TYPE.STRING, "key", null);
		factory.createEditor(annotation);
		verify(mockPortalGinInjector).createStringEditorCell();
	}
}
