package org.sagebionetworks.web.client.widget.table.v2;

import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnModel;

public interface ColumnModelsView {


	/**
	 * Configure a newly created view.
	 * 
	 * @param headerText
	 * @param models
	 * @param isEditabl
	 */
	public void configure(String headerText, List<ColumnModel> models, boolean isEditable);


}
