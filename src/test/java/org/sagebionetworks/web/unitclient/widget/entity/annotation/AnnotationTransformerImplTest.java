package org.sagebionetworks.web.unitclient.widget.entity.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.exceptions.DuplicateKeyException;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationTransformerImpl;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

public class AnnotationTransformerImplTest {
	GWTWrapper mockGWTWrapper;
	AnnotationTransformerImpl transformer;
	DateTimeFormat mockFormat;
	
	Annotations initializedAnnotations;
	Map<String, List<Double>> doubleAnnotationsBefore;
	Map<String, List<String>> stringAnnotationsBefore;
	Map<String, List<Long>> longAnnotationsBefore;
	Map<String, List<Date>> dateAnnotationsBefore;
	
	@Before
	public void setUp() throws Exception {
		mockGWTWrapper = mock(GWTWrapper.class);
		mockFormat = mock(DateTimeFormat.class);
		when(mockGWTWrapper.getDateTimeFormat(any(PredefinedFormat.class))).thenReturn(mockFormat);
		transformer = new AnnotationTransformerImpl(mockGWTWrapper);
		
		
		//initalize annotations
		doubleAnnotationsBefore = new LinkedHashMap<String, List<Double>>();
		stringAnnotationsBefore = new LinkedHashMap<String, List<String>>();
		longAnnotationsBefore = new LinkedHashMap<String, List<Long>>();
		dateAnnotationsBefore = new LinkedHashMap<String, List<Date>>();
		initializedAnnotations = new Annotations();
		initializedAnnotations.setId("98208");
		initializedAnnotations.setDoubleAnnotations(doubleAnnotationsBefore);
		initializedAnnotations.setStringAnnotations(stringAnnotationsBefore);
		initializedAnnotations.setLongAnnotations(longAnnotationsBefore);
		initializedAnnotations.setDateAnnotations(dateAnnotationsBefore);
		
		doubleAnnotationsBefore.put("doublekey1", Collections.singletonList(4.5));
		stringAnnotationsBefore.put("stringkey1", Collections.singletonList("abc"));
		longAnnotationsBefore.put("longkey1", Collections.singletonList(4L));
		dateAnnotationsBefore.put("datekey1", Collections.singletonList(new Date()));
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
	public void testAnnotationsToListStrings() {
		//with order check
		Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
		String k1="key1";
		String k2="key2";
		String v2="value2";
		String k3="key3";
		String v3a="value3a";
		String v3b="value3b";
		List<String> v3List = new ArrayList<String>();
		v3List.add(v3a);
		v3List.add(v3b);
		
		List<String> v2List = Collections.singletonList(v2);
		map.put(k1, Collections.EMPTY_LIST);
		map.put(k2, v2List);
		map.put(k3, v3List);
		
		Annotations annotations = new Annotations();
		annotations.setStringAnnotations(map);
		
		List<Annotation> result = transformer.annotationsToList(annotations);
		
		assertEquals(3, result.size());
		
		assertEquals(ANNOTATION_TYPE.STRING, result.get(0).getType());
		assertEquals(k1, result.get(0).getKey());
		assertEquals(Collections.EMPTY_LIST, result.get(0).getValues());
		
		assertEquals(k2, result.get(1).getKey());
		assertEquals(v2List, result.get(1).getValues());
		
		assertEquals(k3, result.get(2).getKey());
		assertEquals(v3List, result.get(2).getValues());
	}
	
	@Test
	public void testAnnotationsToListLongs() {
		//with order check
		Map<String, List<Long>> map = new LinkedHashMap<String, List<Long>>();
		String k1="key1";
		String k2="key2";
		Long v2=2L;
		String k3="key3";
		Long v3a=30L;
		Long v3b=31L;
		List<Long> v3List = new ArrayList<Long>();
		v3List.add(v3a);
		v3List.add(v3b);
		
		List<Long> v2List = Collections.singletonList(v2);
		map.put(k1, Collections.EMPTY_LIST);
		map.put(k2, v2List);
		map.put(k3, v3List);
		
		Annotations annotations = new Annotations();
		annotations.setLongAnnotations(map);
		
		List<Annotation> result = transformer.annotationsToList(annotations);
		
		assertEquals(3, result.size());
		
		assertEquals(ANNOTATION_TYPE.LONG, result.get(0).getType());
		
		assertEquals(k1, result.get(0).getKey());
		assertEquals(Collections.EMPTY_LIST, result.get(0).getValues());
		
		assertEquals(k2, result.get(1).getKey());
		assertEquals(transformer.numbersToStrings(v2List), result.get(1).getValues());
		
		assertEquals(k3, result.get(2).getKey());
		assertEquals(transformer.numbersToStrings(v3List), result.get(2).getValues());
	}
	
	@Test
	public void testAnnotationsToListDoubles() {
		//with order check
		Map<String, List<Double>> map = new LinkedHashMap<String, List<Double>>();
		String k1="key1";
		String k2="key2";
		Double v2=2.2;
		String k3="key3";
		Double v3a=3.1;
		Double v3b=3.2;
		List<Double> v3List = new ArrayList<Double>();
		v3List.add(v3a);
		v3List.add(v3b);
		
		List<Double> v2List = Collections.singletonList(v2);
		map.put(k1, Collections.EMPTY_LIST);
		map.put(k2, v2List);
		map.put(k3, v3List);
		
		Annotations annotations = new Annotations();
		annotations.setDoubleAnnotations(map);
		
		List<Annotation> result = transformer.annotationsToList(annotations);
		
		assertEquals(3, result.size());
		
		assertEquals(ANNOTATION_TYPE.DOUBLE, result.get(0).getType());
		
		assertEquals(k1, result.get(0).getKey());
		assertEquals(Collections.EMPTY_LIST, result.get(0).getValues());
		
		assertEquals(k2, result.get(1).getKey());
		assertEquals(transformer.numbersToStrings(v2List), result.get(1).getValues());
		
		assertEquals(k3, result.get(2).getKey());
		assertEquals(transformer.numbersToStrings(v3List), result.get(2).getValues());
	}
	
	@Test
	public void testAnnotationsToListDates() {
		//with order check
		Map<String, List<Date>> map = new LinkedHashMap<String, List<Date>>();
		String k1="key1";
		String k2="key2";
		Date v2=new Date(12345);
		String k3="key3";
		Date v3a=new Date(6789);
		Date v3b=new Date(2222);
		List<Date> v3List = new ArrayList<Date>();
		v3List.add(v3a);
		v3List.add(v3b);
		
		List<Date> v2List = Collections.singletonList(v2);
		map.put(k1, Collections.EMPTY_LIST);
		map.put(k2, v2List);
		map.put(k3, v3List);
		
		Annotations annotations = new Annotations();
		annotations.setDateAnnotations(map);
		
		List<Annotation> result = transformer.annotationsToList(annotations);
		
		assertEquals(3, result.size());
		
		assertEquals(ANNOTATION_TYPE.DATE, result.get(0).getType());
		
		assertEquals(k1, result.get(0).getKey());
		assertEquals(Collections.EMPTY_LIST, result.get(0).getValues());
		
		assertEquals(k2, result.get(1).getKey());
		assertEquals(transformer.datesToStrings(v2List), result.get(1).getValues());
		
		assertEquals(k3, result.get(2).getKey());
		assertEquals(transformer.datesToStrings(v3List), result.get(2).getValues());
	}

	@Test
	public void testUpdateAnnotationsFromList() throws DuplicateKeyException {
		//update annotations
		String stringKey = "string";
		String stringValue = "banana";
		String dateKey = "date";
		Date dateValue = new Date();
		Long dateTimeValue = dateValue.getTime();
		String doubleKey = "double";
		Double doubleValue = 6.28;
		String longKey = "long";
		Long longValue = 42L;
		
		List<Annotation> annotationsList = new ArrayList<Annotation>();
		annotationsList.add(new Annotation(ANNOTATION_TYPE.DATE, dateKey, Collections.singletonList(Long.toString(dateTimeValue))));
		annotationsList.add(new Annotation(ANNOTATION_TYPE.DOUBLE, doubleKey, Collections.singletonList(Double.toString(doubleValue))));
		annotationsList.add(new Annotation(ANNOTATION_TYPE.LONG, longKey, Collections.singletonList(Long.toString(longValue))));
		annotationsList.add(new Annotation(ANNOTATION_TYPE.STRING, stringKey, Collections.singletonList(stringValue)));
		
		String idBefore = initializedAnnotations.getId();
		
		transformer.updateAnnotationsFromList(initializedAnnotations, annotationsList);
		
		assertEquals(idBefore, initializedAnnotations.getId());
		
		Map<String, List<Double>> doubleAnnotationsAfter = initializedAnnotations.getDoubleAnnotations();
		Map<String, List<String>> stringAnnotationsAfter = initializedAnnotations.getStringAnnotations();
		Map<String, List<Long>> longAnnotationsAfter = initializedAnnotations.getLongAnnotations();
		Map<String, List<Date>> dateAnnotationsAfter = initializedAnnotations.getDateAnnotations();
		
		assertTrue(doubleAnnotationsAfter.containsKey(doubleKey));
		assertTrue(stringAnnotationsAfter.containsKey(stringKey));
		assertTrue(longAnnotationsAfter.containsKey(longKey));
		assertTrue(dateAnnotationsAfter.containsKey(dateKey));
		
		assertEquals(doubleValue, doubleAnnotationsAfter.get(doubleKey).get(0));
		assertEquals(stringValue, stringAnnotationsAfter.get(stringKey).get(0));
		assertEquals(longValue, longAnnotationsAfter.get(longKey).get(0));
		assertEquals(dateValue, dateAnnotationsAfter.get(dateKey).get(0));
	}
	
	@Test (expected=DuplicateKeyException.class)
	public void testUpdateAnnotationsFromListDuplicateStringKey() throws DuplicateKeyException {
		String key = "string";
		List<Annotation> annotationsList = new ArrayList<Annotation>();
		annotationsList.add(new Annotation(ANNOTATION_TYPE.STRING, key, Collections.singletonList("value1")));
		annotationsList.add(new Annotation(ANNOTATION_TYPE.STRING, key, Collections.singletonList("value2")));
		transformer.updateAnnotationsFromList(initializedAnnotations, annotationsList);
	}
	
	@Test (expected=DuplicateKeyException.class)
	public void testUpdateAnnotationsFromListDuplicateDateKey() throws DuplicateKeyException {
		String key = "date";
		List<Annotation> annotationsList = new ArrayList<Annotation>();
		annotationsList.add(new Annotation(ANNOTATION_TYPE.DATE, key, Collections.singletonList(Long.toString(new Date().getTime()))));
		annotationsList.add(new Annotation(ANNOTATION_TYPE.DATE, key, Collections.singletonList(Long.toString(new Date().getTime()))));
		transformer.updateAnnotationsFromList(initializedAnnotations, annotationsList);
	}
	
	@Test (expected=DuplicateKeyException.class)
	public void testUpdateAnnotationsFromListDuplicateDoubleKey() throws DuplicateKeyException {
		String key = "double";
		List<Annotation> annotationsList = new ArrayList<Annotation>();
		annotationsList.add(new Annotation(ANNOTATION_TYPE.DOUBLE, key, Collections.singletonList(Double.toString(6.28))));
		annotationsList.add(new Annotation(ANNOTATION_TYPE.DOUBLE, key, Collections.singletonList(Double.toString(3.14))));
		transformer.updateAnnotationsFromList(initializedAnnotations, annotationsList);
	}
	@Test (expected=DuplicateKeyException.class)
	public void testUpdateAnnotationsFromListDuplicateLongKey() throws DuplicateKeyException {
		String key = "long";
		List<Annotation> annotationsList = new ArrayList<Annotation>();
		annotationsList.add(new Annotation(ANNOTATION_TYPE.LONG, key, Collections.singletonList(Long.toString(2016L))));
		annotationsList.add(new Annotation(ANNOTATION_TYPE.LONG, key, Collections.singletonList(Long.toString(1977L))));
		transformer.updateAnnotationsFromList(initializedAnnotations, annotationsList);
	}
	
	public void testUpdateAnnotationsFromListDuplicateKeyDifferentTypes() throws DuplicateKeyException {
		String key = "dupekey";
		List<Annotation> annotationsList = new ArrayList<Annotation>();
		String stringValue = "2001";
		Long longValue = 1977L;
		annotationsList.add(new Annotation(ANNOTATION_TYPE.STRING, key, Collections.singletonList(stringValue)));
		annotationsList.add(new Annotation(ANNOTATION_TYPE.LONG, key, Collections.singletonList(Long.toString(longValue))));
		transformer.updateAnnotationsFromList(initializedAnnotations, annotationsList);
		Map<String, List<String>> stringAnnotationsAfter = initializedAnnotations.getStringAnnotations();
		Map<String, List<Long>> longAnnotationsAfter = initializedAnnotations.getLongAnnotations();
		assertTrue(stringAnnotationsAfter.containsKey(key));
		assertTrue(longAnnotationsAfter.containsKey(key));
		
		assertEquals(stringValue, stringAnnotationsAfter.get(key).get(0));
		assertEquals(longValue, longAnnotationsAfter.get(key).get(0));
	}

	
	@Test
	public void testGetDoubles() {
		List<String> testList = new ArrayList<String>();
		List<Double> result = transformer.getDoubles(testList);
		assertTrue(result.isEmpty());
		
		testList.add("3.14");
		result = transformer.getDoubles(testList);
		assertEquals(1, result.size());
		
		testList.add("6.28");
		result = transformer.getDoubles(testList);
		assertEquals(2, result.size());
		
		//and add an invalid value, expect parse error
		try {
			testList.add("foo");
			result = transformer.getDoubles(testList);
			fail("Expected a NumberFormatException");
		} catch (NumberFormatException e) {
			//expected
		}
	}

	@Test
	public void testGetLongs() {
		List<String> testList = new ArrayList<String>();
		List<Long> result = transformer.getLongs(testList);
		assertTrue(result.isEmpty());
		
		testList.add("2");
		result = transformer.getLongs(testList);
		assertEquals(1, result.size());
		
		testList.add("4");
		result = transformer.getLongs(testList);
		assertEquals(2, result.size());
		
		//and add an invalid value, expect parse error
		try {
			testList.add("foo");
			result = transformer.getLongs(testList);
			fail("Expected a NumberFormatException");
		} catch (NumberFormatException e) {
			//expected
		}
	}

	@Test
	public void testGetDates() {
		List<String> testList = new ArrayList<String>();
		List<Long> result = transformer.getLongs(testList);
		assertTrue(result.isEmpty());
		
		testList.add(Long.toString(new Date().getTime()));
		result = transformer.getLongs(testList);
		assertEquals(1, result.size());
		
		testList.add(Long.toString(new Date().getTime()));
		result = transformer.getLongs(testList);
		assertEquals(2, result.size());
		
		//and add an invalid value, expect parse error
		try {
			testList.add("foo");
			result = transformer.getLongs(testList);
			fail("Expected a NumberFormatException");
		} catch (NumberFormatException e) {
			//expected
		}
	}

	@Test
	public void testGetFriendlyValues() {
		List<String> values = new ArrayList<String>();
		Annotation annotation = new Annotation(ANNOTATION_TYPE.LONG, "key", values);
		//test empty values
		assertEquals(0, transformer.getFriendlyValues(annotation).length());
		
		//test single value
		String v1 = "128";
		values.add(v1);
		assertEquals(v1, transformer.getFriendlyValues(annotation));
		
		//test multiple value
		String v2 = "256";
		values.add(v2);
		String result = transformer.getFriendlyValues(annotation);
		assertTrue(result.contains(v1));
		assertTrue(result.contains(v2));
		assertTrue(result.contains(","));
	}

	@Test
	public void testFriendlyDateValues() {
		String formattedDate = "October 2, 2015";
		when(mockFormat.format(any(Date.class))).thenReturn(formattedDate);
		
		List<String> values = new ArrayList<String>();
		Annotation annotation = new Annotation(ANNOTATION_TYPE.DATE, "key", values);
		//test empty values
		assertEquals(0, transformer.getFriendlyValues(annotation).length());
		
		values.add("12345");
		assertEquals(formattedDate, transformer.getFriendlyValues(annotation));
	}

}
