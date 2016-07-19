package org.sagebionetworks.web.client.widget.entity.annotation;

import java.util.List;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.web.client.exceptions.DuplicateKeyException;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;

/**
 * Convert between Annotations object and a list of Annotation objects (triples) 
 */
public interface AnnotationTransformer {
	List<Annotation> annotationsToList(Annotations annotations);
	void updateAnnotationsFromList(Annotations annotations, List<Annotation> annotationsList) throws DuplicateKeyException;
	
	String getFriendlyValues(Annotation annotation);
}
