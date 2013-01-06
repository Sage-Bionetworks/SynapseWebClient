package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface EntityFinderView extends IsWidget, SynapseWidgetView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void setVersions(List<VersionInfo> versions);

	public int getViewWidth();
	
	public int getViewHeight();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

		void entitySelected(Reference selected);

		void lookupEntity(String entityId, AsyncCallback<Entity> callback);

		void loadVersions(String entityId);
		
		boolean showVersions();
	}

}
