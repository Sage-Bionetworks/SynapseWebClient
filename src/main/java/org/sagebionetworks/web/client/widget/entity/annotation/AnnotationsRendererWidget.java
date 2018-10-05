package org.sagebionetworks.web.client.widget.entity.annotation;

import java.util.List;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
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
	List<Annotation> annotationsList;
	private PreflightController preflightController;
	private PortalGinInjector ginInjector;
	
	/**
	 * 
	 * @param factory
	 * @param cache
	 * @param propertyView
	 */
	@Inject
	public AnnotationsRendererWidget(AnnotationsRendererWidgetView propertyView, 
			AnnotationTransformer annotationTransformer, 
			PreflightController preflightController,
			PortalGinInjector ginInjector) {
		super();
		this.view = propertyView;
		this.annotationTransformer = annotationTransformer;
		this.ginInjector = ginInjector;
		this.preflightController = preflightController;
		this.view.setPresenter(this);
	}
	
	public EditAnnotationsDialog getEditAnnotationsDialog() {
		if (editorDialog == null) {
			editorDialog = ginInjector.getEditAnnotationsDialog();
			view.addEditorToPage(editorDialog.asWidget());			
		}
		return editorDialog;
	}

	public void configure(EntityBundle bundle, boolean canEdit, boolean isCurrentVersion) {
		this.bundle = bundle;
		annotationsList = annotationTransformer.annotationsToList(bundle.getAnnotations());
		if (!annotationsList.isEmpty())
			view.configure(annotationsList);
		else {
			view.showNoAnnotations();
		}
		view.setEditUIVisible(isCurrentVersion && canEdit);
	}


	public boolean isEmpty() {
		return annotationsList.isEmpty();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void onEdit() {
		preflightController.checkUploadToEntity(bundle, new Callback() {
			@Override
			public void invoke() {
				getEditAnnotationsDialog().configure(bundle);
			}
		});
	}
}