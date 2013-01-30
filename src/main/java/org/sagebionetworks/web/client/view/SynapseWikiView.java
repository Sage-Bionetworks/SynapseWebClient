package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface SynapseWikiView extends IsWidget, SynapseView {
	public void showPage(String ownerId, String ownerType, boolean canEdit, String wikiId);
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);	
	
	public interface Presenter extends SynapsePresenter {
		public void configure(String ownerId, String ownerType, boolean canEdit, String wikiId);
	}

}
