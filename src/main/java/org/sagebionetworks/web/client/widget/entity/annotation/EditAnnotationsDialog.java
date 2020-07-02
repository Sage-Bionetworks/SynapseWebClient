package org.sagebionetworks.web.client.widget.entity.annotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import org.sagebionetworks.repo.model.annotation.v2.Annotations;
import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValue;
import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValueType;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.widget.CommaSeparatedValuesParser;

public class EditAnnotationsDialog implements EditAnnotationsDialogView.Presenter {
	public static final String SEE_THE_ERRORS_ABOVE = "See the error(s) above.";
	EditAnnotationsDialogView view;
	SynapseJavascriptClient jsClient;
	PortalGinInjector ginInjector;
	String entityId;
	List<AnnotationEditor> annotationEditors;
	Annotations annotationsCopy;
	boolean commaSeparatedValuesParserExists;

	@Inject
	public EditAnnotationsDialog(EditAnnotationsDialogView view, SynapseJavascriptClient jsClient, PortalGinInjector ginInjector) {
		this.view = view;
		this.jsClient = jsClient;
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
		Map<String, AnnotationsValue> annnotationsMapCopy = new HashMap<>();
		annotationsCopy.setAnnotations(annnotationsMapCopy);
		annnotationsMapCopy.putAll(originalAnnotations.getAnnotations());
		annotationEditors = new ArrayList<AnnotationEditor>();
		for (String key : annotationsCopy.getAnnotations().keySet()) {
			AnnotationsValue value = originalAnnotations.getAnnotations().get(key);
			AnnotationEditor newEditor = createAnnotationEditor(key, value);
			view.addAnnotationEditor(newEditor.asWidget());
		}
		// if there are no annotations, prepopulate with a single default annotation
		if (annotationsCopy.getAnnotations().isEmpty()) {
			onAddNewAnnotation(null);
		}
		view.showEditor();
	}

	public AnnotationEditor createAnnotationEditor(String key, AnnotationsValue annotation) {
		final AnnotationEditor editor = ginInjector.getAnnotationEditor();
		Callback deletedCallback = () -> onAnnotationDeleted(editor);
		editor.configure(key, annotation, deletedCallback);
		annotationEditors.add(editor);
		return editor;
	}

	@Override
	public void onAddNewAnnotation(List<String> values) {
		String initialKey = "";
		AnnotationsValue annotationValue = new AnnotationsValue();
		annotationValue.setType(AnnotationsValueType.STRING);
		if(values != null) {
			annotationValue.setValue(values);
		}
		AnnotationEditor newEditor = createAnnotationEditor(initialKey, annotationValue);
		view.addAnnotationEditor(newEditor.asWidget());
	}

	@Override
	public void onAnnotationDeleted(AnnotationEditor editor) {
		annotationEditors.remove(editor);
		view.removeAnnotationEditor(editor.asWidget());
	}


	@Override
	public void onClickPasteNewValues(){
		//do not add another parser if one is already active
		if(this.commaSeparatedValuesParserExists){
			return;
		}
		CommaSeparatedValuesParser parser = ginInjector.getCommaSeparatedValuesParser();

		parser.configure(this::onAddNewAnnotation, this::onCancelPasteNewValues);
		view.addCommaSeparatedValuesParser(parser.asWidget());
		GWT.debugger();
		this.commaSeparatedValuesParserExists = true;
	}

	@Override
	public void onCancelPasteNewValues(CommaSeparatedValuesParser commaSeparatedValuesParser){
		//TODO: do we want to enforce singleton? if so we can avoid passing in the parser as an arg.
		view.removeCommaSeparatedValuesParser(commaSeparatedValuesParser.asWidget());
		this.commaSeparatedValuesParserExists = false;
	}

	@Override
	public void onCancel() {
		view.hideEditor();
	};

	@Override
	public void onSave() {
		// check all annotation editor validity
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

		// else, try to update the annotations
		view.setLoading();
		annotationsCopy.getAnnotations().clear();
		for (AnnotationEditor annotationEditor : annotationEditors) {
			String updatedKey = annotationEditor.getUpdatedKey();
			AnnotationsValue updatedValue = annotationEditor.getUpdatedAnnotation();
			annotationsCopy.getAnnotations().put(updatedKey, updatedValue);
		}
		jsClient.updateAnnotations(entityId, annotationsCopy, new AsyncCallback<Annotations>() {
			@Override
			public void onSuccess(Annotations result) {
				view.showInfo("Successfully updated the annotations");
				view.hideEditor();
				ginInjector.getEventBus().fireEvent(new EntityUpdatedEvent());
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showError(caught.getMessage());
			}
		});
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
