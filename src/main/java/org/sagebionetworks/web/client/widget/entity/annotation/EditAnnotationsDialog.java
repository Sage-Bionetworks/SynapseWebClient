package org.sagebionetworks.web.client.widget.entity.annotation;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.exceptions.DuplicateKeyException;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EditAnnotationsDialog implements EditAnnotationsDialogView.Presenter {
	public static final String SEE_THE_ERRORS_ABOVE = "See the error(s) above.";
	EditAnnotationsDialogView view;
	SynapseClientAsync synapseClient;
	AnnotationTransformer transformer;
	PortalGinInjector ginInjector;
	String entityId;
	List<AnnotationEditor> annotationEditors;
	Annotations annotationsCopy;
	
	@Inject
	public EditAnnotationsDialog(EditAnnotationsDialogView view, 
			SynapseClientAsync synapseClient, 
			AnnotationTransformer transformer, 
			PortalGinInjector ginInjector)  {
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.transformer = transformer;
		this.ginInjector = ginInjector;
		view.setPresenter(this);
	}

	@Override
	public void configure(EntityBundle bundle) {
		view.clearAnnotationEditors();
		entityId = bundle.getEntity().getId();
		annotationsCopy = new Annotations();
		Annotations originalAnnotations = bundle.getAnnotations();
		annotationsCopy.setId(originalAnnotations.getId());
		annotationsCopy.setEtag(originalAnnotations.getEtag());
		annotationsCopy.addAll(originalAnnotations);
		List<Annotation> annotationList = transformer.annotationsToList(bundle.getAnnotations());
		annotationEditors = new ArrayList<AnnotationEditor>();
		for (Annotation annotation : annotationList) {
			AnnotationEditor newEditor = createAnnotationEditor(annotation);
			view.addAnnotationEditor(newEditor.asWidget());
		}
		//if there are no annotations, prepopulate with a single default annotation
		if (annotationList.isEmpty())
			onAddNewAnnotation();
		view.showEditor();
	}
	
	public AnnotationEditor createAnnotationEditor(Annotation annotation) {
		final AnnotationEditor editor = ginInjector.getAnnotationEditor();
		Callback deletedCallback = new Callback() {
			@Override
			public void invoke() {
				onAnnotationDeleted(editor);
			}
		};
		editor.configure(annotation, deletedCallback);
		annotationEditors.add(editor);
		return editor;
	}
	
	@Override
	public void onAddNewAnnotation() {
		AnnotationEditor newEditor = createAnnotationEditor(ANNOTATION_TYPE.STRING);
		view.addAnnotationEditor(newEditor.asWidget());
	}
	
	public AnnotationEditor createAnnotationEditor(ANNOTATION_TYPE type) {
		List<String> valuesList = new ArrayList<String>();
		valuesList.add("");
		Annotation newAnnotation = new Annotation(type, "", valuesList);
		return createAnnotationEditor(newAnnotation);
	}
	
	@Override
	public void onAnnotationDeleted(AnnotationEditor editor) {
		annotationEditors.remove(editor);
		view.removeAnnotationEditor(editor.asWidget());
	}
	
	@Override
	public void onCancel() {
		view.hideEditor();	
	};
	@Override
	public void onSave() {
		//check all annotation editor validity
		boolean isValid = true;
		for (AnnotationEditor annotationEditor : annotationEditors) {
			if (!annotationEditor.isValid()) {
				isValid = false;
			}
		}
		if (!isValid) {
			view.showError(SEE_THE_ERRORS_ABOVE);
			return;
		}
		
		//else, try to update the annotations
		view.setLoading();
		List<Annotation> updatedAnnotationsList = new ArrayList<Annotation>();
		for (AnnotationEditor annotationEditor : annotationEditors) {
			updatedAnnotationsList.add(annotationEditor.getUpdatedAnnotation());
		}
		try {
			transformer.updateAnnotationsFromList(annotationsCopy, updatedAnnotationsList);
			
			synapseClient.updateAnnotations(entityId, annotationsCopy, new AsyncCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					view.showInfo("Successfully updated the annotations");
					view.hideEditor();
					ginInjector.getEventBus().fireEvent(new EntityUpdatedEvent());
				}
				
				@Override
				public void onFailure(Throwable caught) {
					view.showError(caught.getMessage());
				}
			});
		} catch (DuplicateKeyException e) {
			view.showError(e.getMessage());
		}
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}
	
	/**
	 * For testing purposes only
	 */
	public Annotations getAnnotationsCopy() {
		return annotationsCopy;
	}
	
	/**
	 * For testing purposes only
	 */
	public List<AnnotationEditor> getAnnotationEditors() {
		return annotationEditors;
	}
}
