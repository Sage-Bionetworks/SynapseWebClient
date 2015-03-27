package org.sagebionetworks.web.client.widget.entity.annotation;

import java.util.List;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Render entity annotations
 */
public class AnnotationsRendererWidget implements AnnotationsRendererWidgetView.Presenter, IsWidget {
	private EntityBundle bundle;
	private AnnotationsRendererWidgetView view;
	private AnnotationTransformer annotationTransformer;
	private EditAnnotationsDialog editorDialog;
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
			AnnotationTransformer annotationTransformer, 
			EditAnnotationsDialog editorDialog) {
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
		annotationsList = annotationTransformer.annotationsToList(bundle.getAnnotations());
		if (!annotationsList.isEmpty())
			view.configure(annotationsList);
		else {
			view.showNoAnnotations();
		}
			
		view.setEditUIVisible(canEdit);
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

	@Override
	public void onEdit() {
		editorDialog.configure(bundle, entityUpdatedHandler);
	}
}
