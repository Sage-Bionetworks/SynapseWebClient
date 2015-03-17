package org.sagebionetworks.web.client.widget.entity.row;

import java.util.List;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;

/**
 * Convert between Annotations object and a list of Annotation objects (triples) 
 */
public interface AnnotationTransformer {
	List<Annotation> annotationsToList(Annotations annotations);
	Annotations listToAnnotationsToList(List<Annotation> annotationsList);
}
