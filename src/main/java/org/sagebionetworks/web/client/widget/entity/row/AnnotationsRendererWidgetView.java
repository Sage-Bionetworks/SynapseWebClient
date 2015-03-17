package org.sagebionetworks.web.client.widget.entity.row;

import java.util.List;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;

import com.google.gwt.user.client.ui.IsWidget;

public interface AnnotationsRendererWidgetView extends IsWidget{
	
	public interface Presenter {
		 void configure(Annotations annotations, boolean canEdit);
		 void onEdit();
	}

	void configure(List<Annotation> annotations);
	void setPresenter(Presenter presenter);
	void setEditButtonVisible(boolean isVisible);
}
