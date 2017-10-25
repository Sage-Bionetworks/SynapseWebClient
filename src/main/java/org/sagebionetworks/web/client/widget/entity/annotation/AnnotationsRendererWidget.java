package org.sagebionetworks.web.client.widget.entity.annotation;

import java.util.List;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Render entity annotations
 */
public class AnnotationsRendererWidget implements IsWidget {
	private AnnotationsRendererWidgetView view;
	private AnnotationTransformer annotationTransformer;
	EntityUpdatedHandler entityUpdatedHandler;
	List<Annotation> annotationsList;
	
	/**
	 * 
	 * @param factory
	 * @param cache
	 * @param propertyView
	 */
	@Inject
	public AnnotationsRendererWidget(AnnotationsRendererWidgetView propertyView, 
			AnnotationTransformer annotationTransformer) {
		super();
		this.view = propertyView;
		this.annotationTransformer = annotationTransformer;
	}

	public void configure(EntityBundle bundle) {
		annotationsList = annotationTransformer.annotationsToList(bundle.getAnnotations());
		if (!annotationsList.isEmpty())
			view.configure(annotationsList);
		else {
			view.showNoAnnotations();
		}
	}

	public boolean isEmpty() {
		return annotationsList.isEmpty();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void setEntityUpdatedHandler(EntityUpdatedHandler handler) {
		this.entityUpdatedHandler = handler;
	}
}
