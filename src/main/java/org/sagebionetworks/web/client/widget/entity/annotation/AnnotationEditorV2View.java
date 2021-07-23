package org.sagebionetworks.web.client.widget.entity.annotation;

import com.google.gwt.user.client.ui.IsWidget;

public interface AnnotationEditorV2View extends IsWidget {

	interface Presenter {
	}

	/**
	 * @param entityId
	 */
	void configure(String entityId);


	void setPresenter(Presenter presenter);

}
