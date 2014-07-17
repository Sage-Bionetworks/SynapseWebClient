package org.sagebionetworks.web.client.widget.table.v2;

import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnModel;

import com.google.gwt.user.client.ui.IsWidget;

public interface ColumnModelsView extends IsWidget {


	/**
	 * Configure a newly created view.
	 * 
	 * @param headerText
	 * @param models
	 * @param isEditabl
	 */
	public void configure(String headerText, List<ColumnModel> models, boolean isEditable);


}
