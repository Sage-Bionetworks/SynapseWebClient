package org.sagebionetworks.web.client.widget.entity.row;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Render entity annotations
 */
public class AnnotationsRendererWidget implements AnnotationsRendererWidgetView.Presenter, IsWidget {

	private Annotations annotations;
	private AnnotationsRendererWidgetView view;
	private AnnotationTransformer annotationTransformer;
	EntityUpdatedHandler entityUpdatedHandler;

	/**
	 * 
	 * @param factory
	 * @param cache
	 * @param propertyView
	 */
	@Inject
	public AnnotationsRendererWidget(AnnotationsRendererWidgetView propertyView, AnnotationTransformer annotationTransformer) {
		super();
		this.view = propertyView;
		this.annotationTransformer = annotationTransformer;
		this.view.setPresenter(this);
	}

	@Override
	public void configure(EntityBundle bundle, boolean canEdit) {
		this.annotations = bundle.getAnnotations();
		view.configure(annotationTransformer.annotationsToList(annotations));
		view.setEditButtonVisible(canEdit);
	}


	public boolean isEmpty() {
		return annotations.keySet().isEmpty();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void setEntityUpdatedHandler(EntityUpdatedHandler handler) {
		this.entityUpdatedHandler = handler;
	}

	@Override
	public void onEdit() {
		// TODO
	}
}
