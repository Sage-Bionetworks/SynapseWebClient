package org.sagebionetworks.web.client.widget.entity.row;

import java.util.List;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;

import com.google.gwt.user.client.ui.IsWidget;

public interface AnnotationsRendererWidgetView extends IsWidget{
	
	public interface Presenter {
		 void configure(Entity entity, Annotations annotations, boolean canEdit);
		 void onEdit();
	}

	void configure(List<EntityRow<?>> rows);
	void setPresenter(Presenter presenter);
	void setEditButtonVisible(boolean isVisible);
}
