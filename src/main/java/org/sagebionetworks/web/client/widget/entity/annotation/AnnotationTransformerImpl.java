package org.sagebionetworks.web.client.widget.entity.annotation;

import java.util.Date;
import java.util.List;
import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValue;
import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValueType;
import org.sagebionetworks.web.client.GWTWrapper;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.inject.Inject;

public class AnnotationTransformerImpl implements AnnotationTransformer {
	private DateTimeFormat standardFormatter;

	@Inject
	public AnnotationTransformerImpl(GWTWrapper gwt) {
		standardFormatter = gwt.getDateTimeFormat(PredefinedFormat.DATE_TIME_MEDIUM);
	}

	@Override
	public String getFriendlyValues(AnnotationsValue annotation) {
		List<String> values = annotation.getValue();
		StringBuilder builder = new StringBuilder();
		boolean isAfterFirst = false;
		boolean isDate = AnnotationsValueType.TIMESTAMP_MS.equals(annotation.getType());
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
