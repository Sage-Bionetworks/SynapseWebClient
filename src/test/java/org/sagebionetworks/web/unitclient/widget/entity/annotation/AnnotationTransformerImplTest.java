package org.sagebionetworks.web.unitclient.widget.entity.annotation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
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
		when(mockGWTWrapper.getDateTimeFormat(any(PredefinedFormat.class))).thenReturn(mockFormat);
		transformer = new AnnotationTransformerImpl(mockGWTWrapper);
	}

	@Test
	public void testNumbersToStrings() {
		//couple Longs
		List<Long> list = new ArrayList<Long>();
		list.add(4L);
		list.add(8L);
		List<String> stringList = transformer.numbersToStrings(list);
		assertEquals(2, stringList.size());
		assertEquals("4", stringList.get(0));
		assertEquals("8", stringList.get(1));
		
		//empty
		assertTrue(transformer.numbersToStrings(new ArrayList<Long>()).isEmpty());
		
		//single double
		Double d = 3.14;
		assertEquals(d.toString(), transformer.numbersToStrings(Collections.singletonList(d)).get(0));
	}

	@Test
	public void testDatesToStrings() {
		//empty
		assertTrue(transformer.datesToStrings(new ArrayList<Date>()).isEmpty());
		
		//date should be converted to string representation of time
		Date d = new Date();
		Long datetime = d.getTime();
		assertEquals(datetime.toString(), transformer.datesToStrings(Collections.singletonList(d)).get(0));
	}

	@Test
	public void testAnnotationsToList() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateAnnotationsFromList() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetDoubles() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetLongs() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetDates() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetFriendlyValues() {
		fail("Not yet implemented");
	}

	@Test
	public void testFriendlyDate() {
		fail("Not yet implemented");
	}

}
