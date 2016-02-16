package org.sagebionetworks.web.client.widget.entity.annotation;

import java.util.List;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface AnnotationsRendererWidgetView extends IsWidget{
	
	public interface Presenter {
		 void onEdit();
	}

	void configure(List<Annotation> annotations);
	void setPresenter(Presenter presenter);
	void setEditUIVisible(boolean isVisible);
	void showNoAnnotations();
	void addEditorToPage(Widget editorWidget);
}
