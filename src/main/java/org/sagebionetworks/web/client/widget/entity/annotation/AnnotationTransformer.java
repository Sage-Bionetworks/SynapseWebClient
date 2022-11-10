package org.sagebionetworks.web.client.widget.entity.annotation;

import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValue;

public interface AnnotationTransformer {
  String getFriendlyValues(AnnotationsValue annotation);
}
