package org.sagebionetworks.web.client.widget.entity.annotation;

import java.util.Map;
import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface AnnotationsRendererWidgetView extends IsWidget {

	public interface Presenter {
		void onEdit();
	}

	void configure(Map<String, AnnotationsValue> annotationsMap);

	void setPresenter(Presenter presenter);

	void setEditUIVisible(boolean isVisible);

	void showNoAnnotations();

	void addEditorToPage(Widget editorWidget);
}
