package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.COLUMN_SORT_TYPE;

import com.google.gwt.user.client.ui.IsWidget;

public interface APITableColumnManagerView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
		
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void configure(List<APITableColumnConfig> configs);
		void deleteColumnConfig(APITableColumnConfig config);
		void addColumnConfig(String rendererName, String inputColumnNames, String displayColumnName, COLUMN_SORT_TYPE sort);
	}

	/**
	 * Configures attachments view for this entity
	 * @param entity
	 */
	public void configure(List<APITableColumnConfig> configs);
}
