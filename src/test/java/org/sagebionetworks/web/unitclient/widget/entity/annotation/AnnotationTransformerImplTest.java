package org.sagebionetworks.web.unitclient.widget.entity.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValue;
import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValueType;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationTransformerImpl;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

public class AnnotationTransformerImplTest {
	GWTWrapper mockGWTWrapper;
	AnnotationTransformerImpl transformer;
	DateTimeFormat mockFormat;

	@Before
	public void setUp() throws Exception {
		mockGWTWrapper = mock(GWTWrapper.class);
		mockFormat = mock(DateTimeFormat.class);
		when(mockGWTWrapper.getDateTimeFormat(any(PredefinedFormat.class))).thenReturn(mockFormat);
		transformer = new AnnotationTransformerImpl(mockGWTWrapper);
	}

	@Test
	public void testGetFriendlyValues() {
		List<String> values = new ArrayList<String>();
		AnnotationsValue annotationValue = new AnnotationsValue();
		annotationValue.setType(AnnotationsValueType.LONG);
		annotationValue.setValue(values);
		// test empty values
		assertEquals(0, transformer.getFriendlyValues(annotationValue).length());

		// test single value
		String v1 = "128";
		values.add(v1);
		assertEquals(v1, transformer.getFriendlyValues(annotationValue));

		// test multiple value
		String v2 = "256";
		values.add(v2);
		String result = transformer.getFriendlyValues(annotationValue);
		assertTrue(result.contains(v1));
		assertTrue(result.contains(v2));
		assertTrue(result.contains(","));
	}

	@Test
	public void testFriendlyDateValues() {
		String formattedDate = "October 2, 2015";
		when(mockFormat.format(any(Date.class))).thenReturn(formattedDate);

		List<String> values = new ArrayList<String>();
		AnnotationsValue annotationValue = new AnnotationsValue();
		annotationValue.setType(AnnotationsValueType.TIMESTAMP_MS);
		annotationValue.setValue(values);

		// test empty values
		assertEquals(0, transformer.getFriendlyValues(annotationValue).length());

		values.add("12345");
		assertEquals(formattedDate, transformer.getFriendlyValues(annotationValue));
	}
}
