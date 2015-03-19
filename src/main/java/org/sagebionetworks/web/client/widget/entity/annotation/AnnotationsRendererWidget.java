package org.sagebionetworks.web.client.widget.entity.annotation;

import java.util.List;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Render entity annotations
 */
public class AnnotationsRendererWidget implements AnnotationsRendererWidgetView.Presenter, IsWidget {

	private Annotations annotations;
	private EntityBundle bundle;
	private AnnotationsRendererWidgetView view;
	private AnnotationTransformer annotationTransformer;
	private EditAnnotationsDialog editorDialog;
	EntityUpdatedHandler entityUpdatedHandler;

	/**
	 * 
	 * @param factory
	 * @param cache
	 * @param propertyView
	 */
	@Inject
	public AnnotationsRendererWidget(AnnotationsRendererWidgetView propertyView, AnnotationTransformer annotationTransformer, EditAnnotationsDialog editorDialog) {
		super();
		this.view = propertyView;
		this.annotationTransformer = annotationTransformer;
		this.editorDialog = editorDialog;
		this.view.setPresenter(this);
		this.view.addEditorToPage(editorDialog.asWidget());
	}

	@Override
	public void configure(EntityBundle bundle, boolean canEdit) {
		this.bundle = bundle;
		this.annotations = bundle.getAnnotations();
		List<Annotation> annotationsList = annotationTransformer.annotationsToList(annotations);
		if (!annotationsList.isEmpty())
			view.configure(annotationsList);
		else
			view.showNoAnnotations();
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
		editorDialog.configure(bundle, entityUpdatedHandler);
	}
}
