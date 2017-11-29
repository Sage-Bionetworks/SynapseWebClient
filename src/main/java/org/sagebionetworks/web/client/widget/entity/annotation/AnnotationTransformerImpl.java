package org.sagebionetworks.web.client.widget.entity.annotation;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.exceptions.DuplicateKeyException;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.inject.Inject;

public class AnnotationTransformerImpl implements AnnotationTransformer {
	private static final String KEY = "Key \'";
	private static final String ALREADY_DEFINED = "\' has multiple definitions.";
	private DateTimeFormat standardFormatter;

	@Inject
	public AnnotationTransformerImpl(GWTWrapper gwt) {
		standardFormatter = gwt.getDateTimeFormat(PredefinedFormat.DATE_TIME_MEDIUM);
	}

	public List<String> numbersToStrings(List<? extends Number> list) {
		List<String> stringList = new ArrayList<String>(list.size());
		for (Number v : list) {
			String value = v == null ? null : v.toString();
			stringList.add(value);
		}
		return stringList;
	}

	public List<String> datesToStrings(List<Date> list) {
		List<String> stringList = new ArrayList<String>(list.size());
		for (Date v : list) {
			String value = v == null ? null : Long.toString(v.getTime());
			stringList.add(value);
		}
		return stringList;
	}

	@Override
	public List<Annotation> annotationsToList(Annotations annos) {
		List<Annotation> results = new ArrayList<Annotation>();
		// Add all strings.
		if (annos != null) {
			// Strings
			if (annos.getStringAnnotations() != null) {
				for (String key : annos.getStringAnnotations().keySet()) {
					results.add(new Annotation(ANNOTATION_TYPE.STRING, key, annos.getStringAnnotations().get(key)));
				}
			}
			// Longs
			if (annos.getLongAnnotations() != null) {
				for (String key : annos.getLongAnnotations().keySet()) {
					List<String> values = numbersToStrings(annos.getLongAnnotations().get(key));
					results.add(new Annotation(ANNOTATION_TYPE.LONG, key, values));
				}
			}
			// Doubles
			if (annos.getDoubleAnnotations() != null) {
				for (String key : annos.getDoubleAnnotations().keySet()) {
					List<String> values = numbersToStrings(annos.getDoubleAnnotations().get(key));
					results.add(new Annotation(ANNOTATION_TYPE.DOUBLE, key, values));
				}
			}
			// Dates
			if (annos.getDateAnnotations() != null) {
				for (String key : annos.getDateAnnotations().keySet()) {
					List<String> values = datesToStrings(annos.getDateAnnotations().get(key));
					results.add(new Annotation(ANNOTATION_TYPE.DATE, key, values));
				}
			}
		}
		return results;
	}

	@Override
	public void updateAnnotationsFromList(Annotations annotations, List<Annotation> annotationsList) throws DuplicateKeyException{
		Map<String, List<Double>> doubleAnnotations = new LinkedHashMap<String, List<Double>>();
		Map<String, List<String>> stringAnnotations = new LinkedHashMap<String, List<String>>();
		Map<String, List<Long>> longAnnotations = new LinkedHashMap<String, List<Long>>();
		Map<String, List<Date>> dateAnnotations = new LinkedHashMap<String, List<Date>>();
		
		for (Annotation annotation : annotationsList) {
			switch (annotation.getType()) {
			case DATE:
				if (dateAnnotations.containsKey(annotation.getKey())) {
					throw new DuplicateKeyException(KEY + annotation.getKey() + ALREADY_DEFINED);
				}
				dateAnnotations.put(annotation.getKey(), getDates(annotation.getValues()));
				break;
			case DOUBLE:
				if (doubleAnnotations.containsKey(annotation.getKey())) {
					throw new DuplicateKeyException(KEY + annotation.getKey() + ALREADY_DEFINED);
				}
				doubleAnnotations.put(annotation.getKey(), getDoubles(annotation.getValues()));
				break;
			case LONG:
				if (longAnnotations.containsKey(annotation.getKey())) {
					throw new DuplicateKeyException(KEY +annotation.getKey() + ALREADY_DEFINED);
				}
				longAnnotations.put(annotation.getKey(), getLongs(annotation.getValues()));
				break;
			case STRING:
				if (stringAnnotations.containsKey(annotation.getKey())) {
					throw new DuplicateKeyException(KEY +annotation.getKey() + ALREADY_DEFINED);
				}
				stringAnnotations.put(annotation.getKey(), annotation.getValues());
				break;
			}
		}
		annotations.setStringAnnotations(stringAnnotations);
		annotations.setDateAnnotations(dateAnnotations);
		annotations.setDoubleAnnotations(doubleAnnotations);
		annotations.setLongAnnotations(longAnnotations);
	}

	public List<Double> getDoubles(List<String> stringList) {
		List<Double> newList = new ArrayList<Double>(stringList.size());
		for (String s : stringList) {
			Double value = s == null ? null : Double.parseDouble(s);
			newList.add(value);
		}
		return newList;
	}
	

	public List<Long> getLongs(List<String> stringList) {
		List<Long> newList = new ArrayList<Long>(stringList.size());
		for (String s : stringList) {
			Long value = s == null ? null : Long.parseLong(s);
			newList.add(value);
		}
		return newList;
	}
	public List<Date> getDates(List<String> stringList) {
		List<Date> newList = new ArrayList<Date>(stringList.size());
		for (String s : stringList) {
			Date value = s == null ? null : new Date(Long.parseLong(s));
			newList.add(value);
		}
		return newList;
	}

	@Override
	public String getFriendlyValues(Annotation annotation) {
		List<String> values = annotation.getValues();
		StringBuilder builder = new StringBuilder();
		boolean isAfterFirst = false;
		boolean isDate = ANNOTATION_TYPE.DATE.equals(annotation.getType());
		for (String value : values) {
			if (isAfterFirst) {
				builder.append(", ");
			}
			isAfterFirst = true;
			if (isDate)
				value = friendlyDate(value);
			builder.append(value);
		}
		return builder.toString();
	}

	public String friendlyDate(String value) {
		return value == null ? null : standardFormatter.format(new Date(Long.parseLong(value)));
	}
}
