package org.sagebionetworks.web.unitclient.widget.entity.annotation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValue;
import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValueType;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationCellFactoryImpl;


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
		AnnotationsValue annotation = new AnnotationsValue();
		annotation.setType(AnnotationsValueType.LONG);
		annotation.setValue(Collections.EMPTY_LIST);
		factory.createEditor(annotation);
		verify(mockPortalGinInjector).createIntegerCellEditor();
	}

	@Test
	public void testCreateEditorDouble() {
		AnnotationsValue annotation = new AnnotationsValue();
		annotation.setType(AnnotationsValueType.DOUBLE);
		annotation.setValue(Collections.EMPTY_LIST);
		factory.createEditor(annotation);
		verify(mockPortalGinInjector).createDoubleCellEditor();
	}

	@Test
	public void testCreateEditorDate() {
		AnnotationsValue annotation = new AnnotationsValue();
		annotation.setType(AnnotationsValueType.TIMESTAMP_MS);
		annotation.setValue(Collections.EMPTY_LIST);
		factory.createEditor(annotation);
		verify(mockPortalGinInjector).createDateCellEditor();
	}

	@Test
	public void testCreateEditorString() {
		AnnotationsValue annotation = new AnnotationsValue();
		annotation.setType(AnnotationsValueType.STRING);
		annotation.setValue(Collections.EMPTY_LIST);
		factory.createEditor(annotation);
		verify(mockPortalGinInjector).createStringEditorCell();
	}
}
