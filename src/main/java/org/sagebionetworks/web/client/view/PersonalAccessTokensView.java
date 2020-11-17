package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.presenter.PersonalAccessTokensPresenter;

import com.google.gwt.user.client.ui.IsWidget;

public interface PersonalAccessTokensView extends IsWidget {

	/**
	 * Set this view's presenter
	 * 
	 * @param presenter
	 */
	public void setPresenter(PersonalAccessTokensPresenter presenter);

	/**
	 * Renders the view for a given presenter
	 */
	public void render();

}
