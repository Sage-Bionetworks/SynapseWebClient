package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.entity.row.EntityRow;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * An abstraction of the property widget view.
 * @author John
 *
 */
public interface AnnotationsWidgetView extends IsWidget, SynapseView{
	
	public interface Presenter {
		/**
		 * Set the entity bundle on this widget
		 * @param bundle
		 */
		 public void configure(EntityBundle bundle, boolean canEdit);
		 public void deleteAnnotation(EntityRow row);
		 public void updateAnnotation(EntityRow row);
		 public void addAnnotation(String name, ANNOTATION_TYPE type);
	}

	/**
	 * Set the rows to be displayed in the view
	 * @param rows
	 */
	void configure(List<EntityRow<?>> rows, boolean canEdit);
	void setPresenter(Presenter presenter);
}
