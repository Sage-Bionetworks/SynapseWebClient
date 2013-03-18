package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;

import org.sagebionetworks.repo.model.widget.APITableColumnConfig;
import org.sagebionetworks.repo.model.widget.APITableColumnConfigList;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;

import com.google.gwt.user.client.ui.IsWidget;

public interface APITableColumnManagerView extends IsWidget, SynapseWidgetView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
		
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void configure(APITableColumnConfigList configs);
		void deleteColumnConfig(String tokenId);
		void addColumnConfig(String rendererName, String inputColumnNames, String displayColumnName);
	}

	/**
	 * Configures attachments view for this entity
	 * @param entity
	 */
	public void configure(List<APITableColumnConfig> configs);
	public void configDeleted(String tokenId, String deletedName);
}
