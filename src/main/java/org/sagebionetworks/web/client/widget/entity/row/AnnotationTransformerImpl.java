package org.sagebionetworks.web.client.widget.entity.row;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.inject.Inject;

public class AnnotationTransformerImpl implements AnnotationTransformer {
	private DateTimeFormat standardFormatter;

	@Inject
	public AnnotationTransformerImpl(GWTWrapper gwt) {
		standardFormatter = gwt.getDateTimeFormat(PredefinedFormat.DATE_TIME_MEDIUM);
	}

	public List<String> numbersToStrings(List<? extends Number> list) {
		List<String> stringList = new ArrayList<String>(list.size());
		for (Number v : list) {
			stringList.add(v.toString());
		}
		return stringList;
	}

	public List<String> datesToStrings(List<Date> list) {
		List<String> stringList = new ArrayList<String>(list.size());
		for (Date v : list) {
			stringList.add(standardFormatter.format(v));
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
	public Annotations listToAnnotationsToList(List<Annotation> annotationsList) {
		///TODO
		return null;
	}
}
