package org.sagebionetworks.web.client.widget.entity.annotation;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AnnotationEditorV2 implements AnnotationEditorV2View.Presenter {

	private AnnotationEditorV2View view;

	@Inject
	public AnnotationEditorV2(AnnotationEditorV2View view) {
		this.view = view;
		view.setPresenter(this);
	}

	public void configure(String entityId) {
		view.configure(entityId);
	}

	public Widget asWidget() {
		return view.asWidget();
	}
}
