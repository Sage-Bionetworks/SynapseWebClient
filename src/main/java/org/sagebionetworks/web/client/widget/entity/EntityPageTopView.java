package org.sagebionetworks.web.client.widget.entity;

import java.util.TreeMap;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface EntityPageTopView extends IsWidget, SynapseWidgetView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void setEntityBundle(EntityBundle bundle, String entityTypeDisplay, boolean isAdmin, boolean canEdit, boolean readOnly);

	/**
	 * Sets the RStudio URL for the view
	 * @param rStudioUrl
	 */
	public void setRStudioUrlReady();

	/**
	 * Presenter interface
	 */
	public interface Presenter {

		void refresh();

		void fireEntityUpdatedEvent();

		boolean isLocationable();

		boolean isLoggedIn();

//		String getRstudioUrl();
//
//		void saveRStudioUrlBase(String value);
//
//		String getRstudioUrlBase();

		void loadShortcuts(int offset, int limit, AsyncCallback<PaginatedResults<EntityHeader>> asyncCallback);

		String createEntityLink(String id, String version, String display);

		ImageResource getIconForType(String typeString);
		
		void getHtmlFromMarkdown(String description, AsyncCallback<String> asyncCallback);
	}

	public void setEntityVersions(Versionable entity, TreeMap<Long, String> latestVersions);


}
