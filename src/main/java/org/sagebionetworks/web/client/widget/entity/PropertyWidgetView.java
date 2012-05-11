package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.entity.row.EntityRow;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * An abstraction of the property widget view.
 * @author John
 *
 */
public interface PropertyWidgetView extends IsWidget{
	
	public interface Presenter {
		/**
		 * Set the entity bundle on this widget
		 * @param bundle
		 */
		 public void setEntityBundle(EntityBundle bundle);
	}

	/**
	 * Set the rows to be displayed in the view
	 * @param rows
	 */
	void setRows(List<EntityRow<?>> rows);
}
